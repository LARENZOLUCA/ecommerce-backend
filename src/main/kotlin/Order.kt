package com.example.model

import java.math.BigDecimal
import java.time.Instant

data class Order(
    val id: Long? = null,
    val userId: Long,
    val status: String = "PENDING",
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun cancel(): Order {
        require(status == "PENDING") { "Only pending orders can be cancelled" }
        return copy(status = "CANCELLED", updatedAt = Instant.now())
    }
}

data class OrderItem(
    val id: Long? = null,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val price: BigDecimal,
    val createdAt: Instant = Instant.now()
) {
    val subtotal: BigDecimal
        get() = price.multiply(BigDecimal(quantity))
}