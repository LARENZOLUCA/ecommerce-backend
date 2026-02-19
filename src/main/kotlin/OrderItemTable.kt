package com.example.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal

object OrderItemTable : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = long("order_id").references(OrderTable.id)
    val productId = long("product_id").references(ProductTable.id)
    val quantity = integer("quantity")
    val price = decimal("price", 10, 2)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}