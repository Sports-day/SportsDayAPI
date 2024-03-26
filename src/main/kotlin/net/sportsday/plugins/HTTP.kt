package net.sportsday.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true

        if (System.getenv("ALLOWED_HOST") != null) {
            allowHost(
                System.getenv("ALLOWED_HOST"),
                listOf(
                    "http",
                    "https",
                ),
            )

            println("Allowed host: ${System.getenv("ALLOWED_HOST")}")
        } else {
            //  warning
            println("WARNING: ALLOWED_HOST is not set. Allow all hosts.")

            anyHost()
        }
    }
}
