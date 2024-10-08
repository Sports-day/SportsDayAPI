package net.sportsday.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */

object Games : IntIdTable("games") {
    val name = varchar("name", 64)
    val description = varchar("description", 512)
    val sport = reference("sport", Sports, onDelete = ReferenceOption.CASCADE)
    val type = enumerationByName<GameType>("type", 32)
    val calculationType = enumerationByName<CalculationType>("calculation_type", 32).default(CalculationType.DIFF_SCORE)
    val weight = integer("weight")
    val tag = reference("tag", Tags, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class GameEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GameEntity> (Games)

    var name by Games.name
    var description by Games.description
    var sport by SportEntity referencedOn Games.sport
    var type by Games.type
    var calculationType by Games.calculationType
    var weight by Games.weight
    var tag by TagEntity optionalReferencedOn Games.tag
    var createdAt by Games.createdAt
    var updatedAt by Games.updatedAt
    val matches by MatchEntity referrersOn Matches.game
    var teams by TeamEntity via Entries

    fun serializableModel(): Game {
        return Game(
            id.value,
            name,
            description,
            sport.id.value,
            type,
            calculationType,
            weight,
            tag?.id?.value,
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
enum class GameType(val status: String) {
    @SerialName("tournament")
    TOURNAMENT("tournament"),

    @SerialName("league")
    LEAGUE("league"),
}

@Serializable
enum class CalculationType(val type: String) {
    @SerialName("total_score")
    TOTAL_SCORE("total_score"),

    @SerialName("diff_score")
    DIFF_SCORE("diff_score"),

    @SerialName("win_score")
    WIN_SCORE("win_score"),
}

@Serializable
data class Game(
    val id: Int,
    val name: String,
    val description: String,
    val sportId: Int,
    val type: GameType,
    val calculationType: CalculationType,
    val weight: Int,
    val tagId: Int?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedGame(
    val name: String,
    val description: String,
    val sportId: Int,
    val type: GameType,
    val calculationType: CalculationType?,
    val weight: Int,
    val tagId: Int?,
)
