package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedLocation
import net.sportsday.services.LocationsService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */

fun Route.locationsRouter() {
    route("/locations") {
        /**
         * Get all locations
         */
        get {
            val locations = LocationsService.getAll()

            call.respond(
                HttpStatusCode.OK,
                DataResponse(locations.getOrDefault(listOf())),
            )
        }

        /**
         * Create new location
         */
        post {
            val omittedLocation = call.receive<OmittedLocation>()

            LocationsService
                .create(omittedLocation)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "created location",
                            it,
                        ),
                    )
                }
        }
    }

    route("/{id?}") {
        /**
         * Get location by id
         */
        get {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id")

            LocationsService
                .getById(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataResponse(it),
                    )
                }
        }

        /**
         * Update location
         */
        put {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id")
            val omittedLocation = call.receive<OmittedLocation>()

            LocationsService
                .update(id, omittedLocation)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "updated location",
                            it,
                        ),
                    )
                }
        }

        /**
         * Delete location
         */
        delete {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id")

            LocationsService
                .deleteById(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "deleted location",
                            it,
                        ),
                    )
                }
        }
    }
}
