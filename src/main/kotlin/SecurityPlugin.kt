package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

data class TokenConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val expiresIn: Long
)

class JwtService(private val config: TokenConfig) {

    fun generateToken(userId: Long, email: String, role: String): String {
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))
            .sign(Algorithm.HMAC256(config.secret))
    }

    fun configureJwtAuth(application: Application) {
        application.authentication {
            jwt {
                verifier(
                    JWT.require(Algorithm.HMAC256(config.secret))
                        .withAudience(config.audience)
                        .withIssuer(config.issuer)
                        .build()
                )
                validate { credential ->
                    val userId = credential.payload.getClaim("userId").asLong()
                    val email = credential.payload.getClaim("email").asString()
                    val role = credential.payload.getClaim("role").asString()

                    if (userId != null && email != null) {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }
    }
}

fun Application.configureSecurity() {
    val tokenConfig = TokenConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = environment.config.property("jwt.expiresIn").getString().toLong()
    )

    val jwtService = JwtService(tokenConfig)
    jwtService.configureJwtAuth(this)
}