package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import com.example.model.User
import com.example.model.UserRole
import com.example.repository.UserRepository
import com.example.RegisterRequest
import com.example.LoginRequest
import com.example.AuthResponse
import com.example.ProfileResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import com.example.dto.ProductRequest
import com.example.dto.ProductResponse
import com.example.dto.ProductUpdateRequest
import com.example.repository.ProductRepository
import com.example.service.ProductService
import com.example.dto.CreateOrderRequest
import com.example.dto.OrderResponse
import com.example.dto.OrderItemResponse
import com.example.repository.OrderRepository
import com.example.repository.OrderItemRepository
import com.example.service.OrderService
import java.math.BigDecimal

private val logger = LoggerFactory.getLogger("Routing")

fun Application.configureRouting() {

    // Создаем зависимости
    val userRepository = UserRepository()
    val productRepository = ProductRepository()
    val productService = ProductService(productRepository)
    val orderRepository = OrderRepository()
    val orderItemRepository = OrderItemRepository()
    val orderService = OrderService(orderRepository, orderItemRepository, productRepository)

    val tokenConfig = TokenConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = environment.config.property("jwt.expiresIn").getString().toLong()
    )
    val jwtService = JwtService(tokenConfig)

    routing {
        get("/") {
            call.respondText("E-commerce API is running!")
        }

        // ТЕСТОВЫЙ МАРШРУТ для диагностики JSON
        post("/test") {
            try {
                val text: String = call.receive()
                logger.info("Test received: $text")
                call.respond(mapOf("received" to text))
            } catch (e: Exception) {
                logger.error("Test error", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Auth routes
        route("/auth") {
            post("/register") {
                try {
                    val body: String = call.receive()
                    val json = Json { ignoreUnknownKeys = true }
                    val request = json.decodeFromString<RegisterRequest>(body)

                    val existingUser = userRepository.findByEmail(request.email)
                    if (existingUser != null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email already registered"))
                        return@post
                    }

                    val user = User.create(
                        email = request.email,
                        password = request.password,
                        firstName = request.firstName,
                        lastName = request.lastName,
                        role = UserRole.USER
                    )

                    val savedUser = userRepository.create(user)

                    val token = jwtService.generateToken(
                        userId = savedUser.id!!,
                        email = savedUser.email,
                        role = savedUser.role.name
                    )

                    call.respond(HttpStatusCode.Created, AuthResponse(
                        token = token,
                        userId = savedUser.id,
                        email = savedUser.email,
                        role = savedUser.role.name
                    ))

                } catch (e: SerializationException) {
                    logger.error("Registration serialization error", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                } catch (e: Exception) {
                    logger.error("Registration error", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post("/login") {
                try {
                    val body: String = call.receive()
                    val json = Json { ignoreUnknownKeys = true }
                    val request = json.decodeFromString<LoginRequest>(body)

                    val user = userRepository.findByEmail(request.email)
                    if (user == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
                        return@post
                    }

                    if (!user.verifyPassword(request.password)) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
                        return@post
                    }

                    val token = jwtService.generateToken(
                        userId = user.id!!,
                        email = user.email,
                        role = user.role.name
                    )

                    call.respond(HttpStatusCode.OK, AuthResponse(
                        token = token,
                        userId = user.id,
                        email = user.email,
                        role = user.role.name
                    ))

                } catch (e: SerializationException) {
                    logger.error("Login serialization error", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                } catch (e: Exception) {
                    logger.error("Login error", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }

        // Защищенные маршруты
        authenticate {
            get("/user/profile") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                        return@get
                    }

                    val userId = principal.payload.getClaim("userId").asLong()
                    val email = principal.payload.getClaim("email").asString()
                    val role = principal.payload.getClaim("role").asString()

                    call.respond(ProfileResponse(userId, email, role))
                } catch (e: Exception) {
                    logger.error("Error in /user/profile", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }

        // Публичные маршруты для товаров
        route("/products") {
            get {
                try {
                    val products = productService.getAllProducts()
                    call.respond(products)
                } catch (e: Exception) {
                    logger.error("Error getting products", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid product ID"))
                        return@get
                    }

                    val product = productService.getProduct(id)
                    if (product == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))
                    } else {
                        call.respond(product)
                    }
                } catch (e: Exception) {
                    logger.error("Error getting product", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }

        // Админские маршруты для товаров
        authenticate {
            post("/products") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                        return@post
                    }

                    val role = principal.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@post
                    }

                    val body: String = call.receive()
                    val json = Json { ignoreUnknownKeys = true }
                    val request = json.decodeFromString<ProductRequest>(body)

                    val product = productService.createProduct(request)
                    call.respond(HttpStatusCode.Created, product)

                } catch (e: SerializationException) {
                    logger.error("Product creation serialization error", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                } catch (e: Exception) {
                    logger.error("Error creating product", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            put("/products/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                        return@put
                    }

                    val role = principal.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@put
                    }

                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid product ID"))
                        return@put
                    }

                    val body: String = call.receive()
                    val json = Json { ignoreUnknownKeys = true }
                    val request = json.decodeFromString<ProductUpdateRequest>(body)

                    val product = productService.updateProduct(id, request)
                    if (product == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))
                    } else {
                        call.respond(product)
                    }

                } catch (e: SerializationException) {
                    logger.error("Product update serialization error", e)
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                } catch (e: Exception) {
                    logger.error("Error updating product", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            delete("/products/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                        return@delete
                    }

                    val role = principal.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@delete
                    }

                    val id = call.parameters["id"]?.toLongOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid product ID"))
                        return@delete
                    }

                    val deleted = productService.deleteProduct(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))
                    }

                } catch (e: Exception) {
                    logger.error("Error deleting product", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }

        // Защищенные маршруты для заказов
        authenticate {
            route("/orders") {
                // Создание заказа
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        if (principal == null) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                            return@post
                        }

                        val userId = principal.payload.getClaim("userId").asLong()
                        val body: String = call.receive()
                        val json = Json { ignoreUnknownKeys = true }
                        val request = json.decodeFromString<CreateOrderRequest>(body)

                        val order = orderService.createOrder(userId, request)
                        call.respond(HttpStatusCode.Created, order)

                    } catch (e: SerializationException) {
                        logger.error("Order creation serialization error", e)
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                    } catch (e: IllegalArgumentException) {
                        logger.error("Order creation error", e)
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        logger.error("Error creating order", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                    }
                }

                // Получение заказов пользователя
                get {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        if (principal == null) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                            return@get
                        }

                        val userId = principal.payload.getClaim("userId").asLong()
                        val orders = orderService.getUserOrders(userId)
                        call.respond(orders)

                    } catch (e: Exception) {
                        logger.error("Error getting orders", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                    }
                }

                // Отмена заказа
                delete("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        if (principal == null) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                            return@delete
                        }

                        val userId = principal.payload.getClaim("userId").asLong()
                        val orderId = call.parameters["id"]?.toLongOrNull()
                        if (orderId == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid order ID"))
                            return@delete
                        }

                        val cancelled = orderService.cancelOrder(orderId, userId)
                        if (cancelled) {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Order cancelled successfully"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Order not found or cannot be cancelled"))
                        }

                    } catch (e: IllegalArgumentException) {
                        logger.error("Order cancellation error", e)
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        logger.error("Error cancelling order", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                    }
                }
            }
        }

        // Админский маршрут для статистики
        authenticate {
            get("/stats/orders") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                        return@get
                    }

                    val role = principal.payload.getClaim("role").asString()
                    if (role != "ADMIN") {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@get
                    }

                    logger.info("Get orders stats (admin)")
                    call.respond(mapOf("message" to "Orders statistics - будет реализовано (только admin)"))
                } catch (e: Exception) {
                    logger.error("Error in GET /stats/orders", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
    }
}