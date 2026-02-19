package com.example.dto

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class OrderEvent(
    val eventId: String,
    val eventType: String,
    val orderId: Long,
    val userId: Long,
    val totalAmount: Double,
    val items: List<OrderItemEvent>,
    val timestamp: String = Instant.now().toString()
)

@Serializable
data class OrderItemEvent(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Double
)