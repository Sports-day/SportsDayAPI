package dev.t7e.routes.v1

import dev.t7e.models.OmittedTag
import dev.t7e.plugins.Role
import dev.t7e.plugins.withRole
import dev.t7e.services.TagService
import dev.t7e.utils.respondOrInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Created by testusuke on 2023/10/02
 * @author testusuke
 */
fun Route.tagRouter() {
    route("/tags") {
        withRole(Role.USER) {
            /**
             * Get all tags
             */
            get {
                val tags = TagService.getAll()

                call.respond(HttpStatusCode.OK, tags)
            }

            withRole(Role.ADMIN) {
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
                                it,
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
                                it,
                            )
                        }
                }

                withRole(Role.ADMIN) {
                    /**
                     * Update tag
                     */
                    put {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                        val requestBody = call.receive<OmittedTag>()

                        TagService
                            .update(id, requestBody)
                            .respondOrInternalError {
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
}
