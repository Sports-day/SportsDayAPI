package net.sportsday.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Created by testusuke on 2024/03/26
 * @author testusuke
 */

fun Application.configureCors() {
    install(CORS) {
        anyHost()
    }
}
