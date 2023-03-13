package dev.t7e.plugins

import dev.t7e.routes.authorizationRouting
import dev.t7e.routes.v1.classesRouter
import dev.t7e.routes.v1.groupsRouter
import dev.t7e.routes.v1.microsoftAccountsRouter
import dev.t7e.routes.v1.usersRouter
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureRouting() {

    routing {
        authorizationRouting()
        //  version 1.0
        route("/v1") {
            authenticate {
                //  Microsoft Accounts
                microsoftAccountsRouter()
                //  Groups
                groupsRouter()
                //  Classes
                classesRouter()
                //  Users
                usersRouter()
            }
        }
    }
}
