package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: InvestmentEntity)

    @Query("SELECT * FROM investments WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeInvestmentsByUserId(userId: String): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getInvestmentsByUserId(userId: String): List<InvestmentEntity>

    @Query("SELECT * FROM investments WHERE id = :id LIMIT 1")
    suspend fun getInvestmentById(id: String): InvestmentEntity?

    @Query("SELECT COUNT(*) FROM investments WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getActiveInvestmentCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM investments WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getCompletedInvestmentCount(userId: String): Int

    @Query("SELECT SUM(amount) FROM investments WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getTotalActiveInvestedAmount(userId: String): Double?

    @Query("SELECT SUM(amount) FROM investments WHERE userId = :userId")
    suspend fun getTotalInvestedAmount(userId: String): Double?

    @Query("SELECT SUM(currentValue) FROM investments WHERE userId = :userId")
    suspend fun getTotalCurrentValue(userId: String): Double?

    @Query("UPDATE investments SET status = :status, currentValue = :currentValue, realizedReturn = :realizedReturn, performancePercentage = :performancePercentage, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateInvestmentPerformance(
        id: String,
        status: String,
        currentValue: Double,
        realizedReturn: Double,
        performancePercentage: Double,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE investments SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateInvestmentStatus(
        id: String,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM investments ORDER BY createdAt DESC")
    fun observeAllInvestments(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments ORDER BY createdAt DESC")
    suspend fun getAllInvestments(): List<InvestmentEntity>

    @Query("SELECT COUNT(*) FROM investments")
    suspend fun getTotalInvestmentCount(): Int

    @Query("SELECT COUNT(*) FROM investments WHERE status = 'ACTIVE'")
    suspend fun getGlobalActiveInvestmentCount(): Int

    @Query("SELECT COUNT(*) FROM investments WHERE status = 'ACTIVE'")
    fun observeGlobalActiveInvestmentCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM investments WHERE status = 'ACTIVE'")
    suspend fun getGlobalTotalActiveInvestedAmount(): Double?
}

