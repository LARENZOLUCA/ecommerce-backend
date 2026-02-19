package com.example.repository

import com.example.model.OrderItem  // Импорт из model, где теперь всё в одном файле
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.*

class OrderItemRepository {

    fun create(orderItem: OrderItem): OrderItem {
        return transaction {
            val id = OrderItemTable.insert {
                it[OrderItemTable.orderId] = orderItem.orderId
                it[OrderItemTable.productId] = orderItem.productId
                it[OrderItemTable.quantity] = orderItem.quantity
                it[OrderItemTable.price] = orderItem.price
                it[OrderItemTable.createdAt] = orderItem.createdAt
            } get OrderItemTable.id

            orderItem.copy(id = id)
        }
    }

    fun createAll(orderItems: List<OrderItem>): List<OrderItem> {
        return transaction {
            orderItems.map { orderItem ->
                val id = OrderItemTable.insert {
                    it[OrderItemTable.orderId] = orderItem.orderId
                    it[OrderItemTable.productId] = orderItem.productId
                    it[OrderItemTable.quantity] = orderItem.quantity
                    it[OrderItemTable.price] = orderItem.price
                    it[OrderItemTable.createdAt] = orderItem.createdAt
                } get OrderItemTable.id
                orderItem.copy(id = id)
            }
        }
    }

    fun findByOrderId(orderId: Long): List<OrderItem> {
        return transaction {
            OrderItemTable.select { OrderItemTable.orderId eq orderId }
                .map { row ->
                    OrderItem(
                        id = row[OrderItemTable.id],
                        orderId = row[OrderItemTable.orderId],
                        productId = row[OrderItemTable.productId],
                        quantity = row[OrderItemTable.quantity],
                        price = row[OrderItemTable.price],
                        createdAt = row[OrderItemTable.createdAt]
                    )
                }
        }
    }
}