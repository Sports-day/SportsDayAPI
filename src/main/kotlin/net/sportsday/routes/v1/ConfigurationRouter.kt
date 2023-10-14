package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.plugins.Role
import net.sportsday.plugins.withRole
import net.sportsday.services.ConfigurationService
import net.sportsday.utils.DataResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/10/15
 * @author testusuke
 */
fun Route.configurationRouter() {
    withRole(Role.ADMIN) {
        route("/configuration") {
            route("/restrict_game_preview") {
                route("/status") {
                    //  get restriction is enabled
                    get {
                        ConfigurationService.RestrictGamePreview.isEnabled()
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(it),
                                )
                            }
                    }

                    //  set restriction status
                    post {
                        val status = call.parameters["status"]?.toBoolean() ?: false

                        ConfigurationService.RestrictGamePreview.setEnabled(status)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(status),
                                )
                            }
                    }
                }

                route("/percentage") {
                    //  get restriction percentage
                    get {
                        ConfigurationService.RestrictGamePreview.getPercentage()
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(it),
                                )
                            }
                    }

                    //  set restriction percentage
                    post {
                        val percentage = call.parameters["percentage"]?.toDouble() ?: 0.6

                        ConfigurationService.RestrictGamePreview.setPercentage(percentage)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(percentage),
                                )
                            }
                    }
                }
            }
        }
    }
}
