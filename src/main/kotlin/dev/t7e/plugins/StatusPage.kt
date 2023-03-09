package dev.t7e.plugins

import dev.t7e.utils.MessageResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.converters.*

/**
 * Created by testusuke on 2023/03/09
 * @author testusuke
 */

fun Application.configureStatusPage() {
    install(StatusPages) {

        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, MessageResponse(cause.message))
        }

        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, MessageResponse(cause.message))
        }

        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, MessageResponse(cause.message))
        }

        exception<ParameterConversionException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, MessageResponse(cause.message))
        }

        exception<DataConversionException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, MessageResponse(cause.message))
        }
    }
}