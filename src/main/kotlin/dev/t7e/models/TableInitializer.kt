package dev.t7e.models

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/02/23
 * @author testusuke
 */

fun initializeTables() {
    transaction {
        //  Configuration
        SchemaUtils.create(Configurations)
        //  Logs
        SchemaUtils.create(Logs)
        //  AllowedDomains
        SchemaUtils.create(AllowedDomains)
        //  Groups
        SchemaUtils.create(Groups)
        //  Classes
        SchemaUtils.create(Classes)
        //  Users
        SchemaUtils.create(Users)
        //  AdminUsers
        SchemaUtils.create(AdminUsers)
        //  MicrosoftAccounts
        SchemaUtils.create(MicrosoftAccounts)
        //  Teams
        SchemaUtils.create(Teams)
        //  TeamUsers
        SchemaUtils.create(TeamUsers)
        //  Sports
        SchemaUtils.create(Sports)
        //  Games
        SchemaUtils.create(Games)
        //  Locations
        SchemaUtils.create(Locations)
        //  Matches
        SchemaUtils.create(Matches)
        //  TournamentPath
        SchemaUtils.create(TournamentPath)
    }
}