package com.example.repository

import com.example.model.Order
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.*
import java.time.Instant

class OrderRepository {

    fun create(order: Order): Order {
        return transaction {
            val id = OrderTable.insert {
                it[OrderTable.userId] = order.userId
                it[OrderTable.status] = order.status  // Теперь строка
                it[OrderTable.totalAmount] = order.totalAmount
                it[OrderTable.createdAt] = order.createdAt
                it[OrderTable.updatedAt] = order.updatedAt
            } get OrderTable.id

            order.copy(id = id)
        }
    }

    fun findById(id: Long): Order? {
        return transaction {
            OrderTable.select { OrderTable.id eq id }
                .map { row ->
                    Order(
                        id = row[OrderTable.id],
                        userId = row[OrderTable.userId],
                        status = row[OrderTable.status],  // Строка
                        totalAmount = row[OrderTable.totalAmount],
                        createdAt = row[OrderTable.createdAt],
                        updatedAt = row[OrderTable.updatedAt]
                    )
                }
                .singleOrNull()
        }
    }

    fun findByUserId(userId: Long): List<Order> {
        return transaction {
            OrderTable.select { OrderTable.userId eq userId }
                .orderBy(OrderTable.createdAt to SortOrder.DESC)
                .map { row ->
                    Order(
                        id = row[OrderTable.id],
                        userId = row[OrderTable.userId],
                        status = row[OrderTable.status],  // Строка
                        totalAmount = row[OrderTable.totalAmount],
                        createdAt = row[OrderTable.createdAt],
                        updatedAt = row[OrderTable.updatedAt]
                    )
                }
        }
    }

    fun updateStatus(id: Long, status: String): Boolean {  // Строка вместо OrderStatus
        return transaction {
            OrderTable.update({ OrderTable.id eq id }) {
                it[OrderTable.status] = status
                it[OrderTable.updatedAt] = Instant.now()
            } > 0
        }
    }
}