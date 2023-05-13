package dev.t7e.routes

import dev.t7e.plugins.UserPrincipal
import dev.t7e.utils.DataMessageResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Created by testusuke on 2023/02/20
 * @author testusuke
 */


fun Route.authorizationRouting() {
    authenticate {
        route("authorization") {
            get {
                call.respond(
                    DataMessageResponse(
                        "authorized",
                        Me(call.authentication.principal<UserPrincipal>()?.microsoftAccountId ?: -1)
                    )
                )
            }
        }
    }
}

@Serializable
data class Me(
    val microsoftAccountId: Int
)