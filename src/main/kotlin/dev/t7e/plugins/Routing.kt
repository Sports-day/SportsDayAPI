package dev.t7e.plugins

import dev.t7e.routes.authorizationRouting
import dev.t7e.routes.v1.microsoftAccountsRouter
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {

    routing {
        authorizationRouting()
        //  version 1.0
        route("/v1") {
            //  Microsoft Accounts
            microsoftAccountsRouter()
        }
    }
}
