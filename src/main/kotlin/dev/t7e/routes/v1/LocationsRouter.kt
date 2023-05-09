package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedLocation
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.LocationsService
import dev.t7e.utils.DataMessageResponse
import dev.t7e.utils.DataResponse
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

fun Route.locationsRouter() {
    route("/locations") {
        withRole(Role.USER) {

            /**
             * Get all locations
             */
            get {
                val locations = LocationsService.getAll()

                call.respond(
                    HttpStatusCode.OK,
                    DataResponse(locations.getOrDefault(listOf()))
                )
            }

            withRole(Role.ADMIN) {
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
                                    it
                                )
                            )
                            //  Logger
                            Logger.commit(
                                "[LocationsRouter] created location: ${it.name}",
                                LogEvents.CREATE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount
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
                                DataResponse(it)
                            )
                        }
                }

                withRole(Role.ADMIN) {
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
                                        it
                                    )
                                )
                                //  Logger
                                Logger.commit(
                                    "[LocationsRouter] updated location: ${it.name}",
                                    LogEvents.UPDATE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount
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
                                        it
                                    )
                                )
                                //  Logger
                                Logger.commit(
                                    "[LocationsRouter] deleted location: $id",
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