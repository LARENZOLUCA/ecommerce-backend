package com.example.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UserTable : Table("users") {
    val id = long("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val role = varchar("role", 20).default("USER")
    val createdAt = datetime("created_at")  // Exposed хранит как LocalDateTime
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}