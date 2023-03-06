package dev.t7e.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/03/06
 * @author testusuke
 */

fun Application.configureRateLimit() {
    install(RateLimit) {
        global {
            rateLimiter(
                limit = 1000,
                refillPeriod = 60.minutes
            )
        }
    }
}