package dev.t7e.plugins

import dev.t7e.routes.AuthorizationRouting
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {

    routing {
        AuthorizationRouting()
    }
}
