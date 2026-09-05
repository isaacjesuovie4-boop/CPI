package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index("userId"),
        Index("recipientRole"),
        Index("isRead"),
        Index("createdAt")
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val notificationId: String,
    val userId: String,
    val recipientRole: String = "INVESTOR", // "INVESTOR", "ADMIN", "ALL"
    val type: String, // "DEPOSIT", "WITHDRAWAL", "INVESTMENT", "SECURITY", "SYSTEM"
    val title: String,
    val message: String,
    val relatedId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
