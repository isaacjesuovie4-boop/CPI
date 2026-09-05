package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeTransactionsByUserId(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getTransactionsByUserId(userId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    fun observeTransactionsByType(userId: String, type: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'WITHDRAWAL' AND status = 'COMPLETED'")
    suspend fun getTotalWithdrawnAmount(userId: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'DEPOSIT' AND status = 'COMPLETED'")
    suspend fun getTotalDepositedAmount(userId: String): Double?

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = 'DEPOSIT' AND status = 'PENDING' ORDER BY createdAt DESC")
    fun observePendingDeposits(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'WITHDRAWAL' AND status = 'PENDING' ORDER BY createdAt DESC")
    fun observePendingWithdrawals(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'DEPOSIT' ORDER BY createdAt DESC")
    fun observeAllDeposits(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'WITHDRAWAL' ORDER BY createdAt DESC")
    fun observeAllWithdrawals(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'DEPOSIT' AND (status = 'APPROVED' OR status = 'COMPLETED')")
    suspend fun getApprovedDepositsCount(): Int

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEPOSIT' AND (status = 'APPROVED' OR status = 'COMPLETED')")
    suspend fun getApprovedDepositsSum(): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'DEPOSIT' AND status = 'PENDING'")
    suspend fun getPendingDepositCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'DEPOSIT' AND status = 'PENDING'")
    fun observePendingDepositCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'WITHDRAWAL' AND status = 'PENDING'")
    suspend fun getPendingWithdrawalCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'WITHDRAWAL' AND status = 'PENDING'")
    fun observePendingWithdrawalCount(): Flow<Int>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateTransactionStatus(id: String, status: String)
}
