package com.super6.pot.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.super6.pot.app.database.daos.notification.NotificationDao
import com.super6.pot.app.database.models.notification.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class NotificationViewModel @Inject constructor (private val notificationDao: NotificationDao) : ViewModel() {


    private val _selectedItem = MutableStateFlow<Notification?>(null)
    val selectedItem: StateFlow<Notification?> get() = _selectedItem

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    init {

        viewModelScope.launch(Dispatchers.IO){
            // Fetch all notifications and emit them to the StateFlow
            notificationDao.getAllNotifications().collectLatest { notificationList ->
                _isLoading.value = false // Data is loaded, set loading to false
                _notifications.value = notificationList
            }
        }
    }




    fun markAllAsRead() {
        // Mark all as read
        viewModelScope.launch {
            notificationDao.markAllAsRead()

        }
    }


    fun markAsRead(notification: Notification) {
        // Mark all as read
        viewModelScope.launch {
            notificationDao.markAsReadByNotificationId(notification.id)

        }
    }


    fun deleteNotification(notification: Notification) {
        // Mark all as read
        viewModelScope.launch {
            notificationDao.deleteNotificationById(notification.id)
        }
    }



    fun setSelectedItem(item: Notification) {
        _selectedItem.value = item
    }




    companion object {

        fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val weeks = days / 7
            val months = days / 30
            val years = days / 365

            return when {
                seconds < 60 -> if (seconds <= 1) "Just now" else "$seconds seconds ago"
                minutes < 60 -> if (minutes <= 1) "1 minute ago" else "$minutes minutes ago"
                hours < 24 -> if (hours <= 1) "1 hour ago" else "$hours hours ago"
                days < 7 -> if (days <= 1) "1 day ago" else "$days days ago"
                weeks < 4 -> if (weeks <= 1) "1 week ago" else "$weeks weeks ago"
                months < 12 -> if (months <= 1) "1 month ago" else "$months months ago"
                else -> if (years <= 1) "1 year ago" else "$years years ago"
            }
        }
    }


}
