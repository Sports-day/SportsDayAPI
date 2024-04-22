package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import net.sportsday.models.OmittedRole
import net.sportsday.models.Permission
import net.sportsday.services.RolesService
import net.sportsday.services.withPermission
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */

fun Route.rolesRouter() {
    route("/roles") {
        withPermission(Permission.Role.Read) {
            /**
             * Get all roles
             */
            get {
                RolesService
                    .getAll()
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataResponse(it)
                        )
                    }
            }

            withPermission(Permission.Role.Write) {
                /**
                 * create role
                 */
                post {
                    val omittedRole = call.receive<OmittedRole>()

                    RolesService
                        .create(omittedRole)
                        .respondOrInternalError {
                            call.respond(
                                DataMessageResponse(
                                    "created role",
                                    it,
                                ),
                            )
                        }
                }
            }

            route("/{id?}") {
                /**
                 * Get role
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    RolesService
                        .getById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, DataResponse(it))
                        }
                }

                withPermission(Permission.Role.Write) {
                    /**
                     * delete role
                     */
                    delete {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        RolesService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(
                                    DataMessageResponse(
                                        "deleted role",
                                        it,
                                    ),
                                )
                            }
                    }

                    /**
                     * update role
                     */
                    put {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                        val omittedRole = call.receive<OmittedRole>()

                        RolesService
                            .update(id, omittedRole)
                            .respondOrInternalError {
                                call.respond(
                                    DataMessageResponse(
                                        "updated role",
                                        it,
                                    ),
                                )
                            }
                    }

                    route("/permissions") {
                        post {
                            val roleId =
                                call.parameters["id"]?.toIntOrNull()
                                    ?: throw BadRequestException("invalid id parameter")
                            val permissionRequest = call.receive<RolePermissionRequest>()

                            RolesService
                                .addPermission(roleId, permissionRequest.permission)
                                .respondOrInternalError {
                                    call.respond(
                                        DataMessageResponse(
                                            "added permission",
                                            it,
                                        ),
                                    )
                                }
                        }

                        delete("/{permission?}") {
                            val roleId =
                                call.parameters["id"]?.toIntOrNull()
                                    ?: throw BadRequestException("invalid id parameter")
                            val permission = call.parameters["permission"]
                                ?: throw BadRequestException("invalid permission parameter")

                            RolesService
                                .removePermission(roleId, permission)
                                .respondOrInternalError {
                                    call.respond(
                                        DataMessageResponse(
                                            "removed permission",
                                            it,
                                        ),
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class RolePermissionRequest(
    val permission: String,
)
