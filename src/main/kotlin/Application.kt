package com.example

import io.ktor.server.application.*
import com.example.plugins.*
import com.example.worker.OrderEventWorker
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    // Запускаем worker
    OrderEventWorker.start()

    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureSecurity()
    configureSwagger()
    configureRouting()
}