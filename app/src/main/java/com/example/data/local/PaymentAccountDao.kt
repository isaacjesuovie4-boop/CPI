package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentAccountDao {

    @Query("SELECT * FROM payment_accounts ORDER BY createdAt DESC")
    fun observeAllPaymentAccounts(): Flow<List<PaymentAccountEntity>>

    @Query("SELECT * FROM payment_accounts WHERE currency = :currency ORDER BY createdAt DESC")
    fun observePaymentAccountsByCurrency(currency: String): Flow<List<PaymentAccountEntity>>

    @Query("SELECT * FROM payment_accounts WHERE currency = :currency AND isPublished = 1 AND isActive = 1")
    fun observePublishedPaymentAccounts(currency: String): Flow<List<PaymentAccountEntity>>

    @Query("SELECT * FROM payment_accounts WHERE id = :id")
    suspend fun getPaymentAccountById(id: String): PaymentAccountEntity?

    @Query("SELECT * FROM payment_accounts WHERE currency = :currency AND isPublished = 1 AND isActive = 1")
    suspend fun getPublishedAccountsSync(currency: String): List<PaymentAccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentAccount(account: PaymentAccountEntity)

    @Update
    suspend fun updatePaymentAccount(account: PaymentAccountEntity)

    @Delete
    suspend fun deletePaymentAccount(account: PaymentAccountEntity)

    @Query("SELECT COUNT(*) FROM payment_accounts")
    suspend fun getPaymentAccountsCount(): Int

    @Query("UPDATE payment_accounts SET isPublished = :isPublished, publishedAt = :publishedAt, expiresAt = :expiresAt, lastPublishedBy = :adminEmail, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePublishStatus(
        id: String,
        isPublished: Boolean,
        publishedAt: Long?,
        expiresAt: Long?,
        adminEmail: String,
        updatedAt: Long
    )

    @Query("UPDATE payment_accounts SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean, updatedAt: Long)
}
