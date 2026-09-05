package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.InvestmentEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PaymentAccountEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.WithdrawalEntity
import com.example.data.repository.DashboardOverview
import com.example.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface QuickActionDialogType {
    data object None : QuickActionDialogType
    data class ActionNotice(val title: String, val message: String, val actionName: String) : QuickActionDialogType
    data object Deposit : QuickActionDialogType
}

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _selectedCurrency = MutableStateFlow("USDT")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _dialogState = MutableStateFlow<QuickActionDialogType>(QuickActionDialogType.None)
    val dialogState: StateFlow<QuickActionDialogType> = _dialogState.asStateFlow()

    private val _selectedTransactionFilter = MutableStateFlow("ALL")
    val selectedTransactionFilter: StateFlow<String> = _selectedTransactionFilter.asStateFlow()

    val overview: StateFlow<DashboardOverview> = _currentUserId.flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(DashboardOverview())
        } else {
            dashboardRepository.observeOverview(userId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardOverview()
    )

    val investments: StateFlow<List<InvestmentEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            dashboardRepository.observeInvestments(userId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<TransactionEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            dashboardRepository.observeTransactions(userId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val withdrawals: StateFlow<List<WithdrawalEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            dashboardRepository.observeWithdrawals(userId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            dashboardRepository.observeNotifications(userId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationCount: StateFlow<Int> = _currentUserId.flatMapLatest { userId ->
        if (userId.isNullOrBlank()) {
            flowOf(0)
        } else {
            dashboardRepository.observeUnreadNotificationCount(userId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val publishedPaymentAccounts: StateFlow<List<PaymentAccountEntity>> = _selectedCurrency.flatMapLatest { curr ->
        dashboardRepository.observePublishedPaymentAccounts(curr)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun markNotificationAsRead(notificationId: String) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            dashboardRepository.markNotificationAsRead(uid, notificationId)
        }
    }

    fun markAllNotificationsAsRead() {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            dashboardRepository.markAllNotificationsAsRead(uid)
        }
    }

    fun setUserId(userId: String?) {
        _currentUserId.value = userId
    }

    fun setSelectedCurrency(currency: String) {
        _selectedCurrency.value = currency.uppercase().trim()
    }

    fun setTransactionFilter(filter: String) {
        _selectedTransactionFilter.value = filter
    }

    fun showActionNotice(actionName: String, title: String, message: String) {
        _dialogState.value = QuickActionDialogType.ActionNotice(
            title = title,
            message = message,
            actionName = actionName
        )
    }

    fun openDepositDialog() {
        _dialogState.value = QuickActionDialogType.Deposit
    }

    fun dismissDialog() {
        _dialogState.value = QuickActionDialogType.None
    }

    // Investment Submission
    private val _isSubmittingInvestment = MutableStateFlow(false)
    val isSubmittingInvestment: StateFlow<Boolean> = _isSubmittingInvestment.asStateFlow()

    private val _investmentErrorMessage = MutableStateFlow<String?>(null)
    val investmentErrorMessage: StateFlow<String?> = _investmentErrorMessage.asStateFlow()

    fun clearInvestmentError() {
        _investmentErrorMessage.value = null
    }

    fun submitInvestment(
        userId: String,
        amount: Double,
        currency: String,
        durationHours: Int,
        network: String,
        onSuccess: (InvestmentEntity) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingInvestment.value = true
            _investmentErrorMessage.value = null
            val result = dashboardRepository.createInvestment(
                userId = userId,
                amount = amount,
                currency = currency,
                durationHours = durationHours,
                network = network
            )
            _isSubmittingInvestment.value = false
            if (result.isSuccess) {
                val investment = result.getOrThrow()
                onSuccess(investment)
            } else {
                _investmentErrorMessage.value = result.exceptionOrNull()?.message ?: "Failed to create investment."
            }
        }
    }

    // Deposit Submission
    private val _isSubmittingDeposit = MutableStateFlow(false)
    val isSubmittingDeposit: StateFlow<Boolean> = _isSubmittingDeposit.asStateFlow()

    private val _depositErrorMessage = MutableStateFlow<String?>(null)
    val depositErrorMessage: StateFlow<String?> = _depositErrorMessage.asStateFlow()

    private val _depositSuccessTransaction = MutableStateFlow<TransactionEntity?>(null)
    val depositSuccessTransaction: StateFlow<TransactionEntity?> = _depositSuccessTransaction.asStateFlow()

    fun clearDepositStatus() {
        _depositErrorMessage.value = null
        _depositSuccessTransaction.value = null
    }

    fun submitDeposit(
        userId: String,
        paymentAccountId: String,
        amount: Double,
        currency: String,
        paymentMethod: String,
        referenceNo: String,
        notes: String = "",
        onSuccess: (TransactionEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingDeposit.value = true
            _depositErrorMessage.value = null
            _depositSuccessTransaction.value = null

            val result = dashboardRepository.submitDeposit(
                userId = userId,
                paymentAccountId = paymentAccountId,
                amount = amount,
                currency = currency,
                paymentMethod = paymentMethod,
                referenceNo = referenceNo,
                notes = notes
            )
            _isSubmittingDeposit.value = false
            if (result.isSuccess) {
                val tx = result.getOrThrow()
                _depositSuccessTransaction.value = tx
                onSuccess(tx)
            } else {
                _depositErrorMessage.value = result.exceptionOrNull()?.message ?: "Deposit submission failed."
            }
        }
    }

    // Withdrawal State & Operations
    private val _isSubmittingWithdrawal = MutableStateFlow(false)
    val isSubmittingWithdrawal: StateFlow<Boolean> = _isSubmittingWithdrawal.asStateFlow()

    private val _withdrawalErrorMessage = MutableStateFlow<String?>(null)
    val withdrawalErrorMessage: StateFlow<String?> = _withdrawalErrorMessage.asStateFlow()

    private val _withdrawalSuccess = MutableStateFlow<WithdrawalEntity?>(null)
    val withdrawalSuccess: StateFlow<WithdrawalEntity?> = _withdrawalSuccess.asStateFlow()

    private val _otpExpiresAt = MutableStateFlow<Long?>(null)
    val otpExpiresAt: StateFlow<Long?> = _otpExpiresAt.asStateFlow()

    private val _isOtpRequested = MutableStateFlow(false)
    val isOtpRequested: StateFlow<Boolean> = _isOtpRequested.asStateFlow()

    fun clearWithdrawalStatus() {
        _withdrawalErrorMessage.value = null
        _withdrawalSuccess.value = null
        _isOtpRequested.value = false
        _otpExpiresAt.value = null
    }

    fun requestWithdrawalOtp(userId: String) {
        viewModelScope.launch {
            val res = dashboardRepository.generateWithdrawalOtp(userId)
            if (res.isSuccess) {
                _otpExpiresAt.value = res.getOrThrow()
                _isOtpRequested.value = true
                _withdrawalErrorMessage.value = null
            } else {
                _withdrawalErrorMessage.value = "Failed to generate security verification code: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun submitWithdrawalRequest(
        userId: String,
        amount: Double,
        currency: String,
        destination: String,
        network: String,
        otpCode: String,
        onSuccess: (WithdrawalEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingWithdrawal.value = true
            _withdrawalErrorMessage.value = null
            _withdrawalSuccess.value = null

            // Validate OTP server-side
            val isOtpValid = dashboardRepository.verifyWithdrawalOtp(userId, otpCode)
            if (!isOtpValid) {
                _isSubmittingWithdrawal.value = false
                _withdrawalErrorMessage.value = "Invalid or expired 6-digit withdrawal verification code. Please request a new security code."
                return@launch
            }

            val result = dashboardRepository.submitWithdrawal(
                userId = userId,
                amount = amount,
                currency = currency,
                destination = destination,
                network = network,
                isSecurityVerified = true
            )
            _isSubmittingWithdrawal.value = false
            if (result.isSuccess) {
                val wth = result.getOrThrow()
                _withdrawalSuccess.value = wth
                _isOtpRequested.value = false
                _otpExpiresAt.value = null
                onSuccess(wth)
            } else {
                _withdrawalErrorMessage.value = result.exceptionOrNull()?.message ?: "Withdrawal submission failed."
            }
        }
    }

    fun fetchAvailableBalance(userId: String, currency: String, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            val bal = dashboardRepository.getAvailableBalance(userId, currency)
            onResult(bal)
        }
    }
}

class DashboardViewModelFactory(
    private val dashboardRepository: DashboardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(dashboardRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
