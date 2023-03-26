package dev.t7e.models

import dev.t7e.utils.SmartCache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import kotlin.time.Duration.Companion.minutes

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

class GameEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: SmartCache<GameEntity, Game> (
        entityName = "group",
        table = Games,
        duration = 5.minutes,
        serializer = { it.serializableModel() }
    )

    var name by Games.name
    var description by Games.description
    var sport by SportEntity referencedOn Games.sport
    var type by Games.type
    var weight by Games.weight
    var createdAt by Games.createdAt
    var updatedAt by Games.updatedAt

    fun serializableModel(): Game {
        return Game(
            id.value,
            name,
            description,
            sport.id.value,
            type,
            weight,
            createdAt.toString(),
            updatedAt.toString()
        )
    }
}

enum class GameType(val status: String) {
    TOURNAMENT("tournament"),
    LEAGUE("league")
}

@Serializable
data class Game(
    val id: Int,
    val name: String,
    val description: String,
    val sportId: Int,
    val type: GameType,
    val weight: Int,
    val createdAt: String,
    val updatedAt: String
)