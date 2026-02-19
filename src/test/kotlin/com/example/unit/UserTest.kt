package com.example.unit

import com.example.model.User
import com.example.model.UserRole
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserTest {

    @Test
    fun `test user creation`() {
        val user = User.create(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.USER
        )

        assertNotNull(user.email)
        assertEquals("test@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals(UserRole.USER, user.role)
        assertNotNull(user.passwordHash)
        assertTrue(user.passwordHash.length > 20)
    }

    @Test
    fun `test password verification`() {
        val user = User.create(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        assertTrue(user.verifyPassword("password123"))
    }
}