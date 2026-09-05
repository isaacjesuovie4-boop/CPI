package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(private val userDao: UserDao) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        country: String,
        occupation: String,
        selectedCurrency: String,
        password: String,
        confirmPassword: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val trimmedName = fullName.trim()
            val trimmedEmail = email.trim().lowercase()
            val trimmedPhone = phoneNumber.trim()
            val trimmedCountry = country.trim()
            val trimmedOccupation = occupation.trim()
            val currency = selectedCurrency.trim().uppercase()

            // Validation
            if (trimmedName.length < 2) {
                return@withContext AuthResult.Error("Please enter your full name.")
            }
            if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                return@withContext AuthResult.Error("Please enter a valid email address.")
            }
            if (trimmedPhone.length < 6) {
                return@withContext AuthResult.Error("Please enter your phone number.")
            }
            if (trimmedCountry.isEmpty()) {
                return@withContext AuthResult.Error("Please select your country.")
            }
            if (trimmedOccupation.isEmpty()) {
                return@withContext AuthResult.Error("Please enter your occupation.")
            }
            if (currency != "USDT" && currency != "PHP") {
                return@withContext AuthResult.Error("Please select a supported currency (USDT or PHP).")
            }
            if (password.length < 6) {
                return@withContext AuthResult.Error("Password must be at least 6 characters.")
            }
            if (password != confirmPassword) {
                return@withContext AuthResult.Error("Passwords do not match.")
            }

            // Check if user already exists
            val existing = userDao.getUserByEmail(trimmedEmail)
            if (existing != null) {
                return@withContext AuthResult.Error("An account with this email address already exists. Please login instead.")
            }

            // Generate salt & secure hash
            val salt = generateSalt()
            val hash = hashPassword(password, salt)
            val newUserId = "CPI-" + UUID.randomUUID().toString().take(8).uppercase()

            val newUser = UserEntity(
                id = newUserId,
                fullName = trimmedName,
                email = trimmedEmail,
                phoneNumber = trimmedPhone,
                country = trimmedCountry,
                occupation = trimmedOccupation,
                selectedCurrency = currency,
                passwordHash = hash,
                passwordSalt = salt,
                role = "INVESTOR",
                createdAt = System.currentTimeMillis(),
                accountStatus = "ACTIVE"
            )

            userDao.insertUser(newUser)
            _currentUser.value = newUser
            AuthResult.Success(newUser)
        } catch (e: Exception) {
            AuthResult.Error("Authentication service is unavailable.")
        }
    }

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail.isEmpty() || password.isEmpty()) {
                return@withContext AuthResult.Error("Please enter your email and password.")
            }

            val user = userDao.getUserByEmail(trimmedEmail)
                ?: return@withContext AuthResult.Error("No account exists with this email.")

            val computedHash = hashPassword(password, user.passwordSalt)
            if (computedHash != user.passwordHash) {
                return@withContext AuthResult.Error("Invalid email or password.")
            }

            _currentUser.value = user
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error("Authentication service is unavailable.")
        }
    }

    suspend fun loginAdmin(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail.isEmpty() || password.isEmpty()) {
                return@withContext AuthResult.Error("Please enter your email and password.")
            }

            val user = userDao.getUserByEmail(trimmedEmail)
                ?: return@withContext AuthResult.Error("No account exists with this email.")

            val computedHash = hashPassword(password, user.passwordSalt)
            if (computedHash != user.passwordHash) {
                return@withContext AuthResult.Error("Invalid email or password.")
            }

            if (user.role != "ADMIN") {
                return@withContext AuthResult.Error("Access denied: This portal is restricted to authorized CPI administrators only.")
            }

            _currentUser.value = user
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error("Authentication service is unavailable.")
        }
    }

    suspend fun ensureAdminAccountExists(): UserEntity = withContext(Dispatchers.IO) {
        val adminCount = userDao.getAdminCount()
        val defaultAdminEmail = "admin@crestpoint.com"
        val existingAdmin = userDao.getUserByEmail(defaultAdminEmail)
        if (existingAdmin != null && existingAdmin.role == "ADMIN") {
            return@withContext existingAdmin
        }

        if (adminCount == 0 || existingAdmin == null) {
            val salt = generateSalt()
            val hash = hashPassword("AdminCPI2026!", salt)
            val adminUser = UserEntity(
                id = "CPI-ADM-001",
                fullName = "CPI Principal Administrator",
                email = defaultAdminEmail,
                phoneNumber = "+1-800-CPI-MGMT",
                country = "United States",
                occupation = "Chief Platform Officer",
                selectedCurrency = "USDT",
                passwordHash = hash,
                passwordSalt = salt,
                role = "ADMIN",
                createdAt = System.currentTimeMillis(),
                accountStatus = "ACTIVE"
            )
            userDao.insertUser(adminUser)
            adminUser
        } else {
            existingAdmin
        }
    }

    suspend fun resetPassword(email: String, newPass: String, confirmPass: String): AuthResult = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) {
            return@withContext AuthResult.Error("Please enter your registered email.")
        }
        if (newPass.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }
        if (newPass != confirmPass) {
            return@withContext AuthResult.Error("Passwords do not match.")
        }

        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return@withContext AuthResult.Error("No account found with this email address.")

        val newSalt = generateSalt()
        val newHash = hashPassword(newPass, newSalt)
        val updatedUser = user.copy(passwordHash = newHash, passwordSalt = newSalt)
        userDao.updateUser(updatedUser)

        if (_currentUser.value?.id == user.id) {
            _currentUser.value = updatedUser
        }
        AuthResult.Success(updatedUser)
    }

    suspend fun updateProfile(
        userId: String,
        fullName: String,
        phoneNumber: String,
        country: String,
        occupation: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val trimmedName = fullName.trim()
        val trimmedPhone = phoneNumber.trim()
        val trimmedCountry = country.trim()
        val trimmedOccupation = occupation.trim()

        if (trimmedName.length < 2) {
            return@withContext AuthResult.Error("Please enter a valid full name.")
        }
        if (trimmedPhone.length < 6) {
            return@withContext AuthResult.Error("Please enter a valid phone number.")
        }
        if (trimmedCountry.isEmpty()) {
            return@withContext AuthResult.Error("Please enter your country.")
        }
        if (trimmedOccupation.isEmpty()) {
            return@withContext AuthResult.Error("Please enter your occupation.")
        }

        val user = userDao.getUserById(userId)
            ?: return@withContext AuthResult.Error("User account not found.")

        val updatedUser = user.copy(
            fullName = trimmedName,
            phoneNumber = trimmedPhone,
            country = trimmedCountry,
            occupation = trimmedOccupation
        )
        userDao.updateUser(updatedUser)

        if (_currentUser.value?.id == user.id) {
            _currentUser.value = updatedUser
        }
        AuthResult.Success(updatedUser)
    }

    fun logout() {
        _currentUser.value = null
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val combined = "$salt:$password:cpi_secure_key"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
