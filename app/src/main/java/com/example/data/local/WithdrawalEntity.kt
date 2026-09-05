package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey
    val withdrawalId: String,
    val userId: String,
    val amount: Double,
    val currency: String, // "USDT" or "PHP"
    val destination: String, // Receiving address or bank/e-wallet account details
    val network: String = "", // "TRC20", "BEP20", or Bank name / E-Wallet
    val status: String = "PENDING_REVIEW", // "PENDING_REVIEW", "PROCESSING", "COMPLETED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val processedByAdminId: String? = null,
    val rejectionReason: String? = null,
    val otpHash: String? = null,
    val otpExpiresAt: Long? = null,
    val isOtpVerified: Boolean = false,
    val transactionId: String? = null
)
