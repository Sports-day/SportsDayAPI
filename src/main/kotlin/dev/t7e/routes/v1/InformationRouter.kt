package dev.t7e.routes.v1

import dev.t7e.models.OmittedInformationModel
import dev.t7e.plugins.Role
import dev.t7e.plugins.withRole
import dev.t7e.services.InformationService
import dev.t7e.utils.DataMessageResponse
import dev.t7e.utils.DataResponse
import dev.t7e.utils.MessageResponse
import dev.t7e.utils.respondOrInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
                    DataResponse(information.getOrDefault(listOf()))
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
                                DataResponse(it)
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
                                DataResponse(it)
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
                                    it
                                )
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
                                MessageResponse("delete information")
                            )
                        }
                }
            }
        }
    }
}