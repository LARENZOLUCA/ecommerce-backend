package com.example.config

import com.rabbitmq.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

object RabbitMQConfig {
    private lateinit var connection: Connection
    lateinit var channel: Channel
        private set

    fun init(host: String, port: Int, username: String, password: String) {
        val factory = ConnectionFactory().apply {
            this.host = host
            this.port = port
            this.username = username
            this.password = password
            this.isAutomaticRecoveryEnabled = true  // Исправлено
            this.networkRecoveryInterval = 10000
        }

        connection = factory.newConnection()
        channel = connection.createChannel()

        // Объявляем очередь (durable = true - сохраняется при перезапуске)
        channel.queueDeclare("order-events", true, false, false, null)

        println("✅ RabbitMQ инициализирован на $host:$port")
    }

    fun publishMessage(queueName: String, message: String) {
        try {
            channel.basicPublish("", queueName, null, message.toByteArray(Charsets.UTF_8))
            println("📤 Сообщение отправлено в очередь $queueName: $message")
        } catch (e: IOException) {
            println("❌ Ошибка отправки сообщения: ${e.message}")
        }
    }

    fun startConsumer(queueName: String, onMessageReceived: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Создаем Consumer правильно
                val consumer = object : DefaultConsumer(channel) {
                    override fun handleDelivery(
                        consumerTag: String,
                        envelope: Envelope,
                        properties: AMQP.BasicProperties,
                        body: ByteArray
                    ) {
                        val message = String(body, Charsets.UTF_8)
                        println("📥 Получено сообщение из очереди $queueName: $message")
                        onMessageReceived(message)
                    }
                }

                channel.basicConsume(queueName, true, consumer)
            } catch (e: Exception) {
                println("❌ Ошибка в consumer: ${e.message}")
            }
        }
    }

    fun close() {
        try {
            channel.close()
            connection.close()
            println("🔌 RabbitMQ соединение закрыто")
        } catch (e: Exception) {
            println("❌ Ошибка при закрытии RabbitMQ: ${e.message}")
        }
    }
}