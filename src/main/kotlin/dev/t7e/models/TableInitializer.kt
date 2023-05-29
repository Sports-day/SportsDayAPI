package dev.t7e.models

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/02/23
 * @author testusuke
 */

fun initializeTables(migrate: Boolean = false) {
    transaction {
        listOf(
            Configurations,
            Logs,
            AllowedDomains,
            Groups,
            Classes,
            Users,
            MicrosoftAccounts,
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
        ).forEach {
            if (migrate) {
                SchemaUtils.createMissingTablesAndColumns(it)
            } else {
                SchemaUtils.create(it)
            }
        }
    }
}