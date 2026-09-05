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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WithdrawalSystemTest {

    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var adminRepository: AdminRepository

    private lateinit var testInvestorUsdt: UserEntity
    private lateinit var testInvestorPhp: UserEntity
    private lateinit var testAdmin: UserEntity

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
        testInvestorUsdt = UserEntity(
            id = "USR_USDT_01",
            fullName = "USDT Investor",
            email = "usdt@cpi.com",
            phoneNumber = "+1234567890",
            country = "United States",
            occupation = "Investor",
            selectedCurrency = "USDT",
            passwordHash = "hash123",
            passwordSalt = "salt123",
            role = "INVESTOR"
        )
        testInvestorPhp = UserEntity(
            id = "USR_PHP_01",
            fullName = "PHP Investor",
            email = "php@cpi.com",
            phoneNumber = "+639171234567",
            country = "Philippines",
            occupation = "Trader",
            selectedCurrency = "PHP",
            passwordHash = "hash123",
            passwordSalt = "salt123",
            role = "INVESTOR"
        )
        testAdmin = UserEntity(
            id = "USR_ADMIN_01",
            fullName = "Compliance Officer",
            email = "admin@cpi.com",
            phoneNumber = "+1987654321",
            country = "Singapore",
            occupation = "Admin",
            selectedCurrency = "USDT",
            passwordHash = "adminHash123",
            passwordSalt = "saltAdmin123",
            role = "ADMIN"
        )

        database.userDao().insertUser(testInvestorUsdt)
        database.userDao().insertUser(testInvestorPhp)
        database.userDao().insertUser(testAdmin)

        // Seed initial completed deposits
        database.transactionDao().insertTransaction(
            TransactionEntity(
                id = "TX_DEP_01",
                userId = testInvestorUsdt.id,
                type = "DEPOSIT",
                amount = 1000.0,
                currency = "USDT",
                status = "COMPLETED",
                reference = "DEP-REF-USDT",
                paymentMethod = "TRC20",
                notes = "Initial verified capital"
            )
        )

        database.transactionDao().insertTransaction(
            TransactionEntity(
                id = "TX_DEP_02",
                userId = testInvestorPhp.id,
                type = "DEPOSIT",
                amount = 50000.0,
                currency = "PHP",
                status = "COMPLETED",
                reference = "DEP-REF-PHP",
                paymentMethod = "GCash",
                notes = "Initial verified capital"
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // 1. Initial available balance calculation
    @Test
    fun `test initial available balance calculation`() = runBlocking {
        val usdtBalance = dashboardRepository.getAvailableBalance(testInvestorUsdt.id, "USDT")
        assertEquals(1000.0, usdtBalance, 0.001)

        val phpBalance = dashboardRepository.getAvailableBalance(testInvestorPhp.id, "PHP")
        assertEquals(50000.0, phpBalance, 0.001)
    }

    // 2. OTP Generation and Verification
    @Test
    fun `test OTP generation and validation flow`() = runBlocking {
        val genResult = dashboardRepository.generateWithdrawalOtp(testInvestorUsdt.id)
        assertTrue(genResult.isSuccess)
        assertTrue(genResult.getOrThrow() > System.currentTimeMillis())

        // Wrong OTP fails
        val wrongResult = dashboardRepository.verifyWithdrawalOtp(
            userId = testInvestorUsdt.id,
            inputOtp = "000000"
        )
        assertFalse(wrongResult)
    }

    // 3. Successful withdrawal request reserves available balance
    @Test
    fun `test successful withdrawal request reserves balance and creates record`() = runBlocking {
        val submitResult = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = 400.0,
            currency = "USDT",
            destination = "TXN9876543210TRC20SAMPLEADDRESS",
            network = "USDT (TRC20)",
            isSecurityVerified = true
        )

        assertTrue("Withdrawal request should succeed", submitResult.isSuccess)
        val withdrawal = submitResult.getOrThrow()
        assertEquals("PENDING_REVIEW", withdrawal.status)
        assertEquals(400.0, withdrawal.amount, 0.001)
        assertEquals("USDT", withdrawal.currency)

        // Available balance should now be 1000 - 400 = 600
        val remainingBalance = dashboardRepository.getAvailableBalance(testInvestorUsdt.id, "USDT")
        assertEquals(600.0, remainingBalance, 0.001)

        // Total pending reservations in overview
        val overview = dashboardRepository.observeOverview(testInvestorUsdt.id).first()
        assertEquals(400.0, overview.pendingWithdrawals, 0.001)
    }

    // 4. Double-withdrawal protection: requesting more than remaining available balance fails
    @Test
    fun `test double withdrawal protection prevents exceeding available balance`() = runBlocking {
        // First withdrawal of 700
        val res1 = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = 700.0,
            currency = "USDT",
            destination = "0x1234567890abcdef1234567890abcdef12345678",
            network = "USDT (BEP20)",
            isSecurityVerified = true
        )
        assertTrue(res1.isSuccess)

        // Remaining balance is 300. Attempting to withdraw 400 must be rejected
        val res2 = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = 400.0,
            currency = "USDT",
            destination = "0x1234567890abcdef1234567890abcdef12345678",
            network = "USDT (BEP20)",
            isSecurityVerified = true
        )
        assertFalse("Second withdrawal exceeding remaining balance must fail", res2.isSuccess)
    }

    // 5. Zero or negative withdrawal amounts rejected
    @Test
    fun `test zero or negative amounts rejected`() = runBlocking {
        val resZero = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = 0.0,
            currency = "USDT",
            destination = "TXN9876543210TRC20SAMPLEADDRESS",
            network = "USDT (TRC20)",
            isSecurityVerified = true
        )
        assertFalse("Zero withdrawal must fail", resZero.isSuccess)

        val resNeg = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = -50.0,
            currency = "USDT",
            destination = "TXN9876543210TRC20SAMPLEADDRESS",
            network = "USDT (TRC20)",
            isSecurityVerified = true
        )
        assertFalse("Negative withdrawal must fail", resNeg.isSuccess)
    }

    // 6. Currency segregation
    @Test
    fun `test currency segregation - invalid currency rejected`() = runBlocking {
        val result = dashboardRepository.submitWithdrawal(
            userId = testInvestorPhp.id,
            amount = 5000.0,
            currency = "EUR", // Unsupported currency
            destination = "EUR_IBAN_123456",
            network = "SEPA",
            isSecurityVerified = true
        )
        assertFalse("Unsupported currency must fail", result.isSuccess)
    }

    // 7. Destination validation: empty or short destination rejected
    @Test
    fun `test empty destination address rejected`() = runBlocking {
        val result = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = 100.0,
            currency = "USDT",
            destination = "   ",
            network = "USDT (TRC20)",
            isSecurityVerified = true
        )
        assertFalse("Blank destination must fail", result.isSuccess)
    }

    // 8. Admin approval completes withdrawal and updates transaction record
    @Test
    fun `test admin approval completes withdrawal and writes transaction ledger`() = runBlocking {
        val reqResult = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = 300.0,
            currency = "USDT",
            destination = "TXN9876543210TRC20SAMPLEADDRESS",
            network = "USDT (TRC20)",
            isSecurityVerified = true
        )
        val withdrawal = reqResult.getOrThrow()

        // Admin approves
        val approveResult = adminRepository.updateWithdrawalStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            withdrawalId = withdrawal.withdrawalId,
            newStatus = "COMPLETED",
            reason = "Payout confirmed on blockchain via TXN_HASH_ABC123"
        )
        assertTrue("Admin approval should succeed", approveResult.isSuccess)

        // Verify withdrawal record updated
        val updated = database.withdrawalDao().getWithdrawalById(withdrawal.withdrawalId)
        assertNotNull(updated)
        assertEquals("COMPLETED", updated?.status)
        assertEquals(testAdmin.id, updated?.processedByAdminId)

        // Verify transaction record was marked COMPLETED
        val txs = database.transactionDao().observeTransactionsByUserId(testInvestorUsdt.id).first()
        val withdrawalTx = txs.find { it.id == withdrawal.transactionId }
        assertNotNull("Withdrawal transaction record must exist", withdrawalTx)
        assertEquals(300.0, withdrawalTx!!.amount, 0.001)
        assertEquals("COMPLETED", withdrawalTx.status)

        // Available balance is now permanently 700
        val finalBalance = dashboardRepository.getAvailableBalance(testInvestorUsdt.id, "USDT")
        assertEquals(700.0, finalBalance, 0.001)
    }

    // 9. Admin rejection releases reserved balance back to user
    @Test
    fun `test admin rejection releases reserved balance back to investor`() = runBlocking {
        val reqResult = dashboardRepository.submitWithdrawal(
            userId = testInvestorPhp.id,
            amount = 20000.0,
            currency = "PHP",
            destination = "GCash: 09171234567 (Juan Dela Cruz)",
            network = "GCash",
            isSecurityVerified = true
        )
        val withdrawal = reqResult.getOrThrow()

        // While pending, available balance is 50000 - 20000 = 30000
        val midBalance = dashboardRepository.getAvailableBalance(testInvestorPhp.id, "PHP")
        assertEquals(30000.0, midBalance, 0.001)

        // Admin rejects
        val rejectResult = adminRepository.updateWithdrawalStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            withdrawalId = withdrawal.withdrawalId,
            newStatus = "REJECTED",
            reason = "GCash account name mismatch with KYC document"
        )
        assertTrue("Admin rejection should succeed", rejectResult.isSuccess)

        // After rejection, reserved funds are released back -> available balance is 50000
        val restoredBalance = dashboardRepository.getAvailableBalance(testInvestorPhp.id, "PHP")
        assertEquals(50000.0, restoredBalance, 0.001)

        val updated = database.withdrawalDao().getWithdrawalById(withdrawal.withdrawalId)
        assertEquals("REJECTED", updated?.status)
        assertEquals("GCash account name mismatch with KYC document", updated?.rejectionReason)
    }

    // 10. Audit log is written for admin withdrawal actions
    @Test
    fun `test audit log recorded for admin actions`() = runBlocking {
        val req = dashboardRepository.submitWithdrawal(
            userId = testInvestorUsdt.id,
            amount = 100.0,
            currency = "USDT",
            destination = "0x1234567890abcdef1234567890abcdef12345678",
            network = "USDT (BEP20)",
            isSecurityVerified = true
        ).getOrThrow()

        adminRepository.updateWithdrawalStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            withdrawalId = req.withdrawalId,
            newStatus = "PROCESSING",
            reason = "Queued in hot wallet batch"
        )

        val logs = database.auditLogDao().observeAllAuditLogs().first()
        val withdrawalLog = logs.find { it.targetId == req.withdrawalId }
        assertNotNull("Audit log must be recorded", withdrawalLog)
        assertEquals("ADMIN_PROCESS_WITHDRAWAL", withdrawalLog?.action)
        assertEquals(testAdmin.email, withdrawalLog?.adminEmail)
    }
}
