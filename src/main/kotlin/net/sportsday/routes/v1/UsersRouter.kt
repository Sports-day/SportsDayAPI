package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.LogEvents
import net.sportsday.models.OmittedUser
import net.sportsday.plugins.Role
import net.sportsday.plugins.UserPrincipal
import net.sportsday.plugins.withRole
import net.sportsday.services.UsersService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.logger.Logger
import net.sportsday.utils.respondOrInternalError

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
                                it,
                            ),
                        )
                        //  Logger
                        Logger.commit(
                            "[UsersRouter] created user: ${it.name}",
                            LogEvents.CREATE,
                            call.authentication.principal<UserPrincipal>()?.microsoftAccount,
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
                                    it,
                                ),
                            )
                            //  Logger
                            Logger.commit(
                                "[UsersRouter] updated user: ${it.name}",
                                LogEvents.UPDATE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount,
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
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount,
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
