package dev.t7e.plugins

import dev.t7e.routes.authorizationRouting
import dev.t7e.routes.v1.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        //  health check
        get {
            call.respondText("Hello")
        }

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
                //  Allowed domains
                allowedDomainsRouter()
                //  Teams
                teamsRouter()
                //  Sports
                sportsRouter()
                //  Games
                gamesRouter()
                //  Matches
                matchesRouter()
                //  Images
                imagesRouter()
                //  Locations
                locationsRouter()
                //  Information
                informationRouter()
            }
        }
    }
}
