package com.example.dto

import com.example.model.Order
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Позиция в заказе")
data class OrderItemRequest(
    @Schema(description = "ID товара", example = "1")
    val productId: Long,

    @Schema(description = "Количество", example = "2")
    val quantity: Int
)

@Serializable
@Schema(description = "Запрос на создание заказа")
data class CreateOrderRequest(
    @Schema(description = "Список товаров в заказе")
    val items: List<OrderItemRequest>
)

@Serializable
@Schema(description = "Позиция заказа в ответе")
data class OrderItemResponse(
    @Schema(description = "ID товара", example = "1")
    val productId: Long,

    @Schema(description = "Название товара", example = "Ноутбук")
    val productName: String,

    @Schema(description = "Количество", example = "2")
    val quantity: Int,

    @Schema(description = "Цена за единицу", example = "999.99")
    val price: Double,

    @Schema(description = "Общая стоимость позиции", example = "1999.98")
    val subtotal: Double
)

@Serializable
@Schema(description = "Ответ с данными заказа")
data class OrderResponse(
    @Schema(description = "ID заказа", example = "1")
    val id: Long,

    @Schema(description = "Статус заказа", example = "PENDING",
        allowableValues = ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"])
    val status: String,

    @Schema(description = "Общая сумма заказа", example = "1999.98")
    val totalAmount: Double,

    @Schema(description = "Список товаров в заказе")
    val items: List<OrderItemResponse>,

    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    val createdAt: String
) {
    companion object {
        fun fromOrder(order: Order, items: List<OrderItemResponse>): OrderResponse {
            return OrderResponse(
                id = order.id!!,
                status = order.status,
                totalAmount = order.totalAmount.toDouble(),
                items = items,
                createdAt = order.createdAt.toString()
            )
        }
    }
}