package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.DashboardRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Step 10: Complete CPI Withdrawal Flow Test Suite
 *
 * Verifies the full end-to-end withdrawal lifecycle:
 * - Test A: Insufficient balance & limit validations
 * - Test B: Valid submission -> PENDING_REVIEW & funds reservation
 * - Test C: Admin rejection -> REJECTED & reserved funds released back to available balance
 * - Test D: Valid submission -> PROCESSING -> COMPLETED -> funds deducted exactly once
 * - Test E: Duplicate completion protection
 * - Test F: Ledger consistency across overview, history, notifications & audit logs
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CompleteWithdrawalFlowTest {

    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var adminRepository: AdminRepository

    private lateinit var investorUsdt: UserEntity
    private lateinit var investorPhp: UserEntity
    private lateinit var complianceAdmin: UserEntity

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        authRepository = AuthRepository(database.userDao())
        dashboardRepository = DashboardRepository(
            investmentDao = database.investmentDao(),
            transactionDao = database.transactionDao(),
            paymentAccountDao = database.paymentAccountDao(),
            withdrawalDao = database.withdrawalDao(),
            notificationDao = database.notificationDao()
        )
        adminRepository = AdminRepository(
            userDao = database.userDao(),
            investmentDao = database.investmentDao(),
            transactionDao = database.transactionDao(),
            auditLogDao = database.auditLogDao(),
            paymentAccountDao = database.paymentAccountDao(),
            withdrawalDao = database.withdrawalDao(),
            notificationDao = database.notificationDao()
        )

        // Seed Users
        investorUsdt = UserEntity(
            id = "USR_USDT_FLOW_01",
            fullName = "USDT Capital Partner",
            email = "partner_usdt@cpi.com",
            phoneNumber = "+12025550143",
            country = "United States",
            occupation = "Fund Manager",
            selectedCurrency = "USDT",
            passwordHash = "hash123",
            passwordSalt = "salt123",
            role = "INVESTOR"
        )
        investorPhp = UserEntity(
            id = "USR_PHP_FLOW_01",
            fullName = "PHP Capital Partner",
            email = "partner_php@cpi.com",
            phoneNumber = "+639185550199",
            country = "Philippines",
            occupation = "Real Estate Investor",
            selectedCurrency = "PHP",
            passwordHash = "hash123",
            passwordSalt = "salt123",
            role = "INVESTOR"
        )
        complianceAdmin = UserEntity(
            id = "ADMIN_COMPLIANCE_01",
            fullName = "Chief Risk Officer",
            email = "risk@cpi.com",
            phoneNumber = "+6561234567",
            country = "Singapore",
            occupation = "Compliance",
            selectedCurrency = "USDT",
            passwordHash = "adminHash123",
            passwordSalt = "saltAdmin123",
            role = "ADMIN"
        )

        database.userDao().insertUser(investorUsdt)
        database.userDao().insertUser(investorPhp)
        database.userDao().insertUser(complianceAdmin)

        // Seed Verified Initial Deposit Capital
        database.transactionDao().insertTransaction(
            TransactionEntity(
                id = "TX_INIT_USDT_1000",
                userId = investorUsdt.id,
                type = "DEPOSIT",
                amount = 2000.0,
                currency = "USDT",
                status = "COMPLETED",
                reference = "DEP-USDT-2000-REF",
                paymentMethod = "TRC20",
                notes = "Initial verified equity"
            )
        )
        database.transactionDao().insertTransaction(
            TransactionEntity(
                id = "TX_INIT_PHP_100K",
                userId = investorPhp.id,
                type = "DEPOSIT",
                amount = 80000.0,
                currency = "PHP",
                status = "COMPLETED",
                reference = "DEP-PHP-80K-REF",
                paymentMethod = "GCash",
                notes = "Initial verified equity"
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==========================================
    // TEST A: Attempt withdrawal greater than available balance
    // ==========================================
    @Test
    fun `TEST A - Attempt withdrawal greater than available balance is rejected`() = runBlocking {
        val initialUsdt = dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT")
        assertEquals(2000.0, initialUsdt, 0.001)

        // Attempting to withdraw 2500 USDT (exceeds 2000 available balance)
        val excessiveResult = dashboardRepository.submitWithdrawal(
            userId = investorUsdt.id,
            amount = 2500.0,
            currency = "USDT",
            destination = "TXN9876543210TRC20SAMPLEADDRESS",
            network = "TRC20",
            isSecurityVerified = true
        )

        assertFalse("Withdrawal exceeding available balance must be rejected", excessiveResult.isSuccess)
        val errorMsg = excessiveResult.exceptionOrNull()?.message ?: ""
        assertTrue("Error message should mention exceeding balance", errorMsg.contains("exceeds available balance"))

        // Also test zero or negative withdrawal
        val zeroResult = dashboardRepository.submitWithdrawal(
            userId = investorUsdt.id,
            amount = 0.0,
            currency = "USDT",
            destination = "TXN9876543210TRC20SAMPLEADDRESS",
            network = "TRC20",
            isSecurityVerified = true
        )
        assertFalse("Zero withdrawal must be rejected", zeroResult.isSuccess)

        // Balance remains intact at 2000
        val remainingUsdt = dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT")
        assertEquals(2000.0, remainingUsdt, 0.001)
    }

    // ==========================================
    // TEST B: Submit a valid withdrawal -> PENDING_REVIEW & balance reserved
    // ==========================================
    @Test
    fun `TEST B - Valid withdrawal submission creates PENDING_REVIEW and reserves balance`() = runBlocking {
        // Step 1: Request OTP
        val otpRes = dashboardRepository.generateWithdrawalOtp(investorUsdt.id)
        assertTrue("OTP generation should succeed", otpRes.isSuccess)

        // Verify security notification was created
        val notifs = database.notificationDao().observeNotificationsForUser(investorUsdt.id).first()
        val otpNotif = notifs.find { it.type == "SECURITY" }
        assertNotNull("Security OTP notification should be delivered", otpNotif)

        // Step 2: Submit 500 USDT withdrawal
        val wthResult = dashboardRepository.submitWithdrawal(
            userId = investorUsdt.id,
            amount = 500.0,
            currency = "USDT",
            destination = "TXN_TRC20_WALLET_SAFE_123",
            network = "TRC20",
            isSecurityVerified = true
        )

        assertTrue("Withdrawal submission must succeed", wthResult.isSuccess)
        val wth = wthResult.getOrThrow()
        assertEquals("PENDING_REVIEW", wth.status)
        assertEquals(500.0, wth.amount, 0.001)
        assertEquals("USDT", wth.currency)

        // Step 3: Available balance must be reduced to 2000 - 500 = 1500
        val available = dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT")
        assertEquals(1500.0, available, 0.001)

        // Overview reflects pending reserved withdrawal
        val overview = dashboardRepository.observeOverview(investorUsdt.id).first()
        assertEquals(1500.0, overview.availableBalance, 0.001)
        assertEquals(500.0, overview.pendingWithdrawals, 0.001)

        // Transaction table has corresponding PENDING withdrawal transaction
        val tx = database.transactionDao().getTransactionById(wth.transactionId ?: "")
        assertNotNull("Ledger transaction must exist", tx)
        assertEquals("WITHDRAWAL", tx?.type)
        assertEquals("PENDING", tx?.status)
    }

    // ==========================================
    // TEST C: Admin rejects the withdrawal -> REJECTED & reserved funds return to available balance
    // ==========================================
    @Test
    fun `TEST C - Admin rejection marks REJECTED and restores reserved funds to available balance`() = runBlocking {
        // Submit 20,000 PHP withdrawal
        val wthResult = dashboardRepository.submitWithdrawal(
            userId = investorPhp.id,
            amount = 20000.0,
            currency = "PHP",
            destination = "GCash: 09185550199 (Juan)",
            network = "GCash",
            isSecurityVerified = true
        )
        val wth = wthResult.getOrThrow()

        // Available balance is reserved: 80,000 - 20,000 = 60,000
        val midBal = dashboardRepository.getAvailableBalance(investorPhp.id, "PHP")
        assertEquals(60000.0, midBal, 0.001)

        // Admin rejects
        val rejectResult = adminRepository.updateWithdrawalStatus(
            adminId = complianceAdmin.id,
            adminEmail = complianceAdmin.email,
            withdrawalId = wth.withdrawalId,
            newStatus = "REJECTED",
            reason = "Beneficiary name mismatch with registered KYC passport"
        )
        assertTrue("Admin rejection should succeed", rejectResult.isSuccess)

        // Reserved funds returned to available balance: restored to 80,000
        val restoredBal = dashboardRepository.getAvailableBalance(investorPhp.id, "PHP")
        assertEquals(80000.0, restoredBal, 0.001)

        // Overview pending withdrawals cleared
        val overview = dashboardRepository.observeOverview(investorPhp.id).first()
        assertEquals(80000.0, overview.availableBalance, 0.001)
        assertEquals(0.0, overview.pendingWithdrawals, 0.001)

        // Withdrawal record shows REJECTED with reason
        val updatedWth = database.withdrawalDao().getWithdrawalById(wth.withdrawalId)
        assertEquals("REJECTED", updatedWth?.status)
        assertEquals("Beneficiary name mismatch with registered KYC passport", updatedWth?.rejectionReason)
    }

    // ==========================================
    // TEST D: Submit withdrawal -> PROCESSING -> COMPLETED -> deducted exactly once
    // ==========================================
    @Test
    fun `TEST D - Submission to PROCESSING to COMPLETED permanently deducts funds exactly once`() = runBlocking {
        // Submit 600 USDT withdrawal
        val wthResult = dashboardRepository.submitWithdrawal(
            userId = investorUsdt.id,
            amount = 600.0,
            currency = "USDT",
            destination = "0xBEP20_0123456789abcdef0123456789abcdef01",
            network = "BEP20",
            isSecurityVerified = true
        )
        val wth = wthResult.getOrThrow()

        // Balance while pending: 2000 - 600 = 1400
        assertEquals(1400.0, dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT"), 0.001)

        // Admin transitions to PROCESSING
        val procResult = adminRepository.updateWithdrawalStatus(
            adminId = complianceAdmin.id,
            adminEmail = complianceAdmin.email,
            withdrawalId = wth.withdrawalId,
            newStatus = "PROCESSING",
            reason = "Dispatched to Binance hot wallet queue"
        )
        assertTrue("Transition to PROCESSING must succeed", procResult.isSuccess)
        val procWth = database.withdrawalDao().getWithdrawalById(wth.withdrawalId)
        assertEquals("PROCESSING", procWth?.status)

        // Balance remains reserved (1400)
        assertEquals(1400.0, dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT"), 0.001)

        // Admin transitions to COMPLETED
        val completeResult = adminRepository.updateWithdrawalStatus(
            adminId = complianceAdmin.id,
            adminEmail = complianceAdmin.email,
            withdrawalId = wth.withdrawalId,
            newStatus = "COMPLETED",
            reason = "On-chain tx confirmed: 0x9876543210fedcba"
        )
        assertTrue("Transition to COMPLETED must succeed", completeResult.isSuccess)

        // Final balance is permanently 1400 (deducted exactly once)
        val finalBal = dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT")
        assertEquals(1400.0, finalBal, 0.001)

        // Overview totalWithdrawn is 600, pending is 0
        val overview = dashboardRepository.observeOverview(investorUsdt.id).first()
        assertEquals(1400.0, overview.availableBalance, 0.001)
        assertEquals(600.0, overview.totalWithdrawn, 0.001)
        assertEquals(0.0, overview.pendingWithdrawals, 0.001)
    }

    // ==========================================
    // TEST E: Duplicate completion protection
    // ==========================================
    @Test
    fun `TEST E - Duplicate completion attempt is rejected without double deduction`() = runBlocking {
        // Complete a 400 USDT withdrawal
        val wth = dashboardRepository.submitWithdrawal(
            userId = investorUsdt.id,
            amount = 400.0,
            currency = "USDT",
            destination = "TXN_TRC20_WALLET_SAFE_123",
            network = "TRC20",
            isSecurityVerified = true
        ).getOrThrow()

        adminRepository.updateWithdrawalStatus(
            adminId = complianceAdmin.id,
            adminEmail = complianceAdmin.email,
            withdrawalId = wth.withdrawalId,
            newStatus = "COMPLETED",
            reason = "Initial valid payout"
        )

        val balanceAfterFirst = dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT")
        assertEquals(1600.0, balanceAfterFirst, 0.001)

        // Second attempt to complete the already completed withdrawal
        val duplicateResult = adminRepository.updateWithdrawalStatus(
            adminId = complianceAdmin.id,
            adminEmail = complianceAdmin.email,
            withdrawalId = wth.withdrawalId,
            newStatus = "COMPLETED",
            reason = "Duplicate trigger attempt"
        )

        assertFalse("Duplicate completion must be rejected", duplicateResult.isSuccess)

        // Balance remains 1600 (no double deduction)
        val balanceAfterSecond = dashboardRepository.getAvailableBalance(investorUsdt.id, "USDT")
        assertEquals(1600.0, balanceAfterSecond, 0.001)

        // Verify only 1 withdrawal transaction exists
        val txs = database.transactionDao().observeTransactionsByUserId(investorUsdt.id).first()
        val withdrawalTxs = txs.filter { it.id == wth.transactionId }
        assertEquals(1, withdrawalTxs.size)
    }

    // ==========================================
    // TEST F: Comprehensive check across all layers
    // ==========================================
    @Test
    fun `TEST F - Full cross-module audit of dashboard, history, transactions, notifications and audit logs`() = runBlocking {
        // Submit 10,000 PHP withdrawal
        val wth = dashboardRepository.submitWithdrawal(
            userId = investorPhp.id,
            amount = 10000.0,
            currency = "PHP",
            destination = "Maya: 09185550199 (Maria)",
            network = "Maya",
            isSecurityVerified = true
        ).getOrThrow()

        // Admin completes
        adminRepository.updateWithdrawalStatus(
            adminId = complianceAdmin.id,
            adminEmail = complianceAdmin.email,
            withdrawalId = wth.withdrawalId,
            newStatus = "COMPLETED",
            reason = "InstaPay reference #99281726"
        )

        // 1. Dashboard Overview
        val overview = dashboardRepository.observeOverview(investorPhp.id).first()
        assertEquals(70000.0, overview.availableBalance, 0.001) // 80k initial - 10k withdrawn
        assertEquals(10000.0, overview.totalWithdrawn, 0.001)
        assertEquals(0.0, overview.pendingWithdrawals, 0.001)

        // 2. Withdrawal History
        val withdrawals = database.withdrawalDao().observeWithdrawalsByUserId(investorPhp.id).first()
        val historyItem = withdrawals.find { it.withdrawalId == wth.withdrawalId }
        assertNotNull("Withdrawal must appear in history", historyItem)
        assertEquals("COMPLETED", historyItem?.status)
        assertEquals(10000.0, historyItem!!.amount, 0.001)

        // 3. Transaction History
        val txs = database.transactionDao().observeTransactionsByUserId(investorPhp.id).first()
        val wthTx = txs.find { it.id == wth.transactionId }
        assertNotNull("Withdrawal transaction must be in transaction history", wthTx)
        assertEquals("COMPLETED", wthTx?.status)
        assertEquals("WITHDRAWAL", wthTx?.type)

        // 4. Notifications
        val userNotifs = database.notificationDao().observeNotificationsForUser(investorPhp.id).first()
        val completionNotif = userNotifs.find { it.relatedId == wth.withdrawalId }
        assertNotNull("User must receive notification for completed withdrawal", completionNotif)
        assertTrue("Notification title matches completion", completionNotif?.title?.contains("Completed") == true)

        // 5. Admin Audit Logs
        val auditLogs = database.auditLogDao().observeAllAuditLogs().first()
        val wthAudit = auditLogs.find { it.targetId == wth.withdrawalId }
        assertNotNull("Audit log must be registered for the action", wthAudit)
        assertEquals("ADMIN_APPROVE_WITHDRAWAL", wthAudit?.action)
        assertEquals(complianceAdmin.email, wthAudit?.adminEmail)
    }
}
