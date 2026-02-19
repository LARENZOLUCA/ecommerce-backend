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
)