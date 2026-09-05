package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications WHERE userId = :userId OR (recipientRole = 'INVESTOR' AND userId = 'ALL') ORDER BY createdAt DESC")
    fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId OR (recipientRole = 'INVESTOR' AND userId = 'ALL') ORDER BY createdAt DESC")
    suspend fun getNotificationsForUser(userId: String): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE (userId = :userId OR (recipientRole = 'INVESTOR' AND userId = 'ALL')) AND isRead = 0")
    fun observeUnreadCountForUser(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE (userId = :userId OR (recipientRole = 'INVESTOR' AND userId = 'ALL')) AND isRead = 0")
    suspend fun getUnreadCountForUser(userId: String): Int

    @Query("SELECT * FROM notifications WHERE recipientRole = 'ADMIN' OR userId = 'ADMIN' ORDER BY createdAt DESC")
    fun observeAdminNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE recipientRole = 'ADMIN' OR userId = 'ADMIN' ORDER BY createdAt DESC")
    suspend fun getAdminNotifications(): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE (recipientRole = 'ADMIN' OR userId = 'ADMIN') AND isRead = 0")
    fun observeAdminUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE (recipientRole = 'ADMIN' OR userId = 'ADMIN') AND isRead = 0")
    suspend fun getAdminUnreadCount(): Int

    @Query("SELECT * FROM notifications WHERE notificationId = :id LIMIT 1")
    suspend fun getNotificationById(id: String): NotificationEntity?

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR (recipientRole = 'INVESTOR' AND userId = 'ALL')")
    suspend fun markAllAsReadForUser(userId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientRole = 'ADMIN' OR userId = 'ADMIN'")
    suspend fun markAllAsReadForAdmin()

    @Query("DELETE FROM notifications WHERE notificationId = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
