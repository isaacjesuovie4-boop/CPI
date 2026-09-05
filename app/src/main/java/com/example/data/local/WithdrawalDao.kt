package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeWithdrawalsByUserId(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getWithdrawalsByUserId(userId: String): List<WithdrawalEntity>

    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun observeAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    suspend fun getAllWithdrawals(): List<WithdrawalEntity>

    @Query("SELECT * FROM withdrawals WHERE status = 'PENDING_REVIEW' OR status = 'PROCESSING' ORDER BY createdAt DESC")
    fun observePendingWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'PENDING_REVIEW' OR status = 'PROCESSING'")
    suspend fun getPendingWithdrawalsCount(): Int

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'COMPLETED'")
    suspend fun getCompletedWithdrawalsCount(): Int

    @Query("SELECT * FROM withdrawals WHERE withdrawalId = :id LIMIT 1")
    suspend fun getWithdrawalById(id: String): WithdrawalEntity?

    @Query("UPDATE withdrawals SET status = :status, updatedAt = :updatedAt, processedAt = :processedAt, processedByAdminId = :adminId, rejectionReason = :reason, transactionId = :transactionId WHERE withdrawalId = :id")
    suspend fun updateWithdrawalStatus(
        id: String,
        status: String,
        updatedAt: Long,
        processedAt: Long? = null,
        adminId: String? = null,
        reason: String? = null,
        transactionId: String? = null
    )

    @Query("SELECT SUM(amount) FROM withdrawals WHERE userId = :userId AND currency = :currency AND (status = 'PENDING_REVIEW' OR status = 'PROCESSING')")
    suspend fun getPendingWithdrawalsSum(userId: String, currency: String): Double?

    @Query("SELECT SUM(amount) FROM withdrawals WHERE userId = :userId AND currency = :currency AND status = 'COMPLETED'")
    suspend fun getCompletedWithdrawalsSum(userId: String, currency: String): Double?
}
