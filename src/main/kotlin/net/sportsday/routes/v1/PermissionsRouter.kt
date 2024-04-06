package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.services.PermissionsService
import net.sportsday.utils.DataResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */

fun Route.permissionsRouter() {
    route("/permissions") {
        get {
            PermissionsService
                .getAll()
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataResponse(it)
                    )
                }
        }
    }
}