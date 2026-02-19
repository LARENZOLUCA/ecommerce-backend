package com.example.unit

import com.example.model.Order
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrderTest {

    @Test
    fun `test order creation`() {
        val order = Order(
            userId = 1,
            totalAmount = BigDecimal("199.98")
        )

        assertEquals(1, order.userId)
        assertEquals("PENDING", order.status)  // Строка вместо OrderStatus.PENDING
        assertEquals(BigDecimal("199.98"), order.totalAmount)
    }

    @Test
    fun `test order cancellation`() {
        // Создаем заказ со статусом PENDING
        val order = Order(
            userId = 1,
            status = "PENDING",
            totalAmount = BigDecimal("199.98")
        )

        // Вызываем метод cancel() (нужно добавить в Order.kt)
        val cancelled = order.cancel()
        assertEquals("CANCELLED", cancelled.status)
    }

    @Test
    fun `test cannot cancel non-pending order`() {
        val order = Order(
            userId = 1,
            totalAmount = BigDecimal("199.98"),
            status = "CONFIRMED"  // Не PENDING
        )

        // Ожидаем исключение при попытке отмены
        assertFailsWith<IllegalArgumentException> {
            order.cancel()
        }
    }
}