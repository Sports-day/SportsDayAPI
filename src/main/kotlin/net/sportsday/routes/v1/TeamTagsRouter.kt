package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedTeamTag
import net.sportsday.services.TeamTagsService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2024/03/28
 * @author testusuke
 */
fun Route.teamTagsRouter() {
    route("/team_tags") {
        /**
         * Get all team tags
         */
        get {
            val teamTags = TeamTagsService.getAll()

            call.respond(
                HttpStatusCode.OK,
                DataResponse(teamTags.getOrDefault(listOf())),
            )
        }

        /**
         * Create new team tag
         */
        post {
            val requestBody = call.receive<OmittedTeamTag>()

            TeamTagsService
                .create(requestBody)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "created team tag",
                            it,
                        ),
                    )
                }
        }

        route("/{id?}") {
            /**
             * Get specific team tag
             */
            get {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                val teamTag = TeamTagsService.getById(id)

                call.respond(
                    HttpStatusCode.OK,
                    DataResponse(teamTag.getOrDefault(null)),
                )
            }

            /**
             * Delete specific team tag
             */
            delete {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                TeamTagsService
                    .deleteById(id)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "deleted team tag",
                                it,
                            ),
                        )
                    }
            }

            /**
             * Update specific team tag
             */
            put {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                val requestBody = call.receive<OmittedTeamTag>()

                TeamTagsService
                    .edit(id, requestBody)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "updated team tag",
                                it,
                            ),
                        )
                    }
            }
        }
    }
}
