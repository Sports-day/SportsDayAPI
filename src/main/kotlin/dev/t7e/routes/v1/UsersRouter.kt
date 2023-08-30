package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedUser
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.UsersService
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
 * Created by testusuke on 2023/03/10
 * @author testusuke
 */

fun Route.usersRouter() {
    route("/users") {
        /**
         * Get all users
         */
        get {
            val users = UsersService.getAll()

            call.respond(HttpStatusCode.OK, DataResponse(users.getOrDefault(listOf())))
        }

        withRole(Role.ADMIN) {
            /**
             * create user
             */
            post {
                val omittedUser = call.receive<OmittedUser>()

                UsersService
                    .create(omittedUser)
                    .respondOrInternalError {
                        call.respond(
                            DataMessageResponse(
                                "created user",
                                it
                            )
                        )
                        //  Logger
                        Logger.commit(
                            "[UsersRouter] created user: ${it.name}",
                            LogEvents.CREATE,
                            call.authentication.principal<UserPrincipal>()?.microsoftAccount
                        )
                    }
            }
        }

        route("/{id?}") {
            /**
             * Get user
             */
            get {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                UsersService
                    .getById(id)
                    .respondOrInternalError {
                        call.respond(HttpStatusCode.OK, DataResponse(it))
                    }
            }

            withRole(Role.ADMIN) {
                /**
                 * update user
                 */
                put {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                    val omittedUser = call.receive<OmittedUser>()

                    UsersService
                        .update(id, omittedUser)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataMessageResponse(
                                    "updated user",
                                    it
                                )
                            )
                            //  Logger
                            Logger.commit(
                                "[UsersRouter] updated user: ${it.name}",
                                LogEvents.UPDATE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount
                            )
                        }
                }

                /**
                 * delete user
                 */
                delete {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    UsersService
                        .deleteById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, MessageResponse("deleted user"))
                            //  Logger
                            Logger.commit(
                                "[UsersRouter] deleted user: $id",
                                LogEvents.DELETE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount
                            )
                        }
                }

                route("/microsoft-accounts") {
                    /**
                     * Get user what linked by microsoft account
                     */
                    get {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        UsersService
                            .getLinkedMicrosoftAccount(id)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, DataResponse(it))
                            }
                    }
                }
            }

            route("/teams") {
                /**
                 * Get all teams what user belong
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    UsersService
                        .getTeams(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, DataResponse(it))
                        }
                }
            }
        }
    }
}
