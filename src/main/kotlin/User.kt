package com.example.model

import at.favre.lib.crypto.bcrypt.BCrypt
import java.time.Instant

enum class UserRole {
    USER,
    ADMIN
}

data class User(
    val id: Long? = null,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole = UserRole.USER,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun verifyPassword(password: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified
    }

    companion object {
        fun create(email: String, password: String, firstName: String, lastName: String, role: UserRole = UserRole.USER): User {
            val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            return User(
                email = email,
                passwordHash = hash,
                firstName = firstName,
                lastName = lastName,
                role = role
            )
        }
    }
}