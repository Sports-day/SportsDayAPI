package net.sportsday.plugins

import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import net.sportsday.utils.JwtConfig
import net.sportsday.utils.MessageResponse

fun Application.configureSecurity() {
    authentication {
        jwt {
            realm = System.getenv("JWT_REALMS") ?: throw IllegalArgumentException("No JWT realms found")

            verifier(JwtConfig.verifier)

            authHeader { call ->
                //  cookie
                println("call.request.cookies: ${call.request.cookies.rawCookies}")

                val cookieValue = call.request.cookies["access_token"] ?: return@authHeader null
                println("cookieValue: $cookieValue")

                try {
                    parseAuthorizationHeader("Bearer $cookieValue")
                } catch (cause: IllegalArgumentException) {
                    cause.message
                    null
                }
            }

            validate { credential ->
                JWTPrincipal(credential.payload)
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, MessageResponse("Token is not valid or has expired"))
            }
        }
    }
}
