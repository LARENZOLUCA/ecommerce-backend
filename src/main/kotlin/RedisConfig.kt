package com.example.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

object RedisConfig {
    private lateinit var redisClient: RedisClient
    private lateinit var connection: RedisCoroutinesCommands<String, String>

    fun init(host: String = "localhost", port: Int = 6379, password: String? = null) {
        val uriBuilder = RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withTimeout(Duration.ofSeconds(5))

        password?.let { uriBuilder.withPassword(it) }

        redisClient = RedisClient.create(uriBuilder.build())
        connection = redisClient.connect().coroutines()
    }

    suspend fun set(key: String, value: String, ttlSeconds: Long = 3600) {
        withContext(Dispatchers.IO) {
            connection.setex(key, ttlSeconds, value)
        }
    }

    suspend fun get(key: String): String? {
        return withContext(Dispatchers.IO) {
            connection.get(key)
        }
    }

    suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            connection.del(key)
        }
    }

    fun close() {
        if (::redisClient.isInitialized) {
            redisClient.shutdown()
        }
    }
}