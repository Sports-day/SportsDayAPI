package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedAllowedDomain
import net.sportsday.models.Permission
import net.sportsday.services.AllowedDomainsService
import net.sportsday.services.withPermission
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/03/13
 * @author testusuke
 */

fun Route.allowedDomainsRouter() {
    route("/allowed-domains") {
        withPermission(Permission.AccessPolicy.Read) {
            /**
             * Get all allowed domain
             */
            get {
                val domains = AllowedDomainsService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(domains.getOrDefault(listOf())))
            }

            withPermission(Permission.AccessPolicy.Write) {
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
                        }
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

                withPermission(Permission.AccessPolicy.Write) {
                    /**
                     * Update specific allowed domain
                     */
                    put {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
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
                            }
                    }

                    /**
                     * Delete specific allowed domain
                     */
                    delete {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        AllowedDomainsService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    MessageResponse("deleted domain"),
                                )
                            }
                    }
                }
            }
        }
    }
}
