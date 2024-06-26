package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedImage
import net.sportsday.models.Permission
import net.sportsday.services.ImagesService
import net.sportsday.services.withPermission
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */

fun Route.imagesRouter() {
    route("/images") {
        withPermission(Permission.Image.Read) {
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

            withPermission(Permission.Image.Write) {
                /**
                 * Create new image
                 */
                post {
                    val requestBody = call.receive<OmittedImage>()

                    ImagesService
                        .create(requestBody)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataResponse(it),
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

                withPermission(Permission.Image.Write) {
                    /**
                     * delete image by id
                     */
                    delete {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        ImagesService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    MessageResponse("deleted image"),
                                )
                            }
                    }
                }

                /**
                 * Get image file by id
                 */
                get("/file") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    ImagesService
                        .getImageFile(id)
                        .respondOrInternalError {
                            //  header
                            call.response.header(
                                HttpHeaders.ContentType,
                                ContentType.Image.JPEG.toString(),
                            )

                            //  response
                            call.respond(
                                HttpStatusCode.OK,
                                it,
                            )
                        }
                }
            }
        }
    }
}
