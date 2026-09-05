package com.example.data.repository

import com.example.data.local.AuditLogDao
import com.example.data.local.AuditLogEntity
import com.example.data.local.InvestmentDao
import com.example.data.local.InvestmentEntity
import com.example.data.local.NotificationDao
import com.example.data.local.NotificationEntity
import com.example.data.local.PaymentAccountDao
import com.example.data.local.PaymentAccountEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalDao
import com.example.data.local.WithdrawalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

data class AdminOverviewStats(
    val totalRegisteredUsers: Int = 0,
    val activeUsersCount: Int = 0,
    val totalApprovedDepositsAmount: Double = 0.0,
    val totalApprovedDepositsCount: Int = 0,
    val pendingDepositsCount: Int = 0,
    val totalInvestmentsCount: Int = 0,
    val activeInvestmentsCount: Int = 0,
    val pendingWithdrawalsCount: Int = 0,
    val completedWithdrawalsCount: Int = 0,
    val totalAuditLogsCount: Int = 0,
    val totalPaymentAccountsCount: Int = 0
)

class AdminRepository(
    private val userDao: UserDao,
    private val investmentDao: InvestmentDao,
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao,
    private val paymentAccountDao: PaymentAccountDao,
    private val withdrawalDao: WithdrawalDao,
    private val notificationDao: NotificationDao
) {

    val usersFlow: Flow<List<UserEntity>> = userDao.observeAllUsers()
    val allInvestmentsFlow: Flow<List<InvestmentEntity>> = investmentDao.observeAllInvestments()
    val allTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.observeAllTransactions()
    val pendingDepositsFlow: Flow<List<TransactionEntity>> = transactionDao.observePendingDeposits()
    val pendingWithdrawalsFlow: Flow<List<TransactionEntity>> = transactionDao.observePendingWithdrawals()
    val allWithdrawalsFlow: Flow<List<WithdrawalEntity>> = withdrawalDao.observeAllWithdrawals()
    val pendingWithdrawalsListFlow: Flow<List<WithdrawalEntity>> = withdrawalDao.observePendingWithdrawals()
    val auditLogsFlow: Flow<List<AuditLogEntity>> = auditLogDao.observeAllAuditLogs()
    val allPaymentAccountsFlow: Flow<List<PaymentAccountEntity>> = paymentAccountDao.observeAllPaymentAccounts()
    val adminNotificationsFlow: Flow<List<NotificationEntity>> = notificationDao.observeAdminNotifications()
    val adminUnreadCountFlow: Flow<Int> = notificationDao.observeAdminUnreadCount()

    suspend fun markAdminNotificationAsRead(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            notificationDao.markAsRead(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAdminNotificationsAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            notificationDao.markAllAsReadForAdmin()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOverviewStats(): AdminOverviewStats = withContext(Dispatchers.IO) {
        val totalUsers = userDao.getUserCount()
        val activeUsers = userDao.getActiveUserCount()
        val approvedDepositsCount = transactionDao.getApprovedDepositsCount()
        val approvedDepositsSum = transactionDao.getApprovedDepositsSum() ?: 0.0
        val pendingDeposits = transactionDao.getPendingDepositCount()
        val totalInvestments = investmentDao.getTotalInvestmentCount()
        val activeInvestments = investmentDao.getGlobalActiveInvestmentCount()
        val pendingWithdrawals = withdrawalDao.getPendingWithdrawalsCount()
        val completedWithdrawals = withdrawalDao.getCompletedWithdrawalsCount()
        val auditLogsCount = auditLogDao.getAuditLogCount()
        val paymentAccountsCount = paymentAccountDao.getPaymentAccountsCount()

        AdminOverviewStats(
            totalRegisteredUsers = totalUsers,
            activeUsersCount = activeUsers,
            totalApprovedDepositsAmount = approvedDepositsSum,
            totalApprovedDepositsCount = approvedDepositsCount,
            pendingDepositsCount = pendingDeposits,
            totalInvestmentsCount = totalInvestments,
            activeInvestmentsCount = activeInvestments,
            pendingWithdrawalsCount = pendingWithdrawals,
            completedWithdrawalsCount = completedWithdrawals,
            totalAuditLogsCount = auditLogsCount,
            totalPaymentAccountsCount = paymentAccountsCount
        )
    }

    suspend fun logAdminAction(
        adminId: String,
        adminEmail: String,
        action: String,
        targetId: String,
        targetType: String,
        valueChange: String,
        reason: String
    ): AuditLogEntity = withContext(Dispatchers.IO) {
        val auditLog = AuditLogEntity(
            id = "AUD-" + UUID.randomUUID().toString().take(8).uppercase(),
            adminId = adminId,
            adminEmail = adminEmail,
            action = action,
            targetId = targetId,
            targetType = targetType,
            valueChange = valueChange,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        auditLogDao.insertAuditLog(auditLog)
        auditLog
    }

    suspend fun addPaymentAccount(
        adminId: String,
        adminEmail: String,
        currency: String,
        paymentMethod: String,
        network: String?,
        accountName: String?,
        accountNumber: String?,
        walletAddress: String?,
        instructions: String,
        autoPublish30Min: Boolean = true
    ): Result<PaymentAccountEntity> = withContext(Dispatchers.IO) {
        try {
            val normCurrency = currency.uppercase().trim()
            val now = System.currentTimeMillis()
            val expiresAt = if (autoPublish30Min) now + (30 * 60 * 1000L) else null

            val id = "ACC-" + normCurrency + "-" + UUID.randomUUID().toString().take(6).uppercase()
            val account = PaymentAccountEntity(
                id = id,
                currency = normCurrency,
                paymentMethod = paymentMethod.trim(),
                network = network?.trim()?.takeIf { it.isNotBlank() },
                accountName = accountName?.trim()?.takeIf { it.isNotBlank() },
                accountNumber = accountNumber?.trim()?.takeIf { it.isNotBlank() },
                walletAddress = walletAddress?.trim()?.takeIf { it.isNotBlank() },
                instructions = instructions.trim(),
                isActive = true,
                isPublished = autoPublish30Min,
                publishedAt = if (autoPublish30Min) now else null,
                expiresAt = expiresAt,
                createdAt = now,
                updatedAt = now,
                lastPublishedBy = adminEmail
            )

            paymentAccountDao.insertPaymentAccount(account)

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = "PAYMENT_ACCOUNT_CREATE",
                targetId = id,
                targetType = "PAYMENT_ACCOUNT",
                valueChange = "Created $normCurrency ($paymentMethod) account. Auto-published: $autoPublish30Min",
                reason = "Configured deposit receiving account"
            )

            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentAccount(
        adminId: String,
        adminEmail: String,
        id: String,
        paymentMethod: String,
        network: String?,
        accountName: String?,
        accountNumber: String?,
        walletAddress: String?,
        instructions: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = paymentAccountDao.getPaymentAccountById(id)
                ?: return@withContext Result.failure(Exception("Account $id not found"))

            val changedFields = mutableListOf<String>()
            val normMethod = paymentMethod.trim()
            val normNet = network?.trim()?.takeIf { it.isNotBlank() }
            val normName = accountName?.trim()?.takeIf { it.isNotBlank() }
            val normNum = accountNumber?.trim()?.takeIf { it.isNotBlank() }
            val normAddr = walletAddress?.trim()?.takeIf { it.isNotBlank() }
            val normInst = instructions.trim()

            if (existing.paymentMethod != normMethod) changedFields.add("paymentMethod")
            if (existing.network != normNet) changedFields.add("network")
            if (existing.accountName != normName) changedFields.add("accountName")
            if (existing.accountNumber != normNum) changedFields.add("accountNumber")
            if (existing.walletAddress != normAddr) changedFields.add("walletAddress")
            if (existing.instructions != normInst) changedFields.add("instructions")

            val updated = existing.copy(
                paymentMethod = normMethod,
                network = normNet,
                accountName = normName,
                accountNumber = normNum,
                walletAddress = normAddr,
                instructions = normInst,
                updatedAt = System.currentTimeMillis()
            )

            paymentAccountDao.updatePaymentAccount(updated)

            val fieldsSummary = if (changedFields.isEmpty()) "No fields changed" else "Fields changed: ${changedFields.joinToString(", ")}"

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = "PAYMENT_ACCOUNT_UPDATED",
                targetId = id,
                targetType = "PAYMENT_ACCOUNT",
                valueChange = fieldsSummary,
                reason = "Admin replaced payment gateway details"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishPaymentAccount(
        adminId: String,
        adminEmail: String,
        id: String,
        durationMinutes: Int = 30
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = paymentAccountDao.getPaymentAccountById(id)
                ?: return@withContext Result.failure(Exception("Account $id not found"))

            val now = System.currentTimeMillis()
            val expiresAt = now + (durationMinutes * 60 * 1000L)

            paymentAccountDao.updatePublishStatus(
                id = id,
                isPublished = true,
                publishedAt = now,
                expiresAt = expiresAt,
                adminEmail = adminEmail,
                updatedAt = now
            )

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = "PAYMENT_ACCOUNT_PUBLISH",
                targetId = id,
                targetType = "PAYMENT_ACCOUNT",
                valueChange = "Published for $durationMinutes min (expires at $expiresAt)",
                reason = "Gateway publication refresh"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unpublishPaymentAccount(
        adminId: String,
        adminEmail: String,
        id: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = paymentAccountDao.getPaymentAccountById(id)
                ?: return@withContext Result.failure(Exception("Account $id not found"))

            val now = System.currentTimeMillis()
            paymentAccountDao.updatePublishStatus(
                id = id,
                isPublished = false,
                publishedAt = null,
                expiresAt = null,
                adminEmail = adminEmail,
                updatedAt = now
            )

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = "PAYMENT_ACCOUNT_UNPUBLISH",
                targetId = id,
                targetType = "PAYMENT_ACCOUNT",
                valueChange = "Unpublished / Revoked publication",
                reason = "Manual unpublish"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setAccountActiveStatus(
        adminId: String,
        adminEmail: String,
        id: String,
        isActive: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = paymentAccountDao.getPaymentAccountById(id)
                ?: return@withContext Result.failure(Exception("Account $id not found"))

            val now = System.currentTimeMillis()
            paymentAccountDao.updateActiveStatus(id = id, isActive = isActive, updatedAt = now)

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = if (isActive) "PAYMENT_ACCOUNT_ACTIVATE" else "PAYMENT_ACCOUNT_DEACTIVATE",
                targetId = id,
                targetType = "PAYMENT_ACCOUNT",
                valueChange = "Active status set to $isActive",
                reason = "Account availability status toggle"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureDefaultPaymentAccountsExist() = withContext(Dispatchers.IO) {
        val count = paymentAccountDao.getPaymentAccountsCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val expires30Min = now + (30 * 60 * 1000L)
            val adminEmail = "admin@crestpoint.com"

            // 1. USDT TRC20 Account
            val usdtTrc20 = PaymentAccountEntity(
                id = "ACC-USDT-TRC20-01",
                currency = "USDT",
                paymentMethod = "USDT TRC20",
                network = "TRC20",
                walletAddress = "TXaZ7Q9xKpLm4N2wR8vY6sJ1dF3gH5bC8e",
                instructions = "Send exact USDT amount via TRON (TRC20) network only. Minimum deposit: 50 USDT.",
                isActive = true,
                isPublished = true,
                publishedAt = now,
                expiresAt = expires30Min,
                createdAt = now,
                updatedAt = now,
                lastPublishedBy = adminEmail
            )
            paymentAccountDao.insertPaymentAccount(usdtTrc20)

            // 2. USDT BEP20 Account
            val usdtBep20 = PaymentAccountEntity(
                id = "ACC-USDT-BEP20-01",
                currency = "USDT",
                paymentMethod = "USDT BEP20",
                network = "BEP20",
                walletAddress = "0x71C8366420AAb41666F6A6E0C42d1314B47833a6",
                instructions = "Send exact USDT amount via BNB Smart Chain (BEP20) network only. Minimum deposit: 50 USDT.",
                isActive = true,
                isPublished = true,
                publishedAt = now,
                expiresAt = expires30Min,
                createdAt = now,
                updatedAt = now,
                lastPublishedBy = adminEmail
            )
            paymentAccountDao.insertPaymentAccount(usdtBep20)

            // 3. PHP GCash Account
            val phpGcash = PaymentAccountEntity(
                id = "ACC-PHP-GCASH-01",
                currency = "PHP",
                paymentMethod = "GCash",
                accountName = "Crest Point Treasury Operations",
                accountNumber = "0917-882-9912",
                instructions = "Transfer via GCash Express Send. Provide exact Ref No. for manual verification.",
                isActive = true,
                isPublished = true,
                publishedAt = now,
                expiresAt = expires30Min,
                createdAt = now,
                updatedAt = now,
                lastPublishedBy = adminEmail
            )
            paymentAccountDao.insertPaymentAccount(phpGcash)

            // 4. PHP BDO Account
            val phpBdo = PaymentAccountEntity(
                id = "ACC-PHP-BDO-01",
                currency = "PHP",
                paymentMethod = "BDO Unibank",
                accountName = "Crest Point Investment Corp",
                accountNumber = "0082-9104-5519",
                instructions = "Transfer via BDO Online / InstaPay. Attach reference number in deposit note.",
                isActive = true,
                isPublished = true,
                publishedAt = now,
                expiresAt = expires30Min,
                createdAt = now,
                updatedAt = now,
                lastPublishedBy = adminEmail
            )
            paymentAccountDao.insertPaymentAccount(phpBdo)
        }
    }

    suspend fun updateTransactionStatus(
        adminId: String,
        adminEmail: String,
        transactionId: String,
        newStatus: String,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val tx = transactionDao.getTransactionById(transactionId)
                ?: return@withContext Result.failure(Exception("Transaction $transactionId not found."))

            val oldStatus = tx.status
            if (oldStatus == "COMPLETED" || oldStatus == "APPROVED") {
                if (newStatus == "COMPLETED" || newStatus == "APPROVED") {
                    return@withContext Result.failure(
                        IllegalStateException("This transaction has already been approved and credited. Double approval is strictly prohibited.")
                    )
                }
            }

            transactionDao.updateTransactionStatus(transactionId, newStatus)

            // Notifications for deposit status change
            if (tx.type == "DEPOSIT") {
                val notifMessage = when (newStatus) {
                    "COMPLETED", "APPROVED" -> "Your deposit has been approved and the approved amount has been credited to your available balance."
                    "FAILED", "REJECTED" -> "Your deposit was rejected. Please review the deposit details or contact support."
                    else -> "Your deposit status has been updated to $newStatus."
                }
                val notifTitle = when (newStatus) {
                    "COMPLETED", "APPROVED" -> "Deposit Approved"
                    "FAILED", "REJECTED" -> "Deposit Rejected"
                    else -> "Deposit Status Updated"
                }

                val depNotif = NotificationEntity(
                    notificationId = "NOTIF-DEP-" + UUID.randomUUID().toString().take(8).uppercase(),
                    userId = tx.userId,
                    recipientRole = "INVESTOR",
                    type = "DEPOSIT",
                    title = notifTitle,
                    message = notifMessage,
                    relatedId = transactionId,
                    isRead = false,
                    createdAt = System.currentTimeMillis()
                )
                notificationDao.insertNotification(depNotif)
            }

            val actionName = when {
                tx.type == "DEPOSIT" && (newStatus == "COMPLETED" || newStatus == "APPROVED") -> "ADMIN_APPROVE_DEPOSIT"
                tx.type == "DEPOSIT" && (newStatus == "FAILED" || newStatus == "REJECTED") -> "ADMIN_REJECT_DEPOSIT"
                tx.type == "WITHDRAWAL" && (newStatus == "COMPLETED" || newStatus == "APPROVED") -> "ADMIN_APPROVE_WITHDRAWAL"
                tx.type == "WITHDRAWAL" && (newStatus == "FAILED" || newStatus == "REJECTED") -> "ADMIN_REJECT_WITHDRAWAL"
                else -> "TRANSACTION_STATUS_UPDATE"
            }

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = actionName,
                targetId = transactionId,
                targetType = if (tx.type == "DEPOSIT") "DEPOSIT" else "WITHDRAWAL",
                valueChange = "Status: $oldStatus -> $newStatus (${tx.amount} ${tx.currency})",
                reason = reason.ifBlank { "Administrative verification" }
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Comprehensive Withdrawal Management for Admin
    suspend fun updateWithdrawalStatus(
        adminId: String,
        adminEmail: String,
        withdrawalId: String,
        newStatus: String, // "PENDING_REVIEW", "PROCESSING", "COMPLETED", "REJECTED"
        reason: String
    ): Result<WithdrawalEntity> = withContext(Dispatchers.IO) {
        try {
            val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
                ?: return@withContext Result.failure(Exception("Withdrawal request $withdrawalId not found."))

            val oldStatus = withdrawal.status

            // Double-processing protection
            if (oldStatus == "COMPLETED" || oldStatus == "APPROVED") {
                if (newStatus == "COMPLETED" || newStatus == "APPROVED") {
                    return@withContext Result.failure(
                        IllegalStateException("Withdrawal $withdrawalId has already been completed and dispatched. Double processing is prohibited.")
                    )
                }
            }
            if (oldStatus == "REJECTED" && newStatus == "REJECTED") {
                return@withContext Result.failure(
                    IllegalStateException("Withdrawal $withdrawalId has already been rejected.")
                )
            }

            val now = System.currentTimeMillis()
            val isFinal = newStatus == "COMPLETED" || newStatus == "REJECTED"
            val processedAt = if (isFinal) now else withdrawal.processedAt
            val processedBy = if (isFinal) adminId else withdrawal.processedByAdminId
            val rejectionReason = if (newStatus == "REJECTED") reason.ifBlank { "Administrative verification discrepancy." } else null

            // Find matching transaction
            val txId = withdrawal.transactionId ?: ("TXN-WTH-" + withdrawal.withdrawalId.removePrefix("WTH-"))
            val existingTx = transactionDao.getTransactionById(txId)

            val newTxStatus = when (newStatus) {
                "COMPLETED" -> "COMPLETED"
                "REJECTED" -> "REJECTED"
                "PROCESSING" -> "PROCESSING"
                else -> "PENDING"
            }

            if (existingTx != null) {
                transactionDao.updateTransactionStatus(existingTx.id, newTxStatus)
            } else {
                val newTx = TransactionEntity(
                    id = txId,
                    userId = withdrawal.userId,
                    type = "WITHDRAWAL",
                    amount = withdrawal.amount,
                    currency = withdrawal.currency,
                    status = newTxStatus,
                    createdAt = withdrawal.createdAt,
                    reference = "Withdrawal $withdrawalId to ${withdrawal.network} (${withdrawal.destination})",
                    paymentMethod = withdrawal.network,
                    notes = "Admin Action by $adminEmail: $newStatus - $reason"
                )
                transactionDao.insertTransaction(newTx)
            }

            val updatedWithdrawal = withdrawal.copy(
                status = newStatus,
                updatedAt = now,
                processedAt = processedAt,
                processedByAdminId = processedBy,
                rejectionReason = rejectionReason,
                transactionId = txId
            )
            withdrawalDao.updateWithdrawal(updatedWithdrawal)

            // Notifications for withdrawal status change
            val notifTitle = when (newStatus) {
                "PROCESSING" -> "Withdrawal Processing"
                "COMPLETED" -> "Withdrawal Completed"
                "REJECTED" -> "Withdrawal Rejected"
                else -> "Withdrawal Status Updated"
            }
            val notifMessage = when (newStatus) {
                "PROCESSING" -> "Your withdrawal is being processed."
                "COMPLETED" -> "Your withdrawal has been completed."
                "REJECTED" -> "Your withdrawal request was rejected. Please review the reason provided."
                else -> "Your withdrawal status has been updated to $newStatus."
            }

            val wthNotif = NotificationEntity(
                notificationId = "NOTIF-WTH-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = withdrawal.userId,
                recipientRole = "INVESTOR",
                type = "WITHDRAWAL",
                title = notifTitle,
                message = notifMessage,
                relatedId = withdrawalId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(wthNotif)

            val actionName = when (newStatus) {
                "COMPLETED" -> "ADMIN_APPROVE_WITHDRAWAL"
                "REJECTED" -> "ADMIN_REJECT_WITHDRAWAL"
                "PROCESSING" -> "ADMIN_PROCESS_WITHDRAWAL"
                else -> "ADMIN_UPDATE_WITHDRAWAL"
            }

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = actionName,
                targetId = withdrawalId,
                targetType = "WITHDRAWAL",
                valueChange = "Status: $oldStatus -> $newStatus (${withdrawal.amount} ${withdrawal.currency})",
                reason = reason.ifBlank { "Administrative withdrawal management" }
            )

            Result.success(updatedWithdrawal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInvestmentPerformance(
        adminId: String,
        adminEmail: String,
        investmentId: String,
        newStatus: String,
        newCurrentValue: Double,
        newRealizedReturn: Double,
        newPerformancePercentage: Double,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = investmentDao.getInvestmentById(investmentId)
                ?: return@withContext Result.failure(Exception("Investment $investmentId not found."))

            val now = System.currentTimeMillis()
            investmentDao.updateInvestmentPerformance(
                id = investmentId,
                currentValue = newCurrentValue,
                realizedReturn = newRealizedReturn,
                performancePercentage = newPerformancePercentage,
                status = newStatus,
                updatedAt = now
            )

            if (newStatus == "COMPLETED" && existing.status != "COMPLETED" && newRealizedReturn > 0) {
                val returnTxId = "TXN-RET-" + UUID.randomUUID().toString().take(8).uppercase()
                val returnTx = TransactionEntity(
                    id = returnTxId,
                    userId = existing.userId,
                    investmentId = investmentId,
                    type = "RETURN",
                    amount = newRealizedReturn,
                    currency = existing.currency,
                    status = "COMPLETED",
                    createdAt = now,
                    reference = "Settlement Return for Portfolio $investmentId"
                )
                transactionDao.insertTransaction(returnTx)
            }

            // Investor Notification (Investment Status Updated)
            val invNotif = NotificationEntity(
                notificationId = "NOTIF-INV-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = existing.userId,
                recipientRole = "INVESTOR",
                type = "INVESTMENT",
                title = "Investment Status Updated",
                message = "Your investment status has been updated.",
                relatedId = investmentId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(invNotif)

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = "INVESTMENT_STATUS_UPDATE",
                targetId = investmentId,
                targetType = "INVESTMENT",
                valueChange = "Status: ${existing.status} -> $newStatus | Val: ${existing.currentValue} -> $newCurrentValue | Perf: ${existing.performancePercentage}% -> $newPerformancePercentage%",
                reason = reason.ifBlank { "Performance settlement update" }
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserById(userId: String): Flow<UserEntity?> = userDao.observeUserById(userId)

    fun observeUserInvestments(userId: String): Flow<List<InvestmentEntity>> =
        investmentDao.observeInvestmentsByUserId(userId)

    fun observeUserTransactions(userId: String): Flow<List<TransactionEntity>> =
        transactionDao.observeTransactionsByUserId(userId)

    fun observeUserWithdrawals(userId: String): Flow<List<WithdrawalEntity>> =
        withdrawalDao.observeWithdrawalsByUserId(userId)

    fun observeUserNotifications(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.observeNotificationsForUser(userId)

    suspend fun updateUserStatus(
        adminId: String,
        adminEmail: String,
        userId: String,
        newStatus: String, // "ACTIVE", "SUSPENDED"
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserById(userId)
                ?: return@withContext Result.failure(Exception("User $userId not found."))

            val oldStatus = user.accountStatus
            userDao.updateUserAccountStatus(userId, newStatus)

            val actionName = if (newStatus == "SUSPENDED") "USER_SUSPENDED" else "USER_REACTIVATED"
            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = actionName,
                targetId = userId,
                targetType = "USER",
                valueChange = "Account status: $oldStatus -> $newStatus",
                reason = reason.ifBlank { "Administrative account status update" }
            )

            // Security notification to user
            val notif = NotificationEntity(
                notificationId = "NOTIF-SEC-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = userId,
                recipientRole = "INVESTOR",
                type = "SECURITY",
                title = if (newStatus == "SUSPENDED") "Account Suspended" else "Account Reactivated",
                message = if (newStatus == "SUSPENDED")
                    "Your account has been suspended by administration. Reason: ${reason.ifBlank { "Policy compliance review" }}."
                else
                    "Your account has been reactivated. You may now resume full platform activities.",
                relatedId = userId,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            notificationDao.insertNotification(notif)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAdminNote(
        adminId: String,
        adminEmail: String,
        userId: String,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserById(userId)
                ?: return@withContext Result.failure(Exception("User $userId not found."))

            logAdminAction(
                adminId = adminId,
                adminEmail = adminEmail,
                action = "ADMIN_NOTE_ADDED",
                targetId = userId,
                targetType = "USER",
                valueChange = "Internal note added for ${user.fullName} (${user.email})",
                reason = note.trim()
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
