package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val amount: Double,
    val currency: String, // "USDT" or "PHP"
    val durationHours: Int = 24, // 24 or 48 hours
    val startAt: Long,
    val endAt: Long,
    val status: String = "ACTIVE",   // "ACTIVE", "COMPLETED", "PENDING", "CANCELLED"
    val currentValue: Double = 0.0,
    val realizedReturn: Double = 0.0,
    val performancePercentage: Double = 0.0,
    val network: String = "", // "TRC20", "BEP20", "FIAT_BANK"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

