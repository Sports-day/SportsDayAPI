package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.OmittedTag
import net.sportsday.models.Permission
import net.sportsday.services.TagService
import net.sportsday.services.withPermission
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/10/02
 * @author testusuke
 */
fun Route.tagRouter() {
    route("/tags") {
        withPermission(Permission.Tag.Read) {
            /**
             * Get all tags
             */
            get {
                val tags = TagService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(tags.getOrDefault(listOf())))
            }

            withPermission(Permission.Tag.Write) {
                /**
                 * Create new tag
                 */
                post {
                    val requestBody = call.receive<OmittedTag>()

                    TagService
                        .create(requestBody)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataMessageResponse(
                                    "created tag",
                                    it,
                                ),
                            )
                        }
                }
            }

            route("/{id?}") {

                /**
                 * Get tag by id
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    TagService
                        .getById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataResponse(it),
                            )
                        }
                }

                withPermission(Permission.Tag.Write) {

                    /**
                     * Update tag
                     */
                    put {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                        val requestBody = call.receive<OmittedTag>()

                        TagService
                            .update(id, requestBody)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "update tag",
                                        it,
                                    ),
                                )
                            }
                    }

                    /**
                     * Delete tag
                     */
                    delete {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        TagService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "deleted tag",
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
