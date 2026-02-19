package com.example.service

import com.example.dto.CreateOrderRequest
import com.example.dto.OrderItemResponse
import com.example.dto.OrderResponse
import com.example.model.Order
import com.example.model.OrderItem
import com.example.repository.OrderItemRepository
import com.example.repository.OrderRepository
import com.example.repository.ProductRepository
import com.example.dto.OrderEvent
import com.example.dto.OrderItemEvent
import com.example.config.RabbitMQConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun createOrder(userId: Long, request: CreateOrderRequest): OrderResponse {
        // ВСЯ ЛОГИКА В ОДНОЙ ТРАНЗАКЦИИ
        return transaction {
            val orderItems = mutableListOf<OrderItemResponse>()
            var totalAmount = BigDecimal.ZERO

            // 1. Проверяем товары и собираем информацию
            request.items.forEach { item ->
                val product = productRepository.findById(item.productId)
                    ?: throw IllegalArgumentException("Product with id ${item.productId} not found")

                if (product.stock < item.quantity) {
                    throw IllegalArgumentException("Insufficient stock for product ${product.name}. Available: ${product.stock}, requested: ${item.quantity}")
                }

                val price = product.price
                val subtotal = price.multiply(BigDecimal(item.quantity))
                totalAmount = totalAmount.add(subtotal)

                orderItems.add(
                    OrderItemResponse(
                        productId = product.id!!,
                        productName = product.name,
                        quantity = item.quantity,
                        price = price.toDouble(),
                        subtotal = subtotal.toDouble()
                    )
                )
            }

            // 2. Создаем заказ
            val order = Order(
                userId = userId,
                status = "PENDING",
                totalAmount = totalAmount,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val savedOrder = orderRepository.create(order)

            // 3. Уменьшаем stock и создаем позиции заказа
            request.items.forEach { item ->
                val product = productRepository.findById(item.productId)!!

                // Уменьшаем количество товара
                productRepository.decreaseStock(item.productId, item.quantity)

                // Создаем позицию заказа
                val orderItem = OrderItem(
                    orderId = savedOrder.id!!,
                    productId = item.productId,
                    quantity = item.quantity,
                    price = product.price,
                    createdAt = Instant.now()
                )
                orderItemRepository.create(orderItem)
            }

            val response = OrderResponse.fromOrder(savedOrder, orderItems)

            // Отправляем событие в RabbitMQ (это вне транзакции)
            sendOrderEvent(savedOrder.id!!, userId, orderItems, totalAmount)

            return@transaction response
        }
    }

    private fun sendOrderEvent(orderId: Long, userId: Long, items: List<OrderItemResponse>, totalAmount: BigDecimal) {
        try {
            val eventItems = items.map {
                OrderItemEvent(
                    productId = it.productId,
                    productName = it.productName,
                    quantity = it.quantity,
                    price = it.price
                )
            }

            val event = OrderEvent(
                eventId = UUID.randomUUID().toString(),
                eventType = "ORDER_CREATED",
                orderId = orderId,
                userId = userId,
                totalAmount = totalAmount.toDouble(),
                items = eventItems
            )

            val message = json.encodeToString(event)
            RabbitMQConfig.publishMessage("order-events", message)
            println("📤 Событие заказа отправлено в RabbitMQ: $message")
        } catch (e: Exception) {
            println("❌ Ошибка при отправке события в RabbitMQ: ${e.message}")
            // Не выбрасываем исключение, так как заказ уже создан
        }
    }

    fun getUserOrders(userId: Long): List<OrderResponse> {
        return transaction {
            val orders = orderRepository.findByUserId(userId)
            orders.map { order ->
                val items = orderItemRepository.findByOrderId(order.id!!)
                val itemResponses = items.map { item ->
                    val product = productRepository.findById(item.productId)!!
                    OrderItemResponse(
                        productId = item.productId,
                        productName = product.name,
                        quantity = item.quantity,
                        price = item.price.toDouble(),
                        subtotal = item.subtotal.toDouble()
                    )
                }
                OrderResponse(
                    id = order.id!!,
                    status = order.status,
                    totalAmount = order.totalAmount.toDouble(),
                    items = itemResponses,
                    createdAt = order.createdAt.toString()
                )
            }
        }
    }

    fun cancelOrder(orderId: Long, userId: Long): Boolean {
        return transaction {
            val order = orderRepository.findById(orderId)
                ?: throw IllegalArgumentException("Order not found")

            if (order.userId != userId) {
                throw IllegalArgumentException("You can only cancel your own orders")
            }

            if (order.status != "PENDING") {
                throw IllegalArgumentException("Only pending orders can be cancelled")
            }

            // Возвращаем товары на склад
            val items = orderItemRepository.findByOrderId(orderId)
            items.forEach { item ->
                productRepository.decreaseStock(item.productId, -item.quantity)
            }

            // Обновляем статус заказа
            orderRepository.updateStatus(orderId, "CANCELLED")
        }
    }
}