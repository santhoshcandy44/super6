package com.lts360.app.notifications

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


object NotificationIdManager {
    private val counter = AtomicInteger(0)
    private val senderChatNotificationMap = ConcurrentHashMap<Long, Int>()

    fun getNotificationIdForChatNotification(senderId: Long): Int {
        return senderChatNotificationMap.computeIfAbsent(senderId) {
            counter.incrementAndGet()
        }
    }

    fun getNotificationId() =  counter.incrementAndGet()
}
