package com.example.integration

import com.example.model.Product
import com.example.repository.ProductRepository
import com.example.repository.ProductTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Testcontainers
class ProductRepositoryIntegrationTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        private lateinit var dataSource: HikariDataSource
        private lateinit var productRepository: ProductRepository

        @JvmStatic
        @BeforeAll
        fun setup() {
            val config = HikariConfig().apply {
                jdbcUrl = postgresContainer.jdbcUrl
                username = postgresContainer.username
                password = postgresContainer.password
                maximumPoolSize = 1
                driverClassName = "org.postgresql.Driver"
            }
            dataSource = HikariDataSource(config)
            Database.connect(dataSource)

            transaction {
                SchemaUtils.create(ProductTable)
            }

            productRepository = ProductRepository()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            dataSource.close()
        }
    }

    @BeforeEach
    fun cleanDatabase() {
        transaction {
            ProductTable.deleteAll()
        }
    }

    @Test
    fun `test create and find product by id`() = runBlocking {
        val product = Product(
            name = "Test Product",
            description = "Test Description",
            price = BigDecimal("99.99"),
            stock = 10
        )

        val saved = productRepository.create(product)
        assertNotNull(saved.id)

        val found = productRepository.findById(saved.id!!)
        assertNotNull(found)
        assertEquals("Test Product", found.name)
        assertEquals("Test Description", found.description)
        assertEquals(BigDecimal("99.99"), found.price)
        assertEquals(10, found.stock)
    }

    @Test
    fun `test find all products`() = runBlocking {
        val product1 = Product(
            name = "Product 1",
            description = "Description 1",
            price = BigDecimal("10.99"),
            stock = 5
        )

        val product2 = Product(
            name = "Product 2",
            description = "Description 2",
            price = BigDecimal("20.99"),
            stock = 10
        )

        productRepository.create(product1)
        productRepository.create(product2)

        val products = productRepository.findAll()
        assertEquals(2, products.size)
    }

    @Test
    fun `test update product`() = runBlocking {
        val product = Product(
            name = "Original Name",
            description = "Original Description",
            price = BigDecimal("99.99"),
            stock = 10
        )

        val saved = productRepository.create(product)

        val updated = saved.copy(
            name = "Updated Name",
            price = BigDecimal("89.99"),
            stock = 15
        )

        val result = productRepository.update(updated)

        val found = productRepository.findById(saved.id!!)
        assertNotNull(found)
        assertEquals("Updated Name", found.name)
        assertEquals(BigDecimal("89.99"), found.price)
        assertEquals(15, found.stock)
    }

    @Test
    fun `test delete product`() = runBlocking {
        val product = Product(
            name = "To Delete",
            description = "Will be deleted",
            price = BigDecimal("49.99"),
            stock = 3
        )

        val saved = productRepository.create(product)
        assertNotNull(productRepository.findById(saved.id!!))

        val deleted = productRepository.delete(saved.id!!)
        assertTrue(deleted)

        assertNull(productRepository.findById(saved.id!!))
    }

    @Test
    fun `test decrease stock`() = runBlocking {
        val product = Product(
            name = "Stock Test",
            description = "Testing stock decrease",
            price = BigDecimal("29.99"),
            stock = 10
        )

        val saved = productRepository.create(product)

        val result = productRepository.decreaseStock(saved.id!!, 3)
        assertTrue(result)

        val updated = productRepository.findById(saved.id!!)
        assertEquals(7, updated?.stock)
    }

    @Test
    fun `test decrease stock with insufficient quantity`() = runBlocking {
        val product = Product(
            name = "Stock Test",
            description = "Testing insufficient stock",
            price = BigDecimal("29.99"),
            stock = 2
        )

        val saved = productRepository.create(product)

        val result = productRepository.decreaseStock(saved.id!!, 5)
        assertTrue(!result)

        val updated = productRepository.findById(saved.id!!)
        assertEquals(2, updated?.stock)
    }
}