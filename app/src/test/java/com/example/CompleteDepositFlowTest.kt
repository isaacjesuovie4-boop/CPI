package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.PaymentAccountEntity
import com.example.data.local.UserEntity
import com.example.data.repository.AdminRepository
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
class CompleteDepositFlowTest {

    private lateinit var database: AppDatabase
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var adminRepository: AdminRepository

    private lateinit var testInvestor: UserEntity
    private lateinit var testAdmin: UserEntity
    private lateinit var validUsdtAccount: PaymentAccountEntity
    private lateinit var validPhpAccount: PaymentAccountEntity

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

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

        testInvestor = UserEntity(
            id = "USR-INV-001",
            fullName = "Isaac Investor",
            email = "investor@example.com",
            phoneNumber = "+639171234567",
            country = "Philippines",
            occupation = "Software Engineer",
            selectedCurrency = "USDT",
            passwordHash = "hash123",
            passwordSalt = "salt123",
            role = "INVESTOR",
            accountStatus = "ACTIVE"
        )
        database.userDao().insertUser(testInvestor)

        testAdmin = UserEntity(
            id = "ADM-CPI-001",
            fullName = "CPI Global Administrator",
            email = "admin@cpi.com",
            phoneNumber = "+1234567890",
            country = "Philippines",
            occupation = "Compliance Officer",
            selectedCurrency = "USDT",
            passwordHash = "adminhash",
            passwordSalt = "adminsalt",
            role = "ADMIN",
            accountStatus = "ACTIVE"
        )
        database.userDao().insertUser(testAdmin)

        val now = System.currentTimeMillis()
        val expires30Min = now + (30 * 60 * 1000L)

        validUsdtAccount = PaymentAccountEntity(
            id = "ACC-USDT-TRC20-TEST",
            currency = "USDT",
            paymentMethod = "USDT TRC20 Gateway",
            network = "TRC20",
            accountName = null,
            accountNumber = null,
            walletAddress = "TXaZ7Q9xKpLm4N2wR8vY6sJ1dF3gH5bC8e",
            instructions = "Send exact USDT TRC20 amount and submit TxHash.",
            isActive = true,
            isPublished = true,
            publishedAt = now,
            expiresAt = expires30Min,
            createdAt = now,
            updatedAt = now
        )
        database.paymentAccountDao().insertPaymentAccount(validUsdtAccount)

        validPhpAccount = PaymentAccountEntity(
            id = "ACC-PHP-GCASH-TEST",
            currency = "PHP",
            paymentMethod = "GCash Direct",
            network = null,
            accountName = "CPI Official Merchant Account",
            accountNumber = "0917-888-9999",
            walletAddress = null,
            instructions = "Transfer exact PHP amount via GCash and submit reference number.",
            isActive = true,
            isPublished = true,
            publishedAt = now,
            expiresAt = expires30Min,
            createdAt = now,
            updatedAt = now
        )
        database.paymentAccountDao().insertPaymentAccount(validPhpAccount)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testA_depositBelowOrAboveLimits_isRejectedByValidation() = runBlocking {
        // USDT Limits: 50–5,000 USDT
        val usdtBelow = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validUsdtAccount.id,
            amount = 49.99,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TXHASH12345"
        )
        assertTrue("USDT below 50 must fail", usdtBelow.isFailure)
        assertTrue(usdtBelow.exceptionOrNull()?.message?.contains("Minimum deposit for USDT is 50") == true)

        val usdtAbove = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validUsdtAccount.id,
            amount = 5001.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TXHASH12345"
        )
        assertTrue("USDT above 5000 must fail", usdtAbove.isFailure)
        assertTrue(usdtAbove.exceptionOrNull()?.message?.contains("Maximum deposit for USDT is 5,000") == true)

        // PHP Limits: 3,000–100,000 PHP
        val phpBelow = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validPhpAccount.id,
            amount = 2999.0,
            currency = "PHP",
            paymentMethod = "GCash",
            referenceNo = "REF12345"
        )
        assertTrue("PHP below 3000 must fail", phpBelow.isFailure)
        assertTrue(phpBelow.exceptionOrNull()?.message?.contains("Minimum deposit for PHP is ₱3,000") == true)

        val phpAbove = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validPhpAccount.id,
            amount = 100001.0,
            currency = "PHP",
            paymentMethod = "GCash",
            referenceNo = "REF12345"
        )
        assertTrue("PHP above 100000 must fail", phpAbove.isFailure)
        assertTrue(phpAbove.exceptionOrNull()?.message?.contains("Maximum deposit for PHP is ₱100,000") == true)

        // No transactions created
        val txs = database.transactionDao().getTransactionsByUserId(testInvestor.id)
        assertEquals(0, txs.size)
    }

    @Test
    fun testB_createValidDeposit_createsPendingTransactionAndDoesNotIncreaseBalance() = runBlocking {
        // Initial balance is 0.0
        val initialBalance = dashboardRepository.getAvailableBalance(testInvestor.id, "USDT")
        assertEquals(0.0, initialBalance, 0.001)

        val depositResult = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validUsdtAccount.id,
            amount = 500.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TRX-HASH-998877",
            notes = "First test deposit"
        )

        assertTrue("Valid deposit submission must succeed", depositResult.isSuccess)
        val tx = depositResult.getOrNull()
        assertNotNull(tx)
        assertEquals("PENDING", tx?.status)
        assertEquals(500.0, tx?.amount ?: 0.0, 0.001)
        assertEquals("USDT", tx?.currency)

        // Available balance MUST NOT increase yet while PENDING
        val balanceAfterPending = dashboardRepository.getAvailableBalance(testInvestor.id, "USDT")
        assertEquals("Available balance must remain 0.0 while deposit is PENDING", 0.0, balanceAfterPending, 0.001)

        // Investor Notification created
        val notifications = database.notificationDao().observeNotificationsForUser(testInvestor.id).first()
        assertTrue("Investor must receive deposit submission notification", notifications.any { it.title == "Deposit Submitted" })

        // Admin Notification created
        val adminNotifications = database.notificationDao().observeAdminNotifications().first()
        assertTrue("Admin must receive awaiting review notification", adminNotifications.any { it.title == "New Deposit Awaiting Review" })
    }

    @Test
    fun testC_adminRejectsDeposit_balanceRemainsZeroAndAuditLogRecorded() = runBlocking {
        val depositResult = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validUsdtAccount.id,
            amount = 200.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TRX-REJECT-001"
        )
        assertTrue(depositResult.isSuccess)
        val txId = depositResult.getOrThrow().id

        // Admin rejects deposit
        val rejectResult = adminRepository.updateTransactionStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            transactionId = txId,
            newStatus = "REJECTED",
            reason = "Invalid transaction hash provided"
        )
        assertTrue("Admin reject action must succeed", rejectResult.isSuccess)

        // Check Transaction status
        val updatedTx = database.transactionDao().getTransactionById(txId)
        assertNotNull(updatedTx)
        assertEquals("REJECTED", updatedTx?.status)

        // Balance must remain 0.0
        val balance = dashboardRepository.getAvailableBalance(testInvestor.id, "USDT")
        assertEquals(0.0, balance, 0.001)

        // Investor receives rejection notification
        val notifs = database.notificationDao().observeNotificationsForUser(testInvestor.id).first()
        assertTrue("Investor must receive deposit rejection notification", notifs.any { it.title == "Deposit Rejected" })

        // Audit log created
        val auditLogs = database.auditLogDao().getRecentAuditLogs(50)
        val rejectLog = auditLogs.find { it.action == "ADMIN_REJECT_DEPOSIT" && it.targetId == txId }
        assertNotNull("Audit log must record ADMIN_REJECT_DEPOSIT", rejectLog)
        assertEquals(testAdmin.id, rejectLog?.adminId)
    }

    @Test
    fun testD_adminApprovesDeposit_balanceIncreasesExactlyOnce() = runBlocking {
        val depositResult = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validUsdtAccount.id,
            amount = 1000.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TRX-APPROVE-1000"
        )
        assertTrue(depositResult.isSuccess)
        val txId = depositResult.getOrThrow().id

        // Admin approves deposit
        val approveResult = adminRepository.updateTransactionStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            transactionId = txId,
            newStatus = "COMPLETED",
            reason = "Payment received and verified on blockchain"
        )
        assertTrue("Admin approval must succeed", approveResult.isSuccess)

        // Check transaction status
        val updatedTx = database.transactionDao().getTransactionById(txId)
        assertEquals("COMPLETED", updatedTx?.status)

        // Check balance - must increase exactly by 1,000 USDT
        val balance = dashboardRepository.getAvailableBalance(testInvestor.id, "USDT")
        assertEquals(1000.0, balance, 0.001)

        // Notification created
        val notifs = database.notificationDao().observeNotificationsForUser(testInvestor.id).first()
        assertTrue("Investor must receive deposit approval notification", notifs.any { it.title == "Deposit Approved" })

        // Audit log created
        val auditLogs = database.auditLogDao().getRecentAuditLogs(50)
        val approveLog = auditLogs.find { it.action == "ADMIN_APPROVE_DEPOSIT" && it.targetId == txId }
        assertNotNull("Audit log must record ADMIN_APPROVE_DEPOSIT", approveLog)
        assertEquals(testAdmin.id, approveLog?.adminId)
    }

    @Test
    fun testE_duplicateApproval_isBlockedAndBalanceDoesNotDouble() = runBlocking {
        val depositResult = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validUsdtAccount.id,
            amount = 750.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TRX-DUP-TEST"
        )
        val txId = depositResult.getOrThrow().id

        // First approval
        val firstApprove = adminRepository.updateTransactionStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            transactionId = txId,
            newStatus = "COMPLETED",
            reason = "Verified"
        )
        assertTrue(firstApprove.isSuccess)
        assertEquals(750.0, dashboardRepository.getAvailableBalance(testInvestor.id, "USDT"), 0.001)

        // Second approval attempt MUST fail
        val secondApprove = adminRepository.updateTransactionStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            transactionId = txId,
            newStatus = "COMPLETED",
            reason = "Duplicate attempt"
        )
        assertTrue("Second approval must return failure", secondApprove.isFailure)
        assertTrue(secondApprove.exceptionOrNull()?.message?.contains("Double approval is strictly prohibited") == true)

        // Balance must remain exactly 750.0 (NOT 1500.0)
        val balanceAfterDuplicate = dashboardRepository.getAvailableBalance(testInvestor.id, "USDT")
        assertEquals("Balance must NOT double on duplicate approval attempt", 750.0, balanceAfterDuplicate, 0.001)
    }

    @Test
    fun testF_investorTransactionHistoryAndNotifications() = runBlocking {
        // Submit 1 PHP deposit and 1 USDT deposit
        val phpDep = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validPhpAccount.id,
            amount = 15000.0,
            currency = "PHP",
            paymentMethod = "GCash",
            referenceNo = "GCASH-998811"
        )
        assertTrue(phpDep.isSuccess)

        val usdtDep = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = validUsdtAccount.id,
            amount = 300.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TRX-HASH-332211"
        )
        assertTrue(usdtDep.isSuccess)

        val userTxs = database.transactionDao().getTransactionsByUserId(testInvestor.id)
        assertEquals(2, userTxs.size)

        // Approve PHP deposit, reject USDT deposit
        adminRepository.updateTransactionStatus(testAdmin.id, testAdmin.email, phpDep.getOrThrow().id, "COMPLETED", "Approved")
        adminRepository.updateTransactionStatus(testAdmin.id, testAdmin.email, usdtDep.getOrThrow().id, "REJECTED", "Rejected")

        // Check distinct balances
        val phpBalance = dashboardRepository.getAvailableBalance(testInvestor.id, "PHP")
        val usdtBalance = dashboardRepository.getAvailableBalance(testInvestor.id, "USDT")
        assertEquals(15000.0, phpBalance, 0.001)
        assertEquals(0.0, usdtBalance, 0.001)

        // Verify history distinctions
        val updatedHistory = database.transactionDao().getTransactionsByUserId(testInvestor.id)
        val approvedTx = updatedHistory.find { it.id == phpDep.getOrThrow().id }
        val rejectedTx = updatedHistory.find { it.id == usdtDep.getOrThrow().id }
        assertEquals("COMPLETED", approvedTx?.status)
        assertEquals("REJECTED", rejectedTx?.status)

        // Verify all notifications are present
        val notifs = database.notificationDao().observeNotificationsForUser(testInvestor.id).first()
        assertTrue(notifs.any { it.relatedId == phpDep.getOrThrow().id && it.title == "Deposit Approved" })
        assertTrue(notifs.any { it.relatedId == usdtDep.getOrThrow().id && it.title == "Deposit Rejected" })
    }
}
