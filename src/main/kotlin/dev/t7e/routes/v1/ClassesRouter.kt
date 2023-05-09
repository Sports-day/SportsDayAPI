package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedClassModel
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.ClassesService
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
 * Created by testusuke on 2023/03/09
 * @author testusuke
 */

fun Route.classesRouter() {
    route("/classes") {
        withRole(Role.USER) {

            /**
             * Get all classes
             */
            get {
                val classes = ClassesService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(classes.getOrDefault(listOf())))
            }

            withRole(Role.ADMIN) {

                /**
                 * Create new class
                 */
                post {
                    val omittedClass = call.receive<OmittedClassModel>()

                    ClassesService
                        .create(omittedClass)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataMessageResponse(
                                    "created class",
                                    it
                                )
                            )
                            //  Logger
                            Logger.commit(
                                "[ClassesRouter] created class: ${it.name}",
                                LogEvents.CREATE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount
                            )
                        }
                }
            }


            route("/{id?}") {

                /**
                 * Get specific class
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    ClassesService.getById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, DataResponse(it))
                        }
                }


                route("/users") {

                    /**
                     * Get all users belonging to specific class
                     */
                    get {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        ClassesService
                            .getAllUsersOfClass(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(it)
                                )
                            }
                    }
                }

                withRole(Role.ADMIN) {

                    /**
                     * Update class
                     */
                    put {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                        val requestBody = call.receive<OmittedClassModel>()

                        ClassesService
                            .update(id, requestBody)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "updated class",
                                        it
                                    )
                                )
                                //  Logger
                                Logger.commit(
                                    "[ClassesRouter] updated class: ${it.name}",
                                    LogEvents.UPDATE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount
                                )
                            }
                    }

                    delete {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        ClassesService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, MessageResponse("deleted class"))
                                //  Logger
                                Logger.commit(
                                    "[ClassesRouter] deleted class: $id",
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