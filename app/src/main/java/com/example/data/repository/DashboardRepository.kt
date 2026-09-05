package com.example.data.repository

import com.example.data.local.InvestmentDao
import com.example.data.local.InvestmentEntity
import com.example.data.local.NotificationDao
import com.example.data.local.NotificationEntity
import com.example.data.local.PaymentAccountDao
import com.example.data.local.PaymentAccountEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.WithdrawalDao
import com.example.data.local.WithdrawalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

data class DashboardOverview(
    val availableBalance: Double = 0.0,
    val totalInvested: Double = 0.0,
    val activeInvestmentsCount: Int = 0,
    val completedInvestmentsCount: Int = 0,
    val currentInvestmentValue: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val pendingWithdrawals: Double = 0.0,
    val overallPerformancePercentage: Double = 0.0
)

class DashboardRepository(
    private val investmentDao: InvestmentDao,
    private val transactionDao: TransactionDao,
    private val paymentAccountDao: PaymentAccountDao,
    private val withdrawalDao: WithdrawalDao,
    private val notificationDao: NotificationDao
) {
    // In-memory transient storage for active OTP generation session (SHA-256 hashed)
    private val activeOtpSessions = mutableMapOf<String, OtpSession>()

    data class OtpSession(
        val hash: String,
        val expiresAt: Long,
        val salt: String
    )

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun observeInvestments(userId: String): Flow<List<InvestmentEntity>> {
        return investmentDao.observeInvestmentsByUserId(userId)
    }

    fun observeTransactions(userId: String): Flow<List<TransactionEntity>> {
        return transactionDao.observeTransactionsByUserId(userId)
    }

    fun observeWithdrawals(userId: String): Flow<List<WithdrawalEntity>> {
        return withdrawalDao.observeWithdrawalsByUserId(userId)
    }

    fun observeNotifications(userId: String): Flow<List<NotificationEntity>> {
        return notificationDao.observeNotificationsForUser(userId)
    }

    fun observeUnreadNotificationCount(userId: String): Flow<Int> {
        return notificationDao.observeUnreadCountForUser(userId)
    }

    suspend fun markNotificationAsRead(userId: String, notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val notification = notificationDao.getNotificationById(notificationId)
                ?: return@withContext Result.failure(IllegalArgumentException("Notification not found."))

            // Security check: ensure notification belongs to user or is public
            if (notification.userId != userId && notification.recipientRole != "ALL" && notification.userId != "ALL") {
                return@withContext Result.failure(IllegalAccessException("Access denied: You cannot modify notifications belonging to another user."))
            }

            notificationDao.markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsAsRead(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            notificationDao.markAllAsReadForUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observePublishedPaymentAccounts(currency: String): Flow<List<PaymentAccountEntity>> {
        return paymentAccountDao.observePublishedPaymentAccounts(currency.uppercase().trim())
    }

    suspend fun getPaymentAccountById(id: String): PaymentAccountEntity? = withContext(Dispatchers.IO) {
        paymentAccountDao.getPaymentAccountById(id)
    }

    fun observeOverview(userId: String): Flow<DashboardOverview> {
        val investmentsFlow = investmentDao.observeInvestmentsByUserId(userId)
        val transactionsFlow = transactionDao.observeTransactionsByUserId(userId)
        val withdrawalsFlow = withdrawalDao.observeWithdrawalsByUserId(userId)

        return combine(investmentsFlow, transactionsFlow, withdrawalsFlow) { investments, transactions, withdrawals ->
            val totalDeposits = transactions
                .filter { it.type == "DEPOSIT" && (it.status == "COMPLETED" || it.status == "APPROVED") }
                .sumOf { it.amount }

            val totalReturns = transactions
                .filter { it.type == "RETURN" && (it.status == "COMPLETED" || it.status == "APPROVED") }
                .sumOf { it.amount }

            val totalInvested = investments
                .filter { it.status != "CANCELLED" }
                .sumOf { it.amount }

            val activeInvestments = investments.filter { it.status == "ACTIVE" }
            val completedInvestments = investments.filter { it.status == "COMPLETED" }
            val activeCount = activeInvestments.size
            val completedCount = completedInvestments.size

            val currentInvestmentValue = investments.sumOf { it.currentValue }
            val performance = if (totalInvested > 0) {
                ((currentInvestmentValue - totalInvested) / totalInvested) * 100.0
            } else {
                0.0
            }

            val completedWithdrawals = withdrawals
                .filter { it.status == "COMPLETED" || it.status == "APPROVED" }
                .sumOf { it.amount }

            val pendingWithdrawals = withdrawals
                .filter { it.status == "PENDING_REVIEW" || it.status == "PROCESSING" || it.status == "PENDING" }
                .sumOf { it.amount }

            // Balance formula: Approved Deposits + Returns - Capital in Portfolios - Completed Withdrawals - Reserved Pending Withdrawals
            val available = (totalDeposits + totalReturns - totalInvested - completedWithdrawals - pendingWithdrawals).coerceAtLeast(0.0)

            DashboardOverview(
                availableBalance = available,
                totalInvested = totalInvested,
                activeInvestmentsCount = activeCount,
                completedInvestmentsCount = completedCount,
                currentInvestmentValue = currentInvestmentValue,
                totalWithdrawn = completedWithdrawals,
                pendingWithdrawals = pendingWithdrawals,
                overallPerformancePercentage = performance
            )
        }
    }

    suspend fun getAvailableBalance(userId: String, currency: String): Double = withContext(Dispatchers.IO) {
        val normCurrency = currency.uppercase().trim()
        val userTxList = transactionDao.getTransactionsByUserId(userId)
        val userInvList = investmentDao.getInvestmentsByUserId(userId)
        val userWthList = withdrawalDao.getWithdrawalsByUserId(userId)

        val totalApprovedDeposits = userTxList
            .filter { it.type == "DEPOSIT" && (it.status == "COMPLETED" || it.status == "APPROVED") && it.currency == normCurrency }
            .sumOf { it.amount }
        val totalReturns = userTxList
            .filter { it.type == "RETURN" && (it.status == "COMPLETED" || it.status == "APPROVED") && it.currency == normCurrency }
            .sumOf { it.amount }
        val totalInvested = userInvList
            .filter { it.currency == normCurrency && it.status != "CANCELLED" }
            .sumOf { it.amount }
        val totalCompletedWithdrawals = userWthList
            .filter { it.currency == normCurrency && (it.status == "COMPLETED" || it.status == "APPROVED") }
            .sumOf { it.amount }
        val totalPendingWithdrawals = userWthList
            .filter { it.currency == normCurrency && (it.status == "PENDING_REVIEW" || it.status == "PROCESSING" || it.status == "PENDING") }
            .sumOf { it.amount }

        (totalApprovedDeposits + totalReturns - totalInvested - totalCompletedWithdrawals - totalPendingWithdrawals).coerceAtLeast(0.0)
    }

    suspend fun submitDeposit(
        userId: String,
        paymentAccountId: String,
        amount: Double,
        currency: String,
        paymentMethod: String,
        referenceNo: String,
        notes: String = ""
    ): Result<TransactionEntity> = withContext(Dispatchers.IO) {
        try {
            val normalizedCurrency = currency.uppercase().trim()
            if (normalizedCurrency != "USDT" && normalizedCurrency != "PHP") {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported currency: $currency. Only USDT and PHP are supported.")
                )
            }

            // Verify currency amount boundaries
            if (normalizedCurrency == "USDT") {
                if (amount < 50.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Minimum deposit for USDT is 50 USDT.")
                    )
                }
                if (amount > 5000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Maximum deposit for USDT is 5,000 USDT.")
                    )
                }
            } else { // PHP
                if (amount < 3000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Minimum deposit for PHP is ₱3,000.")
                    )
                }
                if (amount > 100000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Maximum deposit for PHP is ₱100,000.")
                    )
                }
            }

            if (referenceNo.trim().isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Please provide your payment reference number or transaction hash.")
                )
            }

            // Verify payment account validity & 30-minute window
            val account = paymentAccountDao.getPaymentAccountById(paymentAccountId)
            val now = System.currentTimeMillis()

            if (account == null || !account.isCurrentlyValid(now)) {
                // Record expired payment details notification
                val expNotif = NotificationEntity(
                    notificationId = "NOTIF-EXP-" + UUID.randomUUID().toString().take(8).uppercase(),
                    userId = userId,
                    recipientRole = "INVESTOR",
                    type = "DEPOSIT",
                    title = "Payment Details Expired",
                    message = "Your payment details have expired. Please request new payment details.",
                    relatedId = paymentAccountId,
                    isRead = false,
                    createdAt = now
                )
                notificationDao.insertNotification(expNotif)

                return@withContext Result.failure(
                    IllegalStateException("These payment details have expired. Please request new payment details.")
                )
            }

            val txId = "TXN-DEP-" + UUID.randomUUID().toString().take(8).uppercase()
            val transaction = TransactionEntity(
                id = txId,
                userId = userId,
                paymentAccountId = paymentAccountId,
                type = "DEPOSIT",
                amount = amount,
                currency = normalizedCurrency,
                status = "PENDING", // Retains PENDING until manually verified and approved by admin
                createdAt = now,
                reference = referenceNo.trim(),
                paymentMethod = paymentMethod.ifBlank { account.paymentMethod },
                notes = notes.trim()
            )

            transactionDao.insertTransaction(transaction)

            // Investor Notification (PENDING_REVIEW)
            val investorNotif = NotificationEntity(
                notificationId = "NOTIF-DEP-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = userId,
                recipientRole = "INVESTOR",
                type = "DEPOSIT",
                title = "Deposit Submitted",
                message = "Your deposit request has been submitted and is awaiting review.",
                relatedId = txId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(investorNotif)

            // Admin Notification (Awaiting Review)
            val adminNotif = NotificationEntity(
                notificationId = "NOTIF-ADM-DEP-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = "ADMIN",
                recipientRole = "ADMIN",
                type = "DEPOSIT",
                title = "New Deposit Awaiting Review",
                message = "Deposit request $txId ($amount $normalizedCurrency) submitted and awaiting verification.",
                relatedId = txId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(adminNotif)

            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInvestment(
        userId: String,
        amount: Double,
        currency: String,
        durationHours: Int,
        network: String = ""
    ): Result<InvestmentEntity> = withContext(Dispatchers.IO) {
        try {
            val normalizedCurrency = currency.uppercase().trim()
            if (normalizedCurrency != "USDT" && normalizedCurrency != "PHP") {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported currency: $currency. Only USDT and PHP are supported.")
                )
            }

            if (normalizedCurrency == "USDT") {
                if (amount < 50.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Minimum investment amount for USDT is 50 USDT.")
                    )
                }
                if (amount > 5000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Maximum investment amount for USDT is 5,000 USDT.")
                    )
                }
            } else { // PHP
                if (amount < 3000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Minimum investment amount for PHP is ₱3,000.")
                    )
                }
                if (amount > 100000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Maximum investment amount for PHP is ₱100,000.")
                    )
                }
            }

            if (durationHours !in 24..48) {
                return@withContext Result.failure(
                    IllegalArgumentException("Investment duration must be between 24 hours and 48 hours.")
                )
            }

            val availableBalance = getAvailableBalance(userId, normalizedCurrency)

            if (availableBalance < amount) {
                return@withContext Result.failure(
                    IllegalStateException("Insufficient available balance ($availableBalance $normalizedCurrency). Please make a deposit and wait for administrator approval.")
                )
            }

            val now = System.currentTimeMillis()
            val endAt = now + (durationHours * 3600L * 1000L)
            val investmentId = "INV-" + UUID.randomUUID().toString().take(8).uppercase()

            val investment = InvestmentEntity(
                id = investmentId,
                userId = userId,
                amount = amount,
                currency = normalizedCurrency,
                durationHours = durationHours,
                startAt = now,
                endAt = endAt,
                status = "ACTIVE",
                currentValue = amount,
                realizedReturn = 0.0,
                performancePercentage = 0.0,
                network = if (normalizedCurrency == "USDT") (if (network.isNotBlank()) network else "TRC20") else "FIAT_PAYMENT",
                createdAt = now,
                updatedAt = now
            )

            investmentDao.insertInvestment(investment)

            // Create corresponding financial transaction record
            val txId = "TXN-INV-" + UUID.randomUUID().toString().take(8).uppercase()
            val transaction = TransactionEntity(
                id = txId,
                userId = userId,
                investmentId = investmentId,
                type = "INVESTMENT",
                amount = amount,
                currency = normalizedCurrency,
                status = "COMPLETED",
                createdAt = now,
                reference = "Allocation to $durationHours-Hour Portfolio ($investmentId)"
            )
            transactionDao.insertTransaction(transaction)

            // Investor Notification (Investment created)
            val invNotif = NotificationEntity(
                notificationId = "NOTIF-INV-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = userId,
                recipientRole = "INVESTOR",
                type = "INVESTMENT",
                title = "Investment Created",
                message = "Your investment has been successfully created.",
                relatedId = investmentId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(invNotif)

            Result.success(investment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cryptographic server-side withdrawal OTP generation
    suspend fun generateWithdrawalOtp(userId: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val random = SecureRandom()
            val otpNum = 100000 + random.nextInt(900000) // 6-digit non-static code
            val codeStr = otpNum.toString()
            val salt = UUID.randomUUID().toString()
            val hash = sha256(codeStr + salt + userId)
            val now = System.currentTimeMillis()
            val expiresAt = now + (5 * 60 * 1000L) // 5 minutes validity

            synchronized(activeOtpSessions) {
                activeOtpSessions[userId] = OtpSession(
                    hash = hash,
                    expiresAt = expiresAt,
                    salt = salt
                )
            }

            // In-app security notification for single-use verification code
            val notif = NotificationEntity(
                notificationId = "NOTIF-OTP-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = userId,
                recipientRole = "INVESTOR",
                type = "SECURITY",
                title = "Withdrawal Verification Code",
                message = "Your 6-digit withdrawal verification code is $codeStr. Valid for 5 minutes. Do not share this code.",
                relatedId = userId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(notif)

            Result.success(expiresAt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyWithdrawalOtp(userId: String, inputOtp: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = inputOtp.trim()
        if (trimmed.length != 6) return@withContext false

        val session = synchronized(activeOtpSessions) { activeOtpSessions[userId] } ?: return@withContext false
        val now = System.currentTimeMillis()
        if (now > session.expiresAt) {
            synchronized(activeOtpSessions) { activeOtpSessions.remove(userId) }
            return@withContext false
        }

        val computedHash = sha256(trimmed + session.salt + userId)
        val matches = computedHash == session.hash
        if (matches) {
            synchronized(activeOtpSessions) { activeOtpSessions.remove(userId) }
        }
        matches
    }

    // Submit Withdrawal Request with Double-Withdrawal Protection
    suspend fun submitWithdrawal(
        userId: String,
        amount: Double,
        currency: String,
        destination: String,
        network: String,
        isSecurityVerified: Boolean = true
    ): Result<WithdrawalEntity> = withContext(Dispatchers.IO) {
        try {
            val normCurrency = currency.uppercase().trim()
            if (normCurrency != "USDT" && normCurrency != "PHP") {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported currency: $currency. Only USDT and PHP are supported.")
                )
            }

            if (amount <= 0.0) {
                return@withContext Result.failure(
                    IllegalArgumentException("Withdrawal amount must be greater than zero.")
                )
            }

            // Minimum and maximum withdrawal amounts
            if (normCurrency == "USDT") {
                if (amount < 50.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Minimum withdrawal for USDT is 50.00 USDT.")
                    )
                }
                if (amount > 5000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Maximum withdrawal for USDT is 5,000.00 USDT per request.")
                    )
                }
            }
            if (normCurrency == "PHP") {
                if (amount < 3000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Minimum withdrawal for PHP is ₱3,000.00.")
                    )
                }
                if (amount > 100000.0) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Maximum withdrawal for PHP is ₱100,000.00 per request.")
                    )
                }
            }

            val cleanDest = destination.trim()
            if (cleanDest.length < 5) {
                return@withContext Result.failure(
                    IllegalArgumentException("Please enter complete destination account or wallet details.")
                )
            }

            val cleanNetwork = network.trim().ifBlank {
                if (normCurrency == "USDT") "TRC20" else "GCash"
            }

            // Atomic balance verification: balance must NEVER be exceeded
            val currentAvailable = getAvailableBalance(userId, normCurrency)
            if (amount > currentAvailable) {
                return@withContext Result.failure(
                    IllegalStateException("Withdrawal amount ($amount $normCurrency) exceeds available balance ($currentAvailable $normCurrency).")
                )
            }

            val now = System.currentTimeMillis()
            val withdrawalId = "WTH-" + UUID.randomUUID().toString().take(8).uppercase()
            val txId = "TXN-WTH-" + UUID.randomUUID().toString().take(8).uppercase()

            val withdrawal = WithdrawalEntity(
                withdrawalId = withdrawalId,
                userId = userId,
                amount = amount,
                currency = normCurrency,
                destination = cleanDest,
                network = cleanNetwork,
                status = "PENDING_REVIEW", // Initial status as required
                createdAt = now,
                updatedAt = now,
                isOtpVerified = isSecurityVerified,
                transactionId = txId
            )

            // Insert matching transaction in PENDING state to reserve funds in ledger
            val transaction = TransactionEntity(
                id = txId,
                userId = userId,
                type = "WITHDRAWAL",
                amount = amount,
                currency = normCurrency,
                status = "PENDING",
                createdAt = now,
                reference = "Withdrawal Request $withdrawalId to $cleanNetwork ($cleanDest)",
                paymentMethod = cleanNetwork,
                notes = "Withdrawal Request: $withdrawalId (PENDING_REVIEW)"
            )

            withdrawalDao.insertWithdrawal(withdrawal)
            transactionDao.insertTransaction(transaction)

            // Investor Notification (PENDING_REVIEW)
            val investorNotif = NotificationEntity(
                notificationId = "NOTIF-WTH-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = userId,
                recipientRole = "INVESTOR",
                type = "WITHDRAWAL",
                title = "Withdrawal Submitted",
                message = "Your withdrawal request has been submitted and is awaiting review.",
                relatedId = withdrawalId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(investorNotif)

            // Admin Notification (Awaiting Review)
            val adminNotif = NotificationEntity(
                notificationId = "NOTIF-ADM-WTH-" + UUID.randomUUID().toString().take(8).uppercase(),
                userId = "ADMIN",
                recipientRole = "ADMIN",
                type = "WITHDRAWAL",
                title = "New Withdrawal Awaiting Review",
                message = "Withdrawal request $withdrawalId ($amount $normCurrency) submitted and awaiting review.",
                relatedId = withdrawalId,
                isRead = false,
                createdAt = now
            )
            notificationDao.insertNotification(adminNotif)

            Result.success(withdrawal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
