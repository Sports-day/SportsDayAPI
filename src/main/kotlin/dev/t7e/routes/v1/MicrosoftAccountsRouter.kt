package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.MicrosoftAccountsService
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
import kotlinx.serialization.Serializable

/**
 * Created by testusuke on 2023/03/06
 * @author testusuke
 */

fun Route.microsoftAccountsRouter() {
    route("/microsoft-accounts") {
        withRole(Role.USER) {
            withRole(Role.ADMIN) {
                /**
                 * Get all microsoft accounts
                 */
                get {
                    val accounts = MicrosoftAccountsService.getAll()

                    call.respond(HttpStatusCode.OK, DataResponse(accounts.getOrDefault(listOf())))
                }
            }
            route("/{id?}") {
                /**
                 * Get specific microsoft account
                 */
                get {
                    val id = getId(call)

                    val ms = call.authentication.principal<UserPrincipal>()
                        ?: throw BadRequestException("failed to get user principal")

                    if (!ms.roles.contains(Role.ADMIN) && ms.microsoftAccount.id.value != id) {
                        throw BadRequestException("you dont have permission to link this account")
                    }

                    MicrosoftAccountsService.getById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, DataResponse(it))
                        }
                }

                withRole(Role.ADMIN) {
                    /**
                     * Delete specific microsoft account
                     */
                    delete {
                        val id = getId(call)
                        MicrosoftAccountsService.deleteById(id)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, MessageResponse("deleted microsoft account"))
                                //  Logger
                                Logger.commit(
                                    "[MicrosoftAccountsRouter] deleted microsoft account: $id",
                                    LogEvents.DELETE,
                                    call.authentication.principal<UserPrincipal>()?.microsoftAccount
                                )
                            }
                    }

                    route("/role") {
                        /**
                         * Update specific microsoft account role
                         */
                        put {
                            val id = getId(call)
                            val role = call.receive<AccountRoleRequest>()

                            MicrosoftAccountsService
                                .setAccountRole(id, role.role)
                                .respondOrInternalError {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        DataMessageResponse(
                                            "updated account role",
                                            it
                                        )
                                    )
                                }
                        }
                    }
                }

                route("/link-user") {
                    put {
                        val id = getId(call)
                        val requestBody = call.receive<LinkUserRequest>()

                        val ms = call.authentication.principal<UserPrincipal>()
                            ?: throw BadRequestException("failed to get user principal")

                        if (!ms.roles.contains(Role.ADMIN) && ms.microsoftAccount.id.value != id) {
                            throw BadRequestException("you dont have permission to link this account")
                        }

                        MicrosoftAccountsService
                            .linkUser(id, requestBody.userId)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, MessageResponse("link user"))
                            }
                    }

                    delete {
                        val id = getId(call)
                        val ms = call.authentication.principal<UserPrincipal>()
                            ?: throw BadRequestException("failed to get user principal")

                        if (!ms.roles.contains(Role.ADMIN) && ms.microsoftAccount.id.value != id) {
                            throw BadRequestException("you dont have permission to link this account")
                        }

                        MicrosoftAccountsService
                            .unlinkUser(id)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, MessageResponse("unlink user"))
                            }
                    }

                    post("later") {
                        val id = getId(call)

                        val ms = call.authentication.principal<UserPrincipal>()
                            ?: throw BadRequestException("failed to get user principal")

                        if (!ms.roles.contains(Role.ADMIN) && ms.microsoftAccount.id.value != id) {
                            throw BadRequestException("you dont have permission to link this account")
                        }

                        MicrosoftAccountsService
                            .linkLater(id)
                            .respondOrInternalError {
                                call.respond(HttpStatusCode.OK, MessageResponse("link user"))
                            }
                    }
                }
            }
        }
    }
}

/**
 * Get id from call
 */
private fun getId(call: ApplicationCall): Int {
    return if (call.parameters["id"] == "me") {
        call.authentication.principal<UserPrincipal>()?.microsoftAccountId
    } else {
        call.parameters["id"]?.toIntOrNull()
    } ?: throw BadRequestException("invalid id parameter")
}

@Serializable
data class AccountRoleRequest(val role: String)

@Serializable
data class LinkUserRequest(val userId: Int)
