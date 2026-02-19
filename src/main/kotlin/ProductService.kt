package com.example.service

import com.example.dto.ProductRequest
import com.example.dto.ProductResponse
import com.example.dto.ProductUpdateRequest
import com.example.model.Product
import com.example.repository.ProductRepository
import java.time.Instant
import java.math.BigDecimal

class ProductService(
    private val productRepository: ProductRepository,
    private val cacheService: ProductCacheService = ProductCacheService()
) {

    suspend fun createProduct(request: ProductRequest): ProductResponse {
        val product = request.toProduct()
        val saved = productRepository.create(product)
        val response = ProductResponse.fromProduct(saved)

        // Инвалидируем кэш при создании (с защитой от ошибок)
        try {
            cacheService.invalidateAllProducts()
        } catch (e: Exception) {
            println("⚠️ Ошибка при инвалидации кэша: ${e.message}")
        }

        return response
    }

    suspend fun getProduct(id: Long): ProductResponse? {
        // Сначала проверяем кэш (с защитой от ошибок)
        try {
            cacheService.getCachedProduct(id)?.let {
                return it
            }
        } catch (e: Exception) {
            println("⚠️ Ошибка при чтении из кэша: ${e.message}")
        }

        // Если нет в кэше или ошибка - ищем в БД
        val product = productRepository.findById(id)
        val response = product?.let { ProductResponse.fromProduct(it) }

        // Сохраняем в кэш (с защитой от ошибок)
        if (response != null) {
            try {
                cacheService.cacheProduct(response)
            } catch (e: Exception) {
                println("⚠️ Ошибка при сохранении в кэш: ${e.message}")
            }
        }

        return response
    }

    suspend fun getAllProducts(): List<ProductResponse> {
        // Сначала проверяем кэш
        cacheService.getCachedAllProducts()?.let {
            return it
        }

        val products = productRepository.findAll()
            .map { ProductResponse.fromProduct(it) }

        // Сохраняем в кэш
        cacheService.cacheAllProducts(products)

        return products
    }

    suspend fun updateProduct(id: Long, request: ProductUpdateRequest): ProductResponse? {
        val existing = productRepository.findById(id) ?: return null

        val price = request.price?.let { BigDecimal(it.toString()) }

        val updated = Product(
            id = id,
            name = request.name ?: existing.name,
            description = request.description ?: existing.description,
            price = price ?: existing.price,
            stock = request.stock ?: existing.stock,
            createdAt = existing.createdAt,
            updatedAt = Instant.now()
        )

        val saved = productRepository.update(updated)
        val response = ProductResponse.fromProduct(saved)

        // Инвалидируем кэш при обновлении (с защитой от ошибок)
        try {
            cacheService.invalidateProduct(id)
        } catch (e: Exception) {
            println("⚠️ Ошибка при инвалидации кэша: ${e.message}")
        }

        return response
    }

    suspend fun deleteProduct(id: Long): Boolean {
        val deleted = productRepository.delete(id)
        if (deleted) {
            // Инвалидируем кэш при удалении (с защитой от ошибок)
            try {
                cacheService.invalidateProduct(id)
            } catch (e: Exception) {
                println("⚠️ Ошибка при инвалидации кэша: ${e.message}")
            }
        }
        return deleted
    }
}