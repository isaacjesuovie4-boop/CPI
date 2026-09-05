package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val country: String,
    val occupation: String,
    val selectedCurrency: String, // "USDT" or "PHP"
    val passwordHash: String,
    val passwordSalt: String,
    val role: String = "INVESTOR", // "INVESTOR", "ADMIN"
    val createdAt: Long = System.currentTimeMillis(),
    val accountStatus: String = "ACTIVE"
)
