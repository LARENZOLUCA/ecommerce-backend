package com.example.integration

import com.example.model.User
import com.example.model.UserRole
import com.example.repository.UserRepository
import com.example.repository.UserTable
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Testcontainers
class UserRepositoryIntegrationTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        private lateinit var dataSource: HikariDataSource
        private lateinit var userRepository: UserRepository

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
                SchemaUtils.create(UserTable)
            }

            userRepository = UserRepository()
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
            UserTable.deleteAll()
        }
    }

    @Test
    fun `test create and find user by email`() = runBlocking {
        val user = User.create(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.USER
        )

        val saved = userRepository.create(user)
        assertNotNull(saved.id)

        val found = userRepository.findByEmail("test@example.com")
        assertNotNull(found)
        assertEquals("test@example.com", found.email)
        assertEquals("John", found.firstName)
        assertEquals("Doe", found.lastName)
        assertEquals(UserRole.USER, found.role)
    }

    @Test
    fun `test find non-existent user returns null`() = runBlocking {
        val found = userRepository.findByEmail("nonexistent@example.com")
        assertNull(found)
    }

    @Test
    fun `test create user with duplicate email throws exception`() = runBlocking {
        val user1 = User.create(
            email = "duplicate@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        userRepository.create(user1)

        val user2 = User.create(
            email = "duplicate@example.com",
            password = "password456",
            firstName = "Jane",
            lastName = "Smith"
        )

        // Явно указываем, что мы ожидаем исключение
        try {
            userRepository.create(user2)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: org.jetbrains.exposed.exceptions.ExposedSQLException) {
            // Ожидаемое исключение - тест пройден
            println("Caught expected exception: ${e.message}")
        }
    }
}