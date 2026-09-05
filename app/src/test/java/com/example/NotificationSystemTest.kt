package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.PaymentAccountEntity
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
class NotificationSystemTest {

    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var adminRepository: AdminRepository

    private lateinit var testInvestor: UserEntity
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

        testInvestor = UserEntity(
            id = "USR_TEST_01",
            fullName = "Isaac Investor",
            email = "isaac@cpi.com",
            phoneNumber = "+1234567890",
            country = "United States",
            occupation = "Investor",
            selectedCurrency = "USDT",
            passwordHash = "hash123",
            passwordSalt = "salt123",
            role = "INVESTOR"
        )
        testAdmin = UserEntity(
            id = "USR_ADMIN_01",
            fullName = "Admin Officer",
            email = "admin@cpi.com",
            phoneNumber = "+1987654321",
            country = "Singapore",
            occupation = "Compliance",
            selectedCurrency = "USDT",
            passwordHash = "adminHash123",
            passwordSalt = "saltAdmin123",
            role = "ADMIN"
        )

        database.userDao().insertUser(testInvestor)
        database.userDao().insertUser(testAdmin)

        // Seed published payment account
        val now = System.currentTimeMillis()
        database.paymentAccountDao().insertPaymentAccount(
            PaymentAccountEntity(
                id = "PA_USDT_01",
                currency = "USDT",
                paymentMethod = "USDT TRC20",
                network = "TRC20",
                walletAddress = "TXaZ7Q9xKpLm4N2wR8vY6sJ1dF3gH5bC8e",
                instructions = "Send exact amount",
                isActive = true,
                isPublished = true,
                publishedAt = now,
                expiresAt = now + 3600000L,
                createdAt = now,
                updatedAt = now,
                lastPublishedBy = testAdmin.email
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `1 & 2 - Deposit submission creates investor and admin notifications`() = runBlocking {
        val result = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = "PA_USDT_01",
            amount = 1000.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TXN_HASH_12345"
        )
        assertTrue("Deposit should succeed", result.isSuccess)

        // Check investor notification
        val userNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        assertEquals(1, userNotifs.size)
        assertEquals("Deposit Submitted", userNotifs[0].title)
        assertEquals("Your deposit request has been submitted and is awaiting review.", userNotifs[0].message)
        assertEquals("DEPOSIT", userNotifs[0].type)
        assertFalse(userNotifs[0].isRead)

        // Check admin notification
        val adminNotifs = adminRepository.adminNotificationsFlow.first()
        assertEquals(1, adminNotifs.size)
        assertEquals("New Deposit Awaiting Review", adminNotifs[0].title)
        assertEquals("ADMIN", adminNotifs[0].recipientRole)
    }

    @Test
    fun `3, 4 & 5 - Admin approves deposit creates approval notification and updates balance`() = runBlocking {
        val depResult = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = "PA_USDT_01",
            amount = 1500.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TXN_HASH_ABC"
        )
        val depositTxn = depResult.getOrThrow()

        // Approve deposit
        val approveResult = adminRepository.updateTransactionStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            transactionId = depositTxn.id,
            newStatus = "COMPLETED",
            reason = "Proof verified against TRON block explorer"
        )
        assertTrue("Approval should succeed", approveResult.isSuccess)

        // Verify available balance calculated properly
        val updatedBalance = dashboardRepository.getAvailableBalance(testInvestor.id, "USDT")
        assertEquals(1500.0, updatedBalance, 0.001)

        // Verify investor received approval notification
        val userNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        val approvalNotif = userNotifs.firstOrNull { it.title == "Deposit Approved" }
        assertNotNull("Should have approval notification", approvalNotif)
        assertEquals(
            "Your deposit has been approved and the approved amount has been credited to your available balance.",
            approvalNotif!!.message
        )
    }

    @Test
    fun `Admin rejects deposit creates rejection notification`() = runBlocking {
        val depResult = dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = "PA_USDT_01",
            amount = 2000.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "INVALID_HASH"
        )
        val depositTxn = depResult.getOrThrow()

        val rejectResult = adminRepository.updateTransactionStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            transactionId = depositTxn.id,
            newStatus = "REJECTED",
            reason = "Transaction reference hash not found on blockchain."
        )
        assertTrue(rejectResult.isSuccess)

        val userNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        val rejectionNotif = userNotifs.firstOrNull { it.title == "Deposit Rejected" }
        assertNotNull(rejectionNotif)
        assertEquals(
            "Your deposit was rejected. Please review the deposit details or contact support.",
            rejectionNotif!!.message
        )
    }

    @Test
    fun `Investment creation and update creates notifications`() = runBlocking {
        // First deposit and approve capital so user has balance
        database.transactionDao().insertTransaction(
            TransactionEntity(
                id = "TX_DEP_TEST",
                userId = testInvestor.id,
                type = "DEPOSIT",
                amount = 2000.0,
                currency = "USDT",
                status = "COMPLETED",
                reference = "REF123",
                paymentMethod = "TRC20"
            )
        )

        val investResult = dashboardRepository.createInvestment(
            userId = testInvestor.id,
            amount = 1000.0,
            currency = "USDT",
            durationHours = 24
        )
        assertTrue("Investment creation should succeed", investResult.isSuccess)

        val userNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        val creationNotif = userNotifs.firstOrNull { it.title == "Investment Created" }
        assertNotNull(creationNotif)
        assertEquals("Your investment has been successfully created.", creationNotif!!.message)
    }

    @Test
    fun `6 to 9 - Withdrawal lifecycle creates accurate step-by-step notifications`() = runBlocking {
        // Seed initial balance
        database.transactionDao().insertTransaction(
            TransactionEntity(
                id = "TX_DEP_INITIAL",
                userId = testInvestor.id,
                type = "DEPOSIT",
                amount = 5000.0,
                currency = "USDT",
                status = "COMPLETED",
                reference = "REF_INIT",
                paymentMethod = "TRC20"
            )
        )

        // 1. Submit withdrawal
        val withdrawResult = dashboardRepository.submitWithdrawal(
            userId = testInvestor.id,
            amount = 500.0,
            currency = "USDT",
            destination = "TXYZ_DESTINATION_ADDRESS",
            network = "USDT (TRC20)",
            isSecurityVerified = true
        )
        assertTrue(withdrawResult.isSuccess)
        val withdrawal = withdrawResult.getOrThrow()

        var userNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        val submitNotif = userNotifs.firstOrNull { it.title == "Withdrawal Submitted" }
        assertNotNull(submitNotif)
        assertEquals("Your withdrawal request has been submitted and is awaiting review.", submitNotif!!.message)

        // 2. Processing
        val procResult = adminRepository.updateWithdrawalStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            withdrawalId = withdrawal.withdrawalId,
            newStatus = "PROCESSING",
            reason = "Initiating batch transfer"
        )
        assertTrue(procResult.isSuccess)

        userNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        val procNotif = userNotifs.firstOrNull { it.title == "Withdrawal Processing" }
        assertNotNull(procNotif)
        assertEquals("Your withdrawal is being processed.", procNotif!!.message)

        // 3. Completed
        val compResult = adminRepository.updateWithdrawalStatus(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            withdrawalId = withdrawal.withdrawalId,
            newStatus = "COMPLETED",
            reason = "Blockchain tx confirmed: 0x999888"
        )
        assertTrue(compResult.isSuccess)

        userNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        val compNotif = userNotifs.firstOrNull { it.title == "Withdrawal Completed" }
        assertNotNull(compNotif)
        assertEquals("Your withdrawal has been completed.", compNotif!!.message)
    }

    @Test
    fun `10 & 11 - Read and unread status and count management`() = runBlocking {
        // Generate notifications
        dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = "PA_USDT_01",
            amount = 500.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TXN_01"
        )
        dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = "PA_USDT_01",
            amount = 600.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "TXN_02"
        )

        var unreadCount = dashboardRepository.observeUnreadNotificationCount(testInvestor.id).first()
        assertEquals(2, unreadCount)

        val notifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        assertEquals(2, notifs.size)

        // Mark single as read
        dashboardRepository.markNotificationAsRead(testInvestor.id, notifs[0].notificationId)
        unreadCount = dashboardRepository.observeUnreadNotificationCount(testInvestor.id).first()
        assertEquals(1, unreadCount)

        // Mark all as read
        dashboardRepository.markAllNotificationsAsRead(testInvestor.id)
        unreadCount = dashboardRepository.observeUnreadNotificationCount(testInvestor.id).first()
        assertEquals(0, unreadCount)
    }

    @Test
    fun `12 - Investor and admin notifications isolation`() = runBlocking {
        dashboardRepository.submitDeposit(
            userId = testInvestor.id,
            paymentAccountId = "PA_USDT_01",
            amount = 100.0,
            currency = "USDT",
            paymentMethod = "USDT TRC20",
            referenceNo = "ISOLATION_TEST"
        )

        val investorNotifs = dashboardRepository.observeNotifications(testInvestor.id).first()
        val adminNotifs = adminRepository.adminNotificationsFlow.first()

        assertTrue("Investor only sees investor role notifs", investorNotifs.all { it.recipientRole == "INVESTOR" && it.userId == testInvestor.id })
        assertTrue("Admin only sees admin role notifs", adminNotifs.all { it.recipientRole == "ADMIN" })
    }
}
