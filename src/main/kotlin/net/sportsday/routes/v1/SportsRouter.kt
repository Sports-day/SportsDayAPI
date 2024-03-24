package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedSport
import net.sportsday.services.SportsService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/05/04
 * @author testusuke
 */

fun Route.sportsRouter() {
    route("/sports") {
        /**
         * Get all sports
         */
        get {
            val sports = SportsService.getAll(
                call.request.queryParameters["filter"] == "true",
            )

            call.respond(HttpStatusCode.OK, DataResponse(sports.getOrDefault(listOf())))
        }

        /**
         * Create new sport
         */
        post {
            val requestBody = call.receive<OmittedSport>()

            SportsService
                .create(requestBody)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataResponse(it),
                    )
                }
        }
    }

    route("/{id?}") {
        /**
         * Get sport by id
         */
        get {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

            SportsService
                .getById(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataResponse(it),
                    )
                }
        }

        /**
         * Get sport progress
         */
        get("/progress") {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

            SportsService
                .getProgress(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataResponse(it),
                    )
                }
        }

        /**
         * Update sport by id
         */
        put {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
            val requestBody = call.receive<OmittedSport>()

            SportsService
                .update(id, requestBody)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "updated sport",
                            it,
                        ),
                    )
                }
        }

        /**
         * Delete sport by id
         */
        delete {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

            SportsService
                .deleteById(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("deleted sport"),
                    )
                }
        }
    }
}
