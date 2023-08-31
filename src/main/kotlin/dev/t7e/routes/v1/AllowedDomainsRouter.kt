package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedAllowedDomain
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.AllowedDomainsService
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
 * Created by testusuke on 2023/03/13
 * @author testusuke
 */

fun Route.allowedDomainsRouter() {
    route("/allowed-domains") {
        withRole(Role.ADMIN) {
            /**
             * Get all allowed domain
             */
            get {
                val domains = AllowedDomainsService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(domains.getOrDefault(listOf())))
            }

            /**
             * Create new allowed domain
             */
            post {
                val omittedAllowedDomain = call.receive<OmittedAllowedDomain>()

                AllowedDomainsService
                    .create(omittedAllowedDomain)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "created allowed domain",
                                it,
                            ),
                        )
                        //  Logger
                        Logger.commit(
                            "[AllowedDomainsRouter] created allowed domain: ${omittedAllowedDomain.domain}",
                            LogEvents.CREATE,
                            call.authentication.principal<UserPrincipal>()?.microsoftAccount,
                        )
                    }
            }

            route("/{id?}") {
                /**
                 * Get specific allowed domain
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    AllowedDomainsService
                        .getById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, DataResponse(it))
                        }
                }

                /**
                 * Update specific allowed domain
                 */
                put {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                    val omittedAllowedDomain = call.receive<OmittedAllowedDomain>()

                    AllowedDomainsService
                        .update(id, omittedAllowedDomain)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataMessageResponse(
                                    "updated allowed domain",
                                    it,
                                ),
                            )
                            //  Logger
                            Logger.commit(
                                "[AllowedDomainsRouter] updated allowed domain: ${omittedAllowedDomain.domain}",
                                LogEvents.UPDATE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount,
                            )
                        }
                }

                /**
                 * Delete specific allowed domain
                 */
                delete {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    AllowedDomainsService
                        .deleteById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                MessageResponse("deleted domain"),
                            )
                            //  Logger
                            Logger.commit(
                                "[AllowedDomainsRouter] deleted allowed domain: $id",
                                LogEvents.DELETE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount,
                            )
                        }
                }
            }
        }
    }
}
