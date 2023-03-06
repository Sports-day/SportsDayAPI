package dev.t7e.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Created by testusuke on 2023/02/20
 * @author testusuke
 */


fun Route.AuthorizationRouting() {
    authenticate("azure-ad") {
        route("authorization") {
            get {
                call.respondText("Authorization route")
            }
        }
    }
}