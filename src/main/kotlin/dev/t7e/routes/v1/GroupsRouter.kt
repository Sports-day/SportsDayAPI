package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedGroup
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.GroupsService
import dev.t7e.utils.DataMessageResponse
import dev.t7e.utils.DataResponse
import dev.t7e.utils.MessageResponse
import dev.t7e.utils.logger.Logger
import dev.t7e.utils.respondOrInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
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

                            //  Logger
                            Logger.commit(
                                "[GroupsRouter] created group: ${it.name}",
                                LogEvents.CREATE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount
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

                withRole(Role.ADMIN) {
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
                                        it
                                    )
                                )
                                //  Logger
                                Logger.commit(
                                    "[GroupsRouter] updated group: ${it.name}",
                                    LogEvents.UPDATE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount
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
                                //  Logger
                                Logger.commit(
                                    "[GroupsRouter] deleted group: $id",
                                    LogEvents.DELETE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount
                                )
                            }
                    }
                }
            }
        }
    }
}
