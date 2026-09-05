package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Idle : UiState
    data object Loading : UiState
    data class Success(val message: String? = null) : UiState
    data class Error(val message: String) : UiState
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepository.currentUser

    private val _registerState = MutableStateFlow<UiState>(UiState.Idle)
    val registerState: StateFlow<UiState> = _registerState.asStateFlow()

    private val _loginState = MutableStateFlow<UiState>(UiState.Idle)
    val loginState: StateFlow<UiState> = _loginState.asStateFlow()

    private val _adminLoginState = MutableStateFlow<UiState>(UiState.Idle)
    val adminLoginState: StateFlow<UiState> = _adminLoginState.asStateFlow()

    private val _resetPassState = MutableStateFlow<UiState>(UiState.Idle)
    val resetPassState: StateFlow<UiState> = _resetPassState.asStateFlow()

    private val _profileUpdateState = MutableStateFlow<UiState>(UiState.Idle)
    val profileUpdateState: StateFlow<UiState> = _profileUpdateState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.ensureAdminAccountExists()
        }
    }

    fun resetStates() {
        _registerState.value = UiState.Idle
        _loginState.value = UiState.Idle
        _adminLoginState.value = UiState.Idle
        _resetPassState.value = UiState.Idle
        _profileUpdateState.value = UiState.Idle
    }

    fun resetAdminLoginState() {
        _adminLoginState.value = UiState.Idle
    }

    fun resetProfileUpdateState() {
        _profileUpdateState.value = UiState.Idle
    }

    fun updateProfile(
        userId: String,
        fullName: String,
        phoneNumber: String,
        country: String,
        occupation: String
    ) {
        viewModelScope.launch {
            _profileUpdateState.value = UiState.Loading
            when (val result = authRepository.updateProfile(
                userId = userId,
                fullName = fullName,
                phoneNumber = phoneNumber,
                country = country,
                occupation = occupation
            )) {
                is AuthResult.Success -> {
                    _profileUpdateState.value = UiState.Success("Profile updated successfully.")
                }
                is AuthResult.Error -> {
                    _profileUpdateState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        country: String,
        occupation: String,
        selectedCurrency: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.register(
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                country = country,
                occupation = occupation,
                selectedCurrency = selectedCurrency,
                password = password,
                confirmPassword = confirmPassword
            )) {
                is AuthResult.Success -> {
                    _registerState.value = UiState.Success("Registration successful! Welcome to Crest Point Investment.")
                }
                is AuthResult.Error -> {
                    _registerState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _loginState.value = UiState.Success("Welcome back, ${result.user.fullName}!")
                }
                is AuthResult.Error -> {
                    _loginState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun loginAdmin(email: String, password: String) {
        viewModelScope.launch {
            _adminLoginState.value = UiState.Loading
            when (val result = authRepository.loginAdmin(email, password)) {
                is AuthResult.Success -> {
                    _adminLoginState.value = UiState.Success("Admin authentication successful.")
                }
                is AuthResult.Error -> {
                    _adminLoginState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun resetPassword(email: String, newPass: String, confirmPass: String) {
        viewModelScope.launch {
            _resetPassState.value = UiState.Loading
            when (val result = authRepository.resetPassword(email, newPass, confirmPass)) {
                is AuthResult.Success -> {
                    _resetPassState.value = UiState.Success("Password updated successfully! You can now log in.")
                }
                is AuthResult.Error -> {
                    _resetPassState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        resetStates()
    }
}

class AuthViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
