package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedInformationModel
import net.sportsday.plugins.Role
import net.sportsday.plugins.withRole
import net.sportsday.services.InformationService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/05/29
 * @author testusuke
 */

fun Route.informationRouter() {
    route("/information") {
        withRole(Role.USER) {
            /**
             * Get all information
             */
            get {
                val information = InformationService.getAll()

                call.respond(
                    HttpStatusCode.OK,
                    DataResponse(information.getOrDefault(listOf())),
                )
            }

            withRole(Role.ADMIN) {
                /**
                 * Create new information
                 */
                post {
                    val omittedInformation = call.receive<OmittedInformationModel>()

                    InformationService
                        .create(omittedInformation)
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
                 * Get information by id
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id")

                    InformationService
                        .getById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataResponse(it),
                            )
                        }
                }

                /**
                 * Update information by id
                 */
                put {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id")
                    val omittedInformation = call.receive<OmittedInformationModel>()

                    InformationService
                        .update(id, omittedInformation)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataMessageResponse(
                                    "updated information",
                                    it,
                                ),
                            )
                        }
                }

                /**
                 * Delete information by id
                 */
                delete {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id")

                    InformationService
                        .deleteById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                MessageResponse("delete information"),
                            )
                        }
                }
            }
        }
    }
}
