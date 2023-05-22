package dev.t7e.plugins

import io.ktor.server.plugins.callloging.*
import org.slf4j.event.*
import io.ktor.server.request.*
import io.ktor.server.application.*

fun Application.configureMonitoring() {
    if (System.getenv("OUTPUT_REQUEST_LOG") == null) {
        return
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
