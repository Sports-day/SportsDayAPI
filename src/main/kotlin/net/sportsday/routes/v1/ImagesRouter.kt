package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.LogEvents
import net.sportsday.models.OmittedImage
import net.sportsday.plugins.Role
import net.sportsday.plugins.UserPrincipal
import net.sportsday.plugins.withRole
import net.sportsday.services.ImagesService
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.logger.Logger
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */

fun Route.imagesRouter() {
    route("/images") {
        withRole(Role.USER) {
            /**
             * Get all images
             */
            get {
                val images = ImagesService.getAll()

                call.respond(
                    HttpStatusCode.OK,
                    DataResponse(images.getOrDefault(listOf())),
                )
            }

            withRole(Role.ADMIN) {
                /**
                 * Create new image
                 */
                post {
                    val requestBody = call.receive<OmittedImage>()
                    val user =
                        call.authentication.principal<UserPrincipal>() ?: throw NotFoundException("user not found")

                    ImagesService
                        .create(user.microsoftAccount, requestBody)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataResponse(it),
                            )
                            //  Logger
                            Logger.commit(
                                "[ImagesRouter] created image: ${it.name}",
                                LogEvents.CREATE,
                                user.microsoftAccount,
                            )
                        }
                }
            }

            route("/{id?}") {
                /**
                 * Get image by id
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    ImagesService
                        .getById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataResponse(it),
                            )
                        }
                }

                /**
                 * delete image by id
                 */
                delete {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    ImagesService
                        .deleteById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                MessageResponse("deleted image"),
                            )
                            //  Logger
                            Logger.commit(
                                "[ImagesRouter] deleted image: $id",
                                LogEvents.DELETE,
                                call.authentication.principal<UserPrincipal>()?.microsoftAccount,
                            )
                        }
                }
            }
        }
    }
}
