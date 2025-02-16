package com.lts360.app.database.daos.notification

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lts360.app.database.models.notification.Notification
import kotlinx.coroutines.flow.Flow


@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(notification: Notification)

    @Query("SELECT * FROM notifications")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT count(*) FROM notifications WHERE status='un_read'")
    fun countAllUnreadNotifications(): Flow<Int>

    // Mark all notifications as read
    @Query("UPDATE notifications SET status = 'read'")
    suspend fun markAllAsRead()

    // Mark a specific notification as read by ID
    @Query("UPDATE notifications SET status = 'read' WHERE id = :notificationId")
    suspend fun markAsReadByNotificationId(notificationId: Int)

    // Delete a notification by ID
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: Int)


}

