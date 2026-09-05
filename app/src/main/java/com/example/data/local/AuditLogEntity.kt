package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey
    val id: String,
    val adminId: String,
    val adminEmail: String,
    val action: String,
    val targetId: String,
    val targetType: String, // "USER", "DEPOSIT", "WITHDRAWAL", "INVESTMENT", "SECURITY", "SYSTEM"
    val valueChange: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
