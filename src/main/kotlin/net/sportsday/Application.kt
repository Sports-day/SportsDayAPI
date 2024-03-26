package net.sportsday

import io.ktor.server.application.*
import io.ktor.server.netty.*
import net.sportsday.models.initializeTables
import net.sportsday.plugins.*
import net.sportsday.utils.DatabaseManager
import net.sportsday.utils.RedisManager
import net.sportsday.utils.configuration.KeyValueStore
import net.sportsday.utils.logger.Logger

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSecurity()
    configureRouting()
    configureCors()
    configureStatusPage()

    //  DB
    DatabaseManager
    //  tables
    initializeTables(true)
    //  configuration
    KeyValueStore
    //  Log
    Logger
    //  Redis Manager
    RedisManager
}
