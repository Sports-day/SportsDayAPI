package dev.t7e

import dev.t7e.models.initializeTables
import io.ktor.server.application.*
import io.ktor.server.netty.*
import dev.t7e.plugins.*
import dev.t7e.utils.DatabaseManager
import dev.t7e.utils.configuration.KeyValueStore

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSecurity()
    configureRouting()
    configureRateLimit()
    configureStatusPage()

    //  DB
    DatabaseManager
    //  tables
    initializeTables()
    //  configuration
    KeyValueStore

}
