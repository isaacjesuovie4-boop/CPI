package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AuditLogEntity
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var adminRepository: AdminRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        authRepository = AuthRepository(database.userDao())
        adminRepository = AdminRepository(
            userDao = database.userDao(),
            investmentDao = database.investmentDao(),
            transactionDao = database.transactionDao(),
            auditLogDao = database.auditLogDao(),
            paymentAccountDao = database.paymentAccountDao(),
            withdrawalDao = database.withdrawalDao(),
            notificationDao = database.notificationDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CPI", appName)
    }

    @Test
    fun `full auth flow register logout and login with same credentials`() = runBlocking {
        val email = "isaacjesuovie4@gmail.com"
        val password = "SecurePassword123!"

        // 1. Register
        val regResult = authRepository.register(
            fullName = "Isaac Jesuovie",
            email = email,
            phoneNumber = "+1234567890",
            country = "United States",
            occupation = "Investor",
            selectedCurrency = "USDT",
            password = password,
            confirmPassword = password
        )

        assertTrue("Registration should succeed", regResult is AuthResult.Success)
        val registeredUser = (regResult as AuthResult.Success).user
        assertEquals("Isaac Jesuovie", registeredUser.fullName)
        assertEquals(email, registeredUser.email)
        assertEquals("USDT", registeredUser.selectedCurrency)
        assertNotNull(authRepository.currentUser.value)

        // 2. Logout
        authRepository.logout()
        assertNull("Current user should be null after logout", authRepository.currentUser.value)

        // 3. Login with same email & password
        val loginResult = authRepository.login(email, password)
        assertTrue("Login with registered credentials should succeed", loginResult is AuthResult.Success)
        val loggedInUser = (loginResult as AuthResult.Success).user
        assertEquals(registeredUser.id, loggedInUser.id)
        assertEquals("Isaac Jesuovie", loggedInUser.fullName)
        assertEquals(email, loggedInUser.email)
        assertEquals(loggedInUser, authRepository.currentUser.value)
    }

    @Test
    fun `login with wrong password returns exact error message`() = runBlocking {
        val email = "investor@crestpoint.com"
        val password = "CorrectPassword123"

        authRepository.register(
            fullName = "Jane Doe",
            email = email,
            phoneNumber = "+1987654321",
            country = "United Kingdom",
            occupation = "Fund Manager",
            selectedCurrency = "PHP",
            password = password,
            confirmPassword = password
        )

        authRepository.logout()

        val failedResult = authRepository.login(email, "WrongPassword999")
        assertTrue(failedResult is AuthResult.Error)
        assertEquals("Invalid email or password.", (failedResult as AuthResult.Error).message)
    }

    @Test
    fun `login with non-existent email returns real reason`() = runBlocking {
        val failedResult = authRepository.login("nonexistent@domain.com", "anyPassword123")
        assertTrue(failedResult is AuthResult.Error)
        assertEquals("No account exists with this email.", (failedResult as AuthResult.Error).message)
    }

    @Test
    fun `login with empty fields returns please enter email and password`() = runBlocking {
        val emptyEmail = authRepository.login("", "pass123")
        assertTrue(emptyEmail is AuthResult.Error)
        assertEquals("Please enter your email and password.", (emptyEmail as AuthResult.Error).message)

        val emptyPass = authRepository.login("user@mail.com", "")
        assertTrue(emptyPass is AuthResult.Error)
        assertEquals("Please enter your email and password.", (emptyPass as AuthResult.Error).message)
    }

    @Test
    fun `login handles whitespace trimming and case insensitivity`() = runBlocking {
        val email = "case.test@cpi.com"
        val password = "MyPassWord2026!"

        authRepository.register(
            fullName = "Case Tester",
            email = email,
            phoneNumber = "+1122334455",
            country = "Canada",
            occupation = "Developer",
            selectedCurrency = "USDT",
            password = password,
            confirmPassword = password
        )

        authRepository.logout()

        // Attempt login with leading/trailing spaces and uppercase characters in email
        val loginResult = authRepository.login("  CASE.TEST@CPI.COM  ", password)
        assertTrue("Case insensitive & trimmed email login should succeed", loginResult is AuthResult.Success)
        assertEquals("Case Tester", (loginResult as AuthResult.Success).user.fullName)
    }

    @Test
    fun `registered user is assigned INVESTOR role and cannot log into admin portal`() = runBlocking {
        val email = "regular.investor@crestpoint.com"
        val password = "InvestorPass2026!"

        val regResult = authRepository.register(
            fullName = "Regular Investor",
            email = email,
            phoneNumber = "+1999888777",
            country = "United States",
            occupation = "Doctor",
            selectedCurrency = "USDT",
            password = password,
            confirmPassword = password
        )

        assertTrue(regResult is AuthResult.Success)
        val user = (regResult as AuthResult.Success).user
        assertEquals("INVESTOR", user.role)

        // Attempting to log into the Admin portal with an Investor role should be rejected
        val adminLoginResult = authRepository.loginAdmin(email, password)
        assertTrue("Investor attempting admin login must fail", adminLoginResult is AuthResult.Error)
        assertEquals(
            "Access denied: This portal is restricted to authorized CPI administrators only.",
            (adminLoginResult as AuthResult.Error).message
        )
    }

    @Test
    fun `admin account seeds automatically and loginAdmin succeeds with ADMIN role`() = runBlocking {
        // Ensure admin account is seeded
        authRepository.ensureAdminAccountExists()

        val adminResult = authRepository.loginAdmin("admin@crestpoint.com", "AdminCPI2026!")
        assertTrue("Admin login with seeded credentials should succeed", adminResult is AuthResult.Success)

        val adminUser = (adminResult as AuthResult.Success).user
        assertEquals("ADMIN", adminUser.role)
        assertEquals("admin@crestpoint.com", adminUser.email)
        assertEquals("CPI Principal Administrator", adminUser.fullName)
        assertEquals(adminUser, authRepository.currentUser.value)
    }

    @Test
    fun `admin login with wrong password returns invalid credentials error`() = runBlocking {
        authRepository.ensureAdminAccountExists()

        val failedResult = authRepository.loginAdmin("admin@crestpoint.com", "WrongAdminPass!")
        assertTrue(failedResult is AuthResult.Error)
        assertEquals("Invalid email or password.", (failedResult as AuthResult.Error).message)
    }

    @Test
    fun `passwords are stored as salted SHA256 hashes and not in plaintext`() = runBlocking {
        val plainPassword = "SecretPlainPassword123!"
        val regResult = authRepository.register(
            fullName = "Hash Verification",
            email = "hash.check@crestpoint.com",
            phoneNumber = "+1234567890",
            country = "Germany",
            occupation = "Auditor",
            selectedCurrency = "USDT",
            password = plainPassword,
            confirmPassword = plainPassword
        )
        assertTrue(regResult is AuthResult.Success)

        val userInDb = database.userDao().getUserByEmail("hash.check@crestpoint.com")
        assertNotNull(userInDb)
        assertNotEquals(plainPassword, userInDb!!.passwordHash)
        assertTrue("Password hash must be 64-char hex SHA-256", userInDb.passwordHash.length == 64)
        assertNotNull(userInDb.passwordSalt)
        assertTrue("Salt must not be empty", userInDb.passwordSalt.isNotEmpty())
    }

    @Test
    fun `audit log recording and retrieval functions correctly`() = runBlocking {
        authRepository.ensureAdminAccountExists()
        val admin = database.userDao().getUserByEmail("admin@crestpoint.com")!!

        // Log an administrative action
        adminRepository.logAdminAction(
            adminId = admin.id,
            adminEmail = admin.email,
            action = "TRANSACTION_STATUS_UPDATE",
            targetId = "tx-12345",
            targetType = "TRANSACTION",
            valueChange = "PENDING -> APPROVED",
            reason = "Wire funds verified against banking records."
        )

        val logs: List<AuditLogEntity> = database.auditLogDao().getRecentAuditLogs()
        assertEquals(1, logs.size)
        val log = logs[0]
        assertEquals(admin.id, log.adminId)
        assertEquals(admin.email, log.adminEmail)
        assertEquals("TRANSACTION_STATUS_UPDATE", log.action)
        assertEquals("tx-12345", log.targetId)
        assertEquals("TRANSACTION", log.targetType)
        assertEquals("PENDING -> APPROVED", log.valueChange)
        assertTrue(log.reason.contains("Wire funds verified"))
    }
}

