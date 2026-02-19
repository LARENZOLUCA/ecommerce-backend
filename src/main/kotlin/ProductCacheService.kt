package com.example.service

import com.example.config.RedisConfig
import com.example.dto.ProductResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.SerializationException

class ProductCacheService {
    private val json = Json { ignoreUnknownKeys = true }
    private val productsCacheKey = "products:all"
    private val productPrefix = "product:"

    suspend fun cacheAllProducts(products: List<ProductResponse>) {
        try {
            println("🔵 Кэшируем список товаров: ${products.size} товаров")
            val jsonString = json.encodeToString(products)
            println("📦 JSON размер: ${jsonString.length} байт")
            RedisConfig.set(productsCacheKey, jsonString, 300)
            println("✅ Список товаров сохранён в Redis")
        } catch (e: SerializationException) {
            println("❌ Ошибка сериализации: ${e.message}")
        } catch (e: Exception) {
            println("❌ Ошибка при кэшировании: ${e.message}")
        }
    }

    suspend fun getCachedAllProducts(): List<ProductResponse>? {
        return try {
            println("🔵 Проверяем кэш для списка товаров")
            val jsonString = RedisConfig.get(productsCacheKey)
            if (jsonString != null) {
                println("✅ Данные найдены в кэше")
                json.decodeFromString<List<ProductResponse>>(jsonString)
            } else {
                println("❌ Данных нет в кэше")
                null
            }
        } catch (e: Exception) {
            println("❌ Ошибка при чтении из кэша: ${e.message}")
            null
        }
    }

    suspend fun cacheProduct(product: ProductResponse) {
        try {
            println("🔵 Кэшируем товар ID: ${product.id}")
            val jsonString = json.encodeToString(product)
            RedisConfig.set("$productPrefix${product.id}", jsonString, 300)
            println("✅ Товар сохранён в Redis")
        } catch (e: SerializationException) {
            println("❌ Ошибка сериализации товара: ${e.message}")
        } catch (e: Exception) {
            println("❌ Ошибка при кэшировании товара: ${e.message}")
        }
    }

    suspend fun getCachedProduct(id: Long): ProductResponse? {
        return try {
            println("🔵 Проверяем кэш для товара ID: $id")
            val jsonString = RedisConfig.get("$productPrefix$id")
            if (jsonString != null) {
                println("✅ Товар найден в кэше")
                json.decodeFromString<ProductResponse>(jsonString)
            } else {
                println("❌ Товара нет в кэше")
                null
            }
        } catch (e: Exception) {
            println("❌ Ошибка при чтении товара из кэша: ${e.message}")
            null
        }
    }

    suspend fun invalidateAllProducts() {
        println("🔵 Инвалидируем общий кэш")
        RedisConfig.delete(productsCacheKey)
    }

    suspend fun invalidateProduct(id: Long) {
        println("🔵 Инвалидируем кэш товара ID: $id")
        RedisConfig.delete("$productPrefix$id")
        invalidateAllProducts()
    }
}