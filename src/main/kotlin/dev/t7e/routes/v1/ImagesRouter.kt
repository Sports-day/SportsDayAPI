package dev.t7e.routes.v1

import dev.t7e.models.OmittedImage
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.ImagesService
import dev.t7e.utils.DataResponse
import dev.t7e.utils.MessageResponse
import dev.t7e.utils.respondOrInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
                    DataResponse(images.getOrDefault(listOf()))
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
                                DataResponse(it)
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
                                DataResponse(it)
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
                                MessageResponse("deleted image")
                            )
                        }
                }
            }
        }
    }
}