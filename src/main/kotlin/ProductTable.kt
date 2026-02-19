package com.example.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal

object ProductTable : Table("products") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255)
    val description = varchar("description", 1000)
    val price = decimal("price", 10, 2)
    val stock = integer("stock").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}