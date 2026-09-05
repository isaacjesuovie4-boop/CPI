package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val investmentId: String? = null,
    val paymentAccountId: String? = null,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "INVESTMENT", "RETURN"
    val amount: Double,
    val currency: String, // "USDT" or "PHP"
    val status: String,   // "COMPLETED", "PENDING", "PROCESSING", "FAILED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis(),
    val reference: String = "",
    val paymentMethod: String = "", // e.g. "GCash", "BDO", "TRC20", "BEP20"
    val notes: String = "" // Additional investor / verification notes
)

