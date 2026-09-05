package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_accounts")
data class PaymentAccountEntity(
    @PrimaryKey
    val id: String, // e.g. "PAC-12345678"
    val currency: String, // "PHP" or "USDT"
    val paymentMethod: String, // e.g. "GCash", "Maya", "BDO Unibank", "USDT"
    val network: String? = null, // "TRC20" or "BEP20" (for USDT)
    val accountName: String? = null, // Account Name for PHP
    val accountNumber: String? = null, // Account Number for PHP
    val walletAddress: String? = null, // Wallet Address for USDT
    val instructions: String = "", // Additional Payment Instructions
    val isActive: Boolean = true, // Whether account is enabled in system
    val isPublished: Boolean = false, // Whether published to investors
    val publishedAt: Long? = null, // Server timestamp when published
    val expiresAt: Long? = null, // Server timestamp when publication expires (publishedAt + 30 * 60 * 1000L)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastPublishedBy: String? = null // Admin email
) {
    /**
     * Checks if this payment account is currently active, published, and within the 30-minute validity window.
     */
    fun isCurrentlyValid(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return isActive && isPublished && expiresAt != null && currentTimeMs < expiresAt
    }

    /**
     * Checks if the publication has expired.
     */
    fun isExpired(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return expiresAt != null && currentTimeMs >= expiresAt
    }

    /**
     * Returns the remaining valid seconds (0 if expired or not published).
     */
    fun getRemainingSeconds(currentTimeMs: Long = System.currentTimeMillis()): Long {
        if (expiresAt == null || expiresAt <= currentTimeMs || !isPublished || !isActive) {
            return 0L
        }
        return (expiresAt - currentTimeMs) / 1000L
    }
}
