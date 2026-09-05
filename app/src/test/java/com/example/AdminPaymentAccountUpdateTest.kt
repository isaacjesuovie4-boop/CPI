package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
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
class AdminPaymentAccountUpdateTest {

    private lateinit var database: AppDatabase
    private lateinit var adminRepository: AdminRepository
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var testAdmin: UserEntity

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

        testAdmin = UserEntity(
            id = "admin-cpi-001",
            fullName = "CPI Global Administrator",
            email = "admin@cpi.com",
            phoneNumber = "+1234567890",
            country = "Philippines",
            occupation = "Compliance Officer",
            selectedCurrency = "USDT",
            passwordHash = "adminpass",
            passwordSalt = "salt123",
            role = "ADMIN",
            accountStatus = "ACTIVE"
        )
        database.userDao().insertUser(testAdmin)

        adminRepository.ensureDefaultPaymentAccountsExist()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testEditExistingPaymentAccount_updatesDatabaseWithoutDuplicates() = runBlocking {
        val allAccountsBefore = database.paymentAccountDao().observeAllPaymentAccounts().first()
        val initialCount = allAccountsBefore.size
        val usdtTrc20Account = allAccountsBefore.first { it.currency == "USDT" && it.network == "TRC20" }
        val originalId = usdtTrc20Account.id

        // Admin updates payment details with their real wallet
        val myRealWallet = "TRealAdminUSDTWalletAddress2026XYZ99"
        val myCustomInstructions = "Please send exact USDT amount via TRC20 and provide transaction hash immediately."

        val updateResult = adminRepository.updatePaymentAccount(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            id = originalId,
            paymentMethod = "USDT TRC20 Gateway",
            network = "TRC20",
            accountName = null,
            accountNumber = null,
            walletAddress = myRealWallet,
            instructions = myCustomInstructions
        )

        assertTrue("Update should succeed", updateResult.isSuccess)

        // Verify no duplicate accounts were created
        val allAccountsAfter = database.paymentAccountDao().observeAllPaymentAccounts().first()
        assertEquals("Account count must remain identical (no duplicates)", initialCount, allAccountsAfter.size)

        // Verify account contains the new details
        val updatedAccount = database.paymentAccountDao().getPaymentAccountById(originalId)
        assertNotNull(updatedAccount)
        assertEquals(myRealWallet, updatedAccount?.walletAddress)
        assertEquals(myCustomInstructions, updatedAccount?.instructions)
        assertEquals("USDT TRC20 Gateway", updatedAccount?.paymentMethod)

        // Verify Audit Log was recorded
        val auditLogs = database.auditLogDao().getRecentAuditLogs(50)
        val updateLog = auditLogs.find { it.action == "PAYMENT_ACCOUNT_UPDATED" && it.targetId == originalId }
        assertNotNull("Audit log must record PAYMENT_ACCOUNT_UPDATED", updateLog)
        assertEquals(testAdmin.id, updateLog?.adminId)
        assertTrue("Audit log should mention changed fields", updateLog?.valueChange?.contains("walletAddress") == true)
    }

    @Test
    fun testEditPhpPaymentAccount_andVerify30MinExpirationFlow() = runBlocking {
        val allAccounts = database.paymentAccountDao().observeAllPaymentAccounts().first()
        val gcashAccount = allAccounts.first { it.currency == "PHP" && it.paymentMethod.contains("GCash", ignoreCase = true) }
        val accountId = gcashAccount.id

        // Admin edits PHP details
        val myName = "Juan Dela Cruz"
        val myNumber = "0999-123-4567"
        val myInstructions = "Transfer via GCash to Juan Dela Cruz. Keep screenshot reference."

        val updateResult = adminRepository.updatePaymentAccount(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            id = accountId,
            paymentMethod = "GCash Direct",
            network = null,
            accountName = myName,
            accountNumber = myNumber,
            walletAddress = null,
            instructions = myInstructions
        )
        assertTrue(updateResult.isSuccess)

        // Admin publishes for 30 minutes
        val publishResult = adminRepository.publishPaymentAccount(
            adminId = testAdmin.id,
            adminEmail = testAdmin.email,
            id = accountId,
            durationMinutes = 30
        )
        assertTrue(publishResult.isSuccess)

        val publishedAccount = database.paymentAccountDao().getPaymentAccountById(accountId)
        assertNotNull(publishedAccount)
        assertTrue("Account must be published", publishedAccount?.isPublished == true)
        assertNotNull(publishedAccount?.publishedAt)
        assertNotNull(publishedAccount?.expiresAt)

        val now = System.currentTimeMillis()
        // Within 30 minutes: valid
        assertTrue("Should be valid currently", publishedAccount!!.isCurrentlyValid(now))
        assertEquals(myName, publishedAccount.accountName)
        assertEquals(myNumber, publishedAccount.accountNumber)

        // After 31 minutes: expired
        val after31Minutes = publishedAccount.expiresAt!! + (60 * 1000L)
        assertFalse("Should be expired after 30 minute window", publishedAccount.isCurrentlyValid(after31Minutes))
        assertTrue("isExpired should return true after window", publishedAccount.isExpired(after31Minutes))

        // Check investor-facing published accounts flow
        val investorPublishedAccounts = dashboardRepository.observePublishedPaymentAccounts("PHP").first()
        val matchingAccount = investorPublishedAccounts.find { it.id == accountId }
        assertNotNull("Investor must receive the published account from database", matchingAccount)
        assertEquals(myName, matchingAccount?.accountName)
        assertEquals(myNumber, matchingAccount?.accountNumber)
    }
}
