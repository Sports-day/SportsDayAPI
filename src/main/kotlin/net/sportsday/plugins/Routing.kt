package net.sportsday.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.routes.v1.*

fun Application.configureRouting() {
    routing {
        //  health check
        get {
            call.respondText("Hello")
        }

        //  version 1.0
        route("/v1") {
            authenticate {
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
                //  Tags
                tagRouter()
                //  Configurations
                configurationRouter()
            }
        }
    }
}
