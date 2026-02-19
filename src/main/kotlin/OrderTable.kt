package com.example.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal

object OrderTable : Table("orders") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UserTable.id)
    val status = varchar("status", 20).default("PENDING")  // varchar, НЕ enumeration!
    val totalAmount = decimal("total_amount", 10, 2).default(BigDecimal.ZERO)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}