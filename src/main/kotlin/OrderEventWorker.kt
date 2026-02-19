package com.example.worker

import com.example.config.RabbitMQConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

object OrderEventWorker {
    private val logger = LoggerFactory.getLogger(OrderEventWorker::class.java)

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info("🚀 Запуск OrderEventWorker для обработки сообщений...")

            // Запускаем consumer
            RabbitMQConfig.startConsumer("order-events") { message ->
                // Запускаем обработку в корутине
                CoroutineScope(Dispatchers.IO).launch {
                    processMessage(message)
                }
            }
        }
    }

    private suspend fun processMessage(message: String) {
        try {
            logger.info("📦 Обработка сообщения из очереди: $message")

            // Эмуляция отправки email
            sendFakeEmail(message)

            // Логирование в консоль
            logEvent(message)

        } catch (e: Exception) {
            logger.error("❌ Ошибка при обработке сообщения: ${e.message}")
        }
    }

    private suspend fun sendFakeEmail(message: String) {
        // Имитация отправки email
        logger.info("📧 Отправка email-уведомления о заказе...")
        // Здесь могла бы быть реальная отправка email
        delay(500) // Имитация задержки
        logger.info("✅ Email успешно отправлен")
    }

    private fun logEvent(message: String) {
        // Запись в лог (вместо audit_logs)
        logger.info("📝 Событие записано в лог: $message")
    }
}