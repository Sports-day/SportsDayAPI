package net.sportsday.models

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/02/23
 * @author testusuke
 */

val tables = listOf(
    Configurations,
    Logs,
    AllowedDomains,
    Classes,
    Users,
    Teams,
    TeamUsers,
    Sports,
    Games,
    Locations,
    Matches,
    TournamentPath,
    Images,
    Entries,
    Information,
    Tags,
)

fun initializeTables(migrate: Boolean = false) {
    transaction {
        if (migrate) {
            SchemaUtils.createMissingTablesAndColumns(tables = tables.toTypedArray())
        } else {
            SchemaUtils.create(tables = tables.toTypedArray())
        }
    }
}

fun dropTables() {
    transaction {
        SchemaUtils.drop(tables = tables.toTypedArray())
    }
}
