package net.sportsday.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import net.sportsday.plugins.Role
import net.sportsday.plugins.UserPrincipal
import net.sportsday.utils.DataMessageResponse

/**
 * Created by testusuke on 2023/02/20
 * @author testusuke
 */

fun Route.authorizationRouting() {
    authenticate {
        route("authorization") {
            get {
                val principal = call.authentication.principal<UserPrincipal>()

                call.respond(
                    DataMessageResponse(
                        "authorized",
                        Me(
                            principal?.microsoftAccountId ?: -1,
                            if (principal?.roles?.contains(Role.ADMIN) == true) "admin" else "user",
                        ),
                    ),
                )
            }
        }
    }
}

@Serializable
data class Me(
    val microsoftAccountId: Int,
    val role: String,
)
