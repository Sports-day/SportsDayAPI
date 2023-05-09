package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedSport
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.SportsService
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
 * Created by testusuke on 2023/05/04
 * @author testusuke
 */

fun Route.sportsRouter() {
    route("/sports") {
        withRole(Role.USER) {

            /**
             * Get all sports
             */
            get {
                val sports = SportsService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(sports.getOrDefault(listOf())))
            }

            withRole(Role.ADMIN) {

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
                                DataResponse(it)
                            )
                            //  Logger
                            Logger.commit(
                                "[SportsRouter] created sport: ${it.name}",
                                LogEvents.CREATE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount
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
                                DataResponse(it)
                            )
                        }
                }

                withRole(Role.ADMIN) {

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
                                        it
                                    )
                                )
                                //  Logger
                                Logger.commit(
                                    "[SportsRouter] updated sport: ${it.name}",
                                    LogEvents.UPDATE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount
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
                                    MessageResponse("deleted sport")
                                )
                                //  Logger
                                Logger.commit(
                                    "[SportsRouter] deleted sport: $id",
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