package com.lts360.app.notifications

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


object NotificationIdManager {
    private val userCounter = AtomicInteger(0)
    private val userNotificationMap = ConcurrentHashMap<Long, Int>()

    // Get or create unique ID for user-specific notifications
    fun getNotificationId(userId: Long): Int {
        return userNotificationMap.computeIfAbsent(userId) {
            userCounter.incrementAndGet()
        }
    }
}
