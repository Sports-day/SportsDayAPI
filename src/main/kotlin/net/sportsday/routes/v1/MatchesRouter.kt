package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedMatch
import net.sportsday.models.Permission
import net.sportsday.services.MatchesService
import net.sportsday.services.withPermission
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */

fun Route.matchesRouter() {
    route("/matches") {
        withPermission(Permission.Match.Read) {
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

                withPermission(Permission.Match.Write) {
                    /**
                     * Update match by id
                     */
                    put {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
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
                            }
                    }

                    /**
                     * Delete match by id
                     */
                    delete {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        MatchesService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    MessageResponse("deleted match"),
                                )
                            }
                    }
                }
            }
        }
    }
}
