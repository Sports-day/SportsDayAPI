package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.sportsday.services.AuthenticationService
import net.sportsday.services.UsersService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.JwtConfig
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2024/03/25
 * @author testusuke
 */

fun Route.authenticationRouter() {
    route("/login") {
        post {
            val code = call.receive<OpenIDConnectCode>()

            try {
                //  login
                val user = AuthenticationService.login(code.code, code.redirectUri)

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, MessageResponse("Unauthorized"))
                    return@post
                }

                //  issue JWT token
                val jwt = JwtConfig.makeToken(user.id.toString())

                //  set token as http only cookie
                call.response.cookies.append(
                    name = "access_token",
                    value = jwt,
                    httpOnly = false,
                    secure = AuthenticationService.getIsSecure(),
                    maxAge = JwtConfig.getExpirationDuration(),
                    domain = JwtConfig.getIssuer(),
                    path = "/",
                )

                call.respond(HttpStatusCode.OK, DataMessageResponse("Authorized", mapOf("token" to jwt)))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, MessageResponse("Internal Server Error"))
            }
        }
    }

    authenticate {
        route("/userinfo") {
            get {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized, MessageResponse("Unauthorized"))

                UsersService
                    .getById(principal.name.toInt())
                    .respondOrInternalError {
                        call.respond(HttpStatusCode.OK, DataMessageResponse("user", it))
                    }
            }
        }
    }
}

@Serializable
data class OpenIDConnectCode(
    val code: String,
    @SerialName("redirect_uri")
    val redirectUri: String,
)
