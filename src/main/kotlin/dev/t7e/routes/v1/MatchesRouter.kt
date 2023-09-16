package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedMatch
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.MatchesService
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
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */

fun Route.matchesRouter() {
    route("/matches") {
        withRole(Role.USER) {
            /**
             * Get all matches
             */
            get {
                val matches = MatchesService.getAll()

                call.respond(
                    HttpStatusCode.OK,
                    DataResponse(matches.getOrDefault(listOf())),
                )
            }

            route("/{id?}") {
                /**
                 * Get match by id
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    MatchesService
                        .getById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataResponse(it),
                            )
                        }
                }

                withRole(Role.ADMIN) {
                    /**
                     * Update match by id
                     */
                    put {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                        val requestBody = call.receive<OmittedMatch>()

                        MatchesService
                            .update(id, requestBody)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "updated match",
                                        it,
                                    ),
                                )
                                //  Logger
                                Logger.commit(
                                    "[MatchesRouter] updated match: $id",
                                    LogEvents.CREATE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount,
                                )
                            }
                    }

                    /**
                     * Delete match by id
                     */
                    delete {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        MatchesService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    MessageResponse("deleted match"),
                                )
                                //  Logger
                                Logger.commit(
                                    "[MatchesRouter] deleted match: $id",
                                    LogEvents.DELETE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount,
                                )
                            }
                    }
                }
            }
        }
    }
}
