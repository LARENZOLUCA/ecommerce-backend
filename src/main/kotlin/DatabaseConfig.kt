package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)

    fun createDataSource(
        url: String,
        user: String,
        password: String,
        maxPoolSize: Int = 10
    ): DataSource {
        logger.info("Инициализация подключения к БД: $url")

        val config = HikariConfig().apply {
            jdbcUrl = url
            this.username = user
            this.password = password
            maximumPoolSize = maxPoolSize
            minimumIdle = 2
            connectionTimeout = 30000 // 30 секунд
            idleTimeout = 600000 // 10 минут
            maxLifetime = 1800000 // 30 минут
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            driverClassName = "org.postgresql.Driver"

            // Дополнительные настройки
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

            validate()
        }

        return HikariDataSource(config)
    }

    fun runMigrations(dataSource: DataSource) {
        try {
            logger.info("Запуск Flyway миграций...")

            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()

            val result = flyway.migrate()

            logger.info("Миграции завершены. Применено: ${result.migrationsExecuted}")
            logger.info("Текущая версия схемы: ${result.targetSchemaVersion}")

        } catch (e: Exception) {
            logger.error("Ошибка при выполнении миграций: ${e.message}", e)
            throw e
        }
    }

    fun validateConnection(dataSource: DataSource): Boolean {
        return try {
            (dataSource as HikariDataSource).connection.use { connection ->
                connection.isValid(5)
            }
        } catch (e: Exception) {
            logger.error("Ошибка проверки подключения: ${e.message}")
            false
        }
    }
}