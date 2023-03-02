package dev.t7e.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */

object Games: IntIdTable("games") {
    val name = varchar("name", 64)
    val description = varchar("description", 512)
    val sport = reference("sport", Sports)
    val type = enumerationByName<GameType>("type", 32)
    val weight = integer("weight")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class Game(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Game>(Games)

    var name by Games.name
    var description by Games.description
    var sport by Sport referencedOn Games.sport
    var type by Games.type
    var weight by Games.weight
    var createdAt by Games.createdAt
    var updatedAt by Games.updatedAt
}

enum class GameType(val status: String) {
    TOURNAMENT("tournament"),
    LEAGUE("league")
}