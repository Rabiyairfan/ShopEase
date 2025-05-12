package com.marketplace.domain.models

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val imageUrl: String = "",
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    ORDER_STATUS,
    PROMOTION,
    SYSTEM,
    GENERAL
}

data class PushNotification(
    val to: String = "",
    val notification: NotificationPayload = NotificationPayload(),
    val data: Map<String, String> = emptyMap()
)

data class NotificationPayload(
    val title: String = "",
    val body: String = "",
    val image: String = ""
) 