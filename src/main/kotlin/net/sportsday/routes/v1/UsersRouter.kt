package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedUser
import net.sportsday.models.Permission
import net.sportsday.services.RoleId
import net.sportsday.services.UsersService
import net.sportsday.services.withPermission
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/03/10
 * @author testusuke
 */

fun Route.usersRouter() {
    route("/users") {
        withPermission(Permission.User.Read) {
            /**
             * Get all users
             */
            get {
                val users = UsersService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(users.getOrDefault(listOf())))
            }

            withPermission(Permission.User.Write) {
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

                withPermission(Permission.User.Write) {
                    /**
                     * update user
                     */
                    put {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
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
                            }
                    }

                    /**
                     * delete user
                     */
                    delete {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        UsersService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, MessageResponse("deleted user"))
                            }
                    }
                }

                route("/teams") {
                    /**
                     * Get all teams what user belong
                     */
                    get {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        UsersService
                            .getTeams(id)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, DataResponse(it))
                            }
                    }
                }

                route("/role") {
                    withPermission(Permission.User.Role.Read) {
                        /**
                         * Get all roles what user have
                         */
                        get {
                            val id =
                                call.parameters["id"]?.toIntOrNull()
                                    ?: throw BadRequestException("invalid id parameter")

                            UsersService
                                .getRole(id)
                                .respondOrInternalError {
                                    call.respond(HttpStatusCode.OK, DataResponse(it))
                                }
                        }

                        withPermission(Permission.User.Role.Write) {
                            /**
                             * Set role to user
                             */
                            post {
                                val id =
                                    call.parameters["id"]?.toIntOrNull()
                                        ?: throw BadRequestException("invalid id parameter")
                                val roleId = call.receive<RoleId>()

                                UsersService
                                    .setRole(id, roleId.id)
                                    .respondOrInternalError {
                                        call.respond(HttpStatusCode.OK, DataResponse(it))
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}
