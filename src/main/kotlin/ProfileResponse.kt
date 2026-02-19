package com.example

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val userId: Long,
    val email: String,
    val role: String
)