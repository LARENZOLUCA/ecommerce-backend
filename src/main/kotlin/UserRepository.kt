package com.example.repository

import com.example.model.User
import com.example.model.UserRole
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class UserRepository {

    suspend fun create(user: User): User {
        return transaction {
            val id = UserTable.insert {
                it[email] = user.email
                it[passwordHash] = user.passwordHash
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[role] = user.role.name
                // Преобразуем Instant в LocalDateTime
                it[createdAt] = LocalDateTime.ofInstant(user.createdAt, ZoneOffset.UTC)
                it[updatedAt] = LocalDateTime.ofInstant(user.updatedAt, ZoneOffset.UTC)
            } get UserTable.id

            user.copy(id = id)
        }
    }

    suspend fun findByEmail(email: String): User? {
        return transaction {
            UserTable.select { UserTable.email eq email }
                .map { toUser(it) }
                .singleOrNull()
        }
    }

    suspend fun findById(id: Long): User? {
        return transaction {
            UserTable.select { UserTable.id eq id }
                .map { toUser(it) }
                .singleOrNull()
        }
    }

    private fun toUser(row: ResultRow): User {
        return User(
            id = row[UserTable.id],
            email = row[UserTable.email],
            passwordHash = row[UserTable.passwordHash],
            firstName = row[UserTable.firstName],
            lastName = row[UserTable.lastName],
            role = UserRole.valueOf(row[UserTable.role]),
            // Преобразуем LocalDateTime обратно в Instant
            createdAt = row[UserTable.createdAt].toInstant(ZoneOffset.UTC),
            updatedAt = row[UserTable.updatedAt].toInstant(ZoneOffset.UTC)
        )
    }
}