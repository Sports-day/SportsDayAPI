package dev.t7e.models

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/02/23
 * @author testusuke
 */

fun initializeTables() {
    transaction {
        addLogger(StdOutSqlLogger)

        //  AllowedDomains
        SchemaUtils.create(AllowedDomains)

    }
}