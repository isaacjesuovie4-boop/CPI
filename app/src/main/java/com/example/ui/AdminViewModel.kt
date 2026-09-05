package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuditLogEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PaymentAccountEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import com.example.data.repository.AdminOverviewStats
import com.example.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

enum class AdminSection(val label: String) {
    DASHBOARD("Dashboard"),
    NOTIFICATIONS("Notifications"),
    USERS("Users"),
    INVESTMENTS("Investments"),
    DEPOSITS("Deposits"),
    PAYMENT_ACCOUNTS("Payment Accounts"),
    WITHDRAWALS("Withdrawals"),
    TRANSACTIONS("Transactions"),
    AUDIT_LOGS("Audit Logs"),
    SETTINGS("Security & Settings")
}

data class GlobalSearchResult(
    val id: String,
    val type: String, // "USER", "DEPOSIT", "WITHDRAWAL", "INVESTMENT", "TRANSACTION"
    val title: String,
    val subtitle: String,
    val amount: String? = null,
    val status: String? = null,
    val timestamp: Long = 0L,
    val targetSection: AdminSection,
    val targetId: String
)

class AdminViewModel(private val adminRepository: AdminRepository) : ViewModel() {

    private val _currentSection = MutableStateFlow(AdminSection.DASHBOARD)
    val currentSection: StateFlow<AdminSection> = _currentSection.asStateFlow()

    private val _selectedUserId = MutableStateFlow<String?>(null)
    val selectedUserId: StateFlow<String?> = _selectedUserId.asStateFlow()

    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _overviewStats = MutableStateFlow(AdminOverviewStats())
    val overviewStats: StateFlow<AdminOverviewStats> = _overviewStats.asStateFlow()

    val usersList: StateFlow<List<UserEntity>> = adminRepository.usersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val investmentsList: StateFlow<List<InvestmentEntity>> = adminRepository.allInvestmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsList: StateFlow<List<TransactionEntity>> = adminRepository.allTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDepositsList: StateFlow<List<TransactionEntity>> = adminRepository.pendingDepositsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingWithdrawalsList: StateFlow<List<TransactionEntity>> = adminRepository.pendingWithdrawalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawalsList: StateFlow<List<WithdrawalEntity>> = adminRepository.allWithdrawalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingWithdrawalsEntityList: StateFlow<List<WithdrawalEntity>> = adminRepository.pendingWithdrawalsListFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogsList: StateFlow<List<AuditLogEntity>> = adminRepository.auditLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentAccountsList: StateFlow<List<PaymentAccountEntity>> = adminRepository.allPaymentAccountsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminNotificationsList: StateFlow<List<NotificationEntity>> = adminRepository.adminNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminUnreadCount: StateFlow<Int> = adminRepository.adminUnreadCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Global Search Engine combining real database flows
    val searchResults: StateFlow<List<GlobalSearchResult>> = combine(
        _globalSearchQuery,
        usersList,
        transactionsList,
        investmentsList,
        allWithdrawalsList
    ) { query, users, transactions, investments, withdrawals ->
        val q = query.trim().lowercase()
        if (q.isBlank()) return@combine emptyList<GlobalSearchResult>()

        val results = mutableListOf<GlobalSearchResult>()

        // 1. Users Search (Name, Email, Phone, ID) - NEVER expose password
        users.filter {
            it.fullName.lowercase().contains(q) ||
            it.email.lowercase().contains(q) ||
            it.phoneNumber.lowercase().contains(q) ||
            it.id.lowercase().contains(q)
        }.forEach { user ->
            results.add(
                GlobalSearchResult(
                    id = "USR-${user.id}",
                    type = "USER",
                    title = user.fullName,
                    subtitle = "${user.email} • ${user.phoneNumber} • ${user.country}",
                    status = user.accountStatus,
                    timestamp = user.createdAt,
                    targetSection = AdminSection.USERS,
                    targetId = user.id
                )
            )
        }

        // 2. Deposits & Transactions Search
        transactions.filter {
            it.id.lowercase().contains(q) ||
            (it.reference != null && it.reference.lowercase().contains(q)) ||
            it.userId.lowercase().contains(q) ||
            (it.notes != null && it.notes.lowercase().contains(q)) ||
            (it.paymentMethod != null && it.paymentMethod.lowercase().contains(q))
        }.forEach { tx ->
            val isDeposit = tx.type.equals("DEPOSIT", ignoreCase = true)
            results.add(
                GlobalSearchResult(
                    id = "TX-${tx.id}",
                    type = if (isDeposit) "DEPOSIT" else "TRANSACTION",
                    title = if (isDeposit) "Deposit ${tx.currency} ${tx.amount}" else "${tx.type} ${tx.currency} ${tx.amount}",
                    subtitle = "Ref: ${tx.reference ?: tx.id} • User ID: ${tx.userId}",
                    amount = "${tx.currency} ${"%,.2f".format(tx.amount)}",
                    status = tx.status,
                    timestamp = tx.createdAt,
                    targetSection = if (isDeposit) AdminSection.DEPOSITS else AdminSection.TRANSACTIONS,
                    targetId = tx.id
                )
            )
        }

        // 3. Withdrawals Search
        withdrawals.filter {
            it.withdrawalId.lowercase().contains(q) ||
            it.userId.lowercase().contains(q) ||
            it.destination.lowercase().contains(q) ||
            it.network.lowercase().contains(q) ||
            (it.transactionId != null && it.transactionId.lowercase().contains(q))
        }.forEach { wth ->
            results.add(
                GlobalSearchResult(
                    id = "WD-${wth.withdrawalId}",
                    type = "WITHDRAWAL",
                    title = "Withdrawal ${wth.currency} ${wth.amount}",
                    subtitle = "To: ${wth.destination} (${wth.network}) • User ID: ${wth.userId}",
                    amount = "${wth.currency} ${"%,.2f".format(wth.amount)}",
                    status = wth.status,
                    timestamp = wth.createdAt,
                    targetSection = AdminSection.WITHDRAWALS,
                    targetId = wth.withdrawalId
                )
            )
        }

        // 4. Investments Search
        investments.filter {
            it.id.lowercase().contains(q) ||
            it.userId.lowercase().contains(q) ||
            it.currency.lowercase().contains(q) ||
            it.network.lowercase().contains(q)
        }.forEach { inv ->
            results.add(
                GlobalSearchResult(
                    id = "INV-${inv.id}",
                    type = "INVESTMENT",
                    title = "Position #${inv.id.takeLast(8)} • ${inv.currency} ${inv.amount}",
                    subtitle = "ID: ${inv.id} • Current Val: ${inv.currency} ${"%,.2f".format(inv.currentValue)} • User: ${inv.userId}",
                    amount = "${inv.currency} ${"%,.2f".format(inv.amount)}",
                    status = inv.status,
                    timestamp = inv.createdAt,
                    targetSection = AdminSection.INVESTMENTS,
                    targetId = inv.id
                )
            )
        }

        results.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalSearchResults: StateFlow<List<GlobalSearchResult>> = searchResults

    // Selected user real reactive data streams
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedUserFlow = _selectedUserId.flatMapLatest { userId ->
        if (userId != null) adminRepository.observeUserById(userId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedUser: StateFlow<UserEntity?> = selectedUserFlow

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedUserInvestmentsFlow = _selectedUserId.flatMapLatest { userId ->
        if (userId != null) adminRepository.observeUserInvestments(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedUserInvestments: StateFlow<List<InvestmentEntity>> = selectedUserInvestmentsFlow

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedUserTransactionsFlow = _selectedUserId.flatMapLatest { userId ->
        if (userId != null) adminRepository.observeUserTransactions(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedUserTransactions: StateFlow<List<TransactionEntity>> = selectedUserTransactionsFlow

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedUserWithdrawalsFlow = _selectedUserId.flatMapLatest { userId ->
        if (userId != null) adminRepository.observeUserWithdrawals(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedUserWithdrawals: StateFlow<List<WithdrawalEntity>> = selectedUserWithdrawalsFlow

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedUserNotificationsFlow = _selectedUserId.flatMapLatest { userId ->
        if (userId != null) adminRepository.observeUserNotifications(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedUserNotifications: StateFlow<List<NotificationEntity>> = selectedUserNotificationsFlow

    fun updateUserStatus(adminUser: UserEntity, userId: String, newStatus: String, reason: String) {
        viewModelScope.launch {
            val result = adminRepository.updateUserStatus(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                userId = userId,
                newStatus = newStatus,
                reason = reason
            )
            if (result.isSuccess) {
                _actionMessage.value = "User $userId account status set to $newStatus."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to update user status: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun selectUser(userId: String?) {
        _selectedUserId.value = userId
        if (userId != null) {
            _currentSection.value = AdminSection.USERS
        }
    }

    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    fun clearGlobalSearch() {
        _globalSearchQuery.value = ""
    }

    fun suspendUser(adminUser: UserEntity, userId: String, reason: String) {
        viewModelScope.launch {
            val result = adminRepository.updateUserStatus(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                userId = userId,
                newStatus = "SUSPENDED",
                reason = reason
            )
            if (result.isSuccess) {
                _actionMessage.value = "User $userId account has been SUSPENDED. Action recorded in audit log."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to suspend user: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun reactivateUser(adminUser: UserEntity, userId: String, reason: String) {
        viewModelScope.launch {
            val result = adminRepository.updateUserStatus(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                userId = userId,
                newStatus = "ACTIVE",
                reason = reason
            )
            if (result.isSuccess) {
                _actionMessage.value = "User $userId account has been REACTIVATED. Action recorded in audit log."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to reactivate user: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun addUserAdminNote(adminUser: UserEntity, userId: String, note: String) {
        viewModelScope.launch {
            val result = adminRepository.addAdminNote(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                userId = userId,
                note = note
            )
            if (result.isSuccess) {
                _actionMessage.value = "Administrative note recorded in audit log."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to record note: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun markAdminNotificationAsRead(id: String) {
        viewModelScope.launch {
            adminRepository.markAdminNotificationAsRead(id)
        }
    }

    fun markAllAdminNotificationsAsRead() {
        viewModelScope.launch {
            adminRepository.markAllAdminNotificationsAsRead()
        }
    }

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        refreshStats()
        viewModelScope.launch {
            adminRepository.ensureDefaultPaymentAccountsExist()
        }
    }

    fun setSection(section: AdminSection) {
        _currentSection.value = section
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun refreshStats() {
        viewModelScope.launch {
            _overviewStats.value = adminRepository.getOverviewStats()
        }
    }

    fun addPaymentAccount(
        adminUser: UserEntity,
        currency: String,
        paymentMethod: String,
        network: String?,
        accountName: String?,
        accountNumber: String?,
        walletAddress: String?,
        instructions: String,
        autoPublish30Min: Boolean = true
    ) {
        viewModelScope.launch {
            val result = adminRepository.addPaymentAccount(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                currency = currency,
                paymentMethod = paymentMethod,
                network = network,
                accountName = accountName,
                accountNumber = accountNumber,
                walletAddress = walletAddress,
                instructions = instructions,
                autoPublish30Min = autoPublish30Min
            )
            if (result.isSuccess) {
                _actionMessage.value = "Payment account (${currency} - ${paymentMethod}) created successfully."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to add payment account: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updatePaymentAccount(
        adminUser: UserEntity,
        id: String,
        paymentMethod: String,
        network: String?,
        accountName: String?,
        accountNumber: String?,
        walletAddress: String?,
        instructions: String
    ) {
        viewModelScope.launch {
            val result = adminRepository.updatePaymentAccount(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                id = id,
                paymentMethod = paymentMethod,
                network = network,
                accountName = accountName,
                accountNumber = accountNumber,
                walletAddress = walletAddress,
                instructions = instructions
            )
            if (result.isSuccess) {
                _actionMessage.value = "Payment account updated successfully."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to update payment account: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun publishPaymentAccount(
        adminUser: UserEntity,
        id: String,
        durationMinutes: Int = 30
    ) {
        viewModelScope.launch {
            val result = adminRepository.publishPaymentAccount(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                id = id,
                durationMinutes = durationMinutes
            )
            if (result.isSuccess) {
                _actionMessage.value = "Payment account $id is now published to investors for $durationMinutes minutes."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to publish payment account: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun unpublishPaymentAccount(
        adminUser: UserEntity,
        id: String
    ) {
        viewModelScope.launch {
            val result = adminRepository.unpublishPaymentAccount(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                id = id
            )
            if (result.isSuccess) {
                _actionMessage.value = "Payment account $id publication has been revoked / expired."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to unpublish payment account: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun togglePaymentAccountActive(
        adminUser: UserEntity,
        id: String,
        currentActive: Boolean
    ) {
        viewModelScope.launch {
            val newActive = !currentActive
            val result = adminRepository.setAccountActiveStatus(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                id = id,
                isActive = newActive
            )
            if (result.isSuccess) {
                _actionMessage.value = if (newActive) "Payment account activated." else "Payment account deactivated."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to update status: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateTransactionStatus(
        adminUser: UserEntity,
        transactionId: String,
        newStatus: String,
        reason: String
    ) {
        viewModelScope.launch {
            val result = adminRepository.updateTransactionStatus(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                transactionId = transactionId,
                newStatus = newStatus,
                reason = reason
            )
            if (result.isSuccess) {
                _actionMessage.value = "Transaction $transactionId marked as $newStatus. Audited."
                refreshStats()
            } else {
                _actionMessage.value = "Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateWithdrawalStatus(
        adminUser: UserEntity,
        withdrawalId: String,
        newStatus: String,
        reason: String
    ) {
        viewModelScope.launch {
            val result = adminRepository.updateWithdrawalStatus(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                withdrawalId = withdrawalId,
                newStatus = newStatus,
                reason = reason
            )
            if (result.isSuccess) {
                _actionMessage.value = "Withdrawal request $withdrawalId transitioned to $newStatus. Audited."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to update withdrawal: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateInvestment(
        adminUser: UserEntity,
        investmentId: String,
        newStatus: String,
        newCurrentValue: Double,
        newRealizedReturn: Double,
        newPerformancePercentage: Double,
        reason: String
    ) {
        viewModelScope.launch {
            val result = adminRepository.updateInvestmentPerformance(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                investmentId = investmentId,
                newStatus = newStatus,
                newCurrentValue = newCurrentValue,
                newRealizedReturn = newRealizedReturn,
                newPerformancePercentage = newPerformancePercentage,
                reason = reason
            )
            if (result.isSuccess) {
                _actionMessage.value = "Investment $investmentId updated with status $newStatus and logged to audit trail."
                refreshStats()
            } else {
                _actionMessage.value = "Failed to update investment: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun recordManualAuditLog(
        adminUser: UserEntity,
        action: String,
        targetId: String,
        targetType: String,
        valueChange: String,
        reason: String
    ) {
        viewModelScope.launch {
            adminRepository.logAdminAction(
                adminId = adminUser.id,
                adminEmail = adminUser.email,
                action = action,
                targetId = targetId,
                targetType = targetType,
                valueChange = valueChange,
                reason = reason
            )
            _actionMessage.value = "Administrative action logged to secure audit trail."
            refreshStats()
        }
    }
}

class AdminViewModelFactory(private val adminRepository: AdminRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(adminRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
