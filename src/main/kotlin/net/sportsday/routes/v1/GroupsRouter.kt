package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedGroup
import net.sportsday.services.GroupsService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */

fun Route.groupsRouter() {
    route("/groups") {
        /**
         * Get all groups
         */
        get {
            val groups = GroupsService.getAll()

            call.respond(HttpStatusCode.OK, DataResponse(groups.getOrDefault(listOf())))
        }

        /**
         * Create new group
         */
        post {
            val requestBody = call.receive<OmittedGroup>()

            GroupsService
                .create(requestBody)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "created group",
                            it,
                        ),
                    )
                }
        }
    }

    route("/{id?}") {
        /**
         * Get specific group
         */
        get {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
            GroupsService.getById(id)
                .respondOrInternalError {
                    call.respond(HttpStatusCode.OK, DataResponse(it))
                }
        }

        /**
         * Update group
         */
        put {
            val id =
                call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
            val requestBody = call.receive<OmittedGroup>()

            GroupsService.update(id, requestBody)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "updated group",
                            it,
                        ),
                    )
                }
        }

        /**
         * Delete group
         */
        delete {
            val id =
                call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

            GroupsService.deleteById(id)
                .respondOrInternalError {
                    call.respond(HttpStatusCode.OK, MessageResponse("deleted group"))
                }
        }
    }
}
