package com.example.model

import java.math.BigDecimal
import java.time.Instant

data class Product(
    val id: Long? = null,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val stock: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun decreaseStock(quantity: Int): Product {
        require(quantity > 0) { "Quantity must be positive" }
        require(stock >= quantity) { "Insufficient stock. Available: $stock, requested: $quantity" }
        return copy(stock = stock - quantity, updatedAt = Instant.now())
    }
}