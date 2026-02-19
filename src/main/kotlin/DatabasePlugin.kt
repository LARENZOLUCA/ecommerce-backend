package com.example.plugins

import com.example.config.DatabaseConfig
import com.example.config.RedisConfig
import com.example.config.RabbitMQConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private lateinit var dataSource: DataSource
private val logger = LoggerFactory.getLogger("DatabasePlugin")

fun Application.configureDatabase() {
    val url = environment.config.property("database.url").getString()
    val user = environment.config.property("database.user").getString()
    val password = environment.config.property("database.password").getString()
    val maxPoolSize = environment.config.property("database.maxPoolSize").getString().toInt()

    // Создаем DataSource через HikariCP
    dataSource = DatabaseConfig.createDataSource(url, user, password, maxPoolSize)
    Database.connect(dataSource)
    DatabaseConfig.runMigrations(dataSource)

    // Инициализируем Redis
    try {
        val redisHost = environment.config.property("redis.host").getString()
        val redisPort = environment.config.property("redis.port").getString().toInt()
        RedisConfig.init(redisHost, redisPort)
        logger.info("✅ Redis инициализирован на $redisHost:$redisPort")
    } catch (e: Exception) {
        logger.error("⚠️ Redis не инициализирован: ${e.message}")
    }

    // Инициализируем RabbitMQ
    try {
        val rabbitHost = environment.config.property("rabbitmq.host").getString()
        val rabbitPort = environment.config.property("rabbitmq.port").getString().toInt()
        val rabbitUser = environment.config.property("rabbitmq.username").getString()
        val rabbitPass = environment.config.property("rabbitmq.password").getString()
        RabbitMQConfig.init(rabbitHost, rabbitPort, rabbitUser, rabbitPass)
        logger.info("✅ RabbitMQ инициализирован на $rabbitHost:$rabbitPort")

        // Запускаем consumer для обработки сообщений
        RabbitMQConfig.startConsumer("order-events") { message ->
            logger.info("📦 Обработка сообщения: $message")
            // Здесь будет логика обработки заказов
            // Например, отправка email, запись в лог и т.д.
        }

    } catch (e: Exception) {
        logger.error("⚠️ RabbitMQ не инициализирован: ${e.message}")
    }

    environment.monitor.subscribe(ApplicationStopped) {
        if (::dataSource.isInitialized && dataSource is HikariDataSource) {
            (dataSource as HikariDataSource).close()
        }
        try {
            RedisConfig.close()
        } catch (e: Exception) {
            // Игнорируем ошибки закрытия Redis
        }
        try {
            RabbitMQConfig.close()
        } catch (e: Exception) {
            // Игнорируем ошибки закрытия RabbitMQ
        }
    }
}

fun getDataSource(): DataSource = dataSource