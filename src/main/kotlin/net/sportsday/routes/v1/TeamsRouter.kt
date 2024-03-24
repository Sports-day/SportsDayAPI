package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedTeam
import net.sportsday.models.OmittedTeamUsers
import net.sportsday.services.TeamsService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/03/27
 * @author testusuke
 */

fun Route.teamsRouter() {
    route("/teams") {
        /**
         * Get all teams
         */
        get {
            val teams = TeamsService.getAll()

            call.respond(
                HttpStatusCode.OK,
                DataResponse(teams.getOrDefault(listOf())),
            )
        }

        /**
         * Create new team
         */
        post {
            val requestBody = call.receive<OmittedTeam>()

            TeamsService
                .create(requestBody)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "created team",
                            it,
                        ),
                    )
                }
        }
    }

    route("/{id?}") {
        /**
         * Get specific team
         */
        get {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

            TeamsService
                .getById(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataResponse(it),
                    )
                }
        }

        route("/users") {
            get {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                TeamsService
                    .getUsers(id)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataResponse(it),
                        )
                    }
            }

            post {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")
                val omittedUsers = call.receive<OmittedTeamUsers>()

                TeamsService
                    .addUsers(id, omittedUsers.users)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "added users",
                                it,
                            ),
                        )
                    }
            }

            delete("/{userId?}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid user id parameter")

                TeamsService
                    .removeUser(id, userId)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "removed user",
                                it,
                            ),
                        )
                    }
            }
        }
    }

    /**
     * Update team
     */
    put {
        val id =
            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
        val requestBody = call.receive<OmittedTeam>()

        TeamsService
            .update(id, requestBody)
            .respondOrInternalError {
                call.respond(
                    HttpStatusCode.OK,
                    DataMessageResponse(
                        "updated team",
                        it,
                    ),
                )
            }
    }

    /**
     * Delete team
     */
    delete {
        val id =
            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

        TeamsService
            .deleteById(id)
            .respondOrInternalError {
                call.respond(
                    HttpStatusCode.OK,
                    MessageResponse("deleted team"),
                )
            }
    }
}