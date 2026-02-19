package com.example.repository

import com.example.model.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.javatime.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq  // Добавить этот импорт!
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ProductRepository {

    fun create(product: Product): Product {
        return transaction {
            val id = ProductTable.insert {
                it[ProductTable.name] = product.name
                it[ProductTable.description] = product.description
                it[ProductTable.price] = product.price
                it[ProductTable.stock] = product.stock
                it[ProductTable.createdAt] = product.createdAt
                it[ProductTable.updatedAt] = product.updatedAt
            } get ProductTable.id

            product.copy(id = id)
        }
    }

    fun findById(id: Long): Product? {
        return transaction {
            ProductTable.select { ProductTable.id eq id }
                .map { row ->
                    Product(
                        id = row[ProductTable.id],
                        name = row[ProductTable.name],
                        description = row[ProductTable.description],
                        price = row[ProductTable.price],
                        stock = row[ProductTable.stock],
                        createdAt = row[ProductTable.createdAt],
                        updatedAt = row[ProductTable.updatedAt]
                    )
                }
                .singleOrNull()
        }
    }

    fun findAll(): List<Product> {
        return transaction {
            ProductTable.selectAll()
                .orderBy(ProductTable.createdAt to SortOrder.DESC)
                .map { row ->
                    Product(
                        id = row[ProductTable.id],
                        name = row[ProductTable.name],
                        description = row[ProductTable.description],
                        price = row[ProductTable.price],
                        stock = row[ProductTable.stock],
                        createdAt = row[ProductTable.createdAt],
                        updatedAt = row[ProductTable.updatedAt]
                    )
                }
        }
    }

    fun update(product: Product): Product {
        return transaction {
            ProductTable.update({ ProductTable.id eq product.id!! }) {
                it[ProductTable.name] = product.name
                it[ProductTable.description] = product.description
                it[ProductTable.price] = product.price
                it[ProductTable.stock] = product.stock
                it[ProductTable.updatedAt] = product.updatedAt
            }
            product
        }
    }

    fun delete(id: Long): Boolean {
        return transaction {
            ProductTable.deleteWhere { ProductTable.id eq id } > 0
        }
    }

    fun decreaseStock(productId: Long, quantity: Int): Boolean {
        return transaction {
            val currentStock = ProductTable.slice(ProductTable.stock)
                .select { ProductTable.id eq productId }
                .map { it[ProductTable.stock] }
                .firstOrNull()

            if (currentStock == null || currentStock < quantity) {
                return@transaction false
            }

            ProductTable.update({ ProductTable.id eq productId }) {
                with(SqlExpressionBuilder) {
                    it.update(ProductTable.stock, ProductTable.stock - quantity)
                }
                it[ProductTable.updatedAt] = Instant.now()
            } > 0
        }
    }
}