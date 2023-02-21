package dev.t7e

import io.ktor.server.application.*
import io.ktor.server.netty.*
import dev.t7e.plugins.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
