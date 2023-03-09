package dev.t7e.routes.v1

import dev.t7e.models.OmittedGroup
import dev.t7e.plugins.Role
import dev.t7e.plugins.withRole
import dev.t7e.services.GroupsService
import dev.t7e.utils.DataMessageResponse
import dev.t7e.utils.DataResponse
import dev.t7e.utils.MessageResponse
import dev.t7e.utils.respondOrInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */

fun Route.groupsRouter() {
    route("/groups") {
        withRole(Role.USER) {

            /**
             * Get all groups
             */
            get {
                val groups = GroupsService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(groups.getOrDefault(listOf())))
            }

            withRole(Role.ADMIN) {

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
                                    it
                                )
                            )
                        }
                }
            }

            route("/{id?}") {

                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                    GroupsService.getById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, DataResponse(it))
                        }
                }

                withRole(Role.ADMIN) {

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
                                        it
                                    )
                                )
                            }
                    }

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
        }
    }
}