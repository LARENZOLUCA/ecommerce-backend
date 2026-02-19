package com.example.dto

import com.example.model.Product
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
@Schema(description = "Запрос на создание товара")
data class ProductRequest(
    @Schema(description = "Название товара", example = "Ноутбук")
    val name: String,

    @Schema(description = "Описание товара", example = "Мощный ноутбук для игр и работы")
    val description: String,

    @Schema(description = "Цена товара", example = "999.99")
    val price: Double,

    @Schema(description = "Количество на складе", example = "10")
    val stock: Int
) {
    fun toProduct(): Product {
        return Product(
            name = name,
            description = description,
            price = BigDecimal(price.toString()),
            stock = stock
        )
    }
}

@Serializable
@Schema(description = "Ответ с данными товара")
data class ProductResponse(
    @Schema(description = "ID товара", example = "1")
    val id: Long,

    @Schema(description = "Название товара", example = "Ноутбук")
    val name: String,

    @Schema(description = "Описание товара", example = "Мощный ноутбук для игр и работы")
    val description: String,

    @Schema(description = "Цена товара", example = "999.99")
    val price: Double,

    @Schema(description = "Количество на складе", example = "10")
    val stock: Int,

    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    val createdAt: String,

    @Schema(description = "Дата обновления", example = "2024-01-01T12:00:00Z")
    val updatedAt: String
) {
    companion object {
        fun fromProduct(product: Product): ProductResponse {
            return ProductResponse(
                id = product.id!!,
                name = product.name,
                description = product.description,
                price = product.price.toDouble(),
                stock = product.stock,
                createdAt = product.createdAt.toString(),
                updatedAt = product.updatedAt.toString()
            )
        }
    }
}

@Serializable
@Schema(description = "Запрос на обновление товара")
data class ProductUpdateRequest(
    @Schema(description = "Название товара", example = "Ноутбук", required = false)
    val name: String? = null,

    @Schema(description = "Описание товара", example = "Обновленное описание", required = false)
    val description: String? = null,

    @Schema(description = "Цена товара", example = "899.99", required = false)
    val price: Double? = null,

    @Schema(description = "Количество на складе", example = "15", required = false)
    val stock: Int? = null
)