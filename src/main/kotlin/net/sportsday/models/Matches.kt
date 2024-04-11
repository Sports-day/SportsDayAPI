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
object Matches : IntIdTable("matches") {
    val location = reference("location", Locations, onDelete = ReferenceOption.SET_NULL).nullable()
    val game = reference("game", Games, onDelete = ReferenceOption.CASCADE)
    val sport = reference("sport", Sports, onDelete = ReferenceOption.CASCADE)
    val startAt = datetime("started_at")
    val leftTeam = reference("left_team", Teams, onDelete = ReferenceOption.SET_NULL).nullable()
    val rightTeam = reference("right_team", Teams, onDelete = ReferenceOption.SET_NULL).nullable()
    val leftScore = integer("left_score").default(0)
    val rightScore = integer("right_score").default(0)
    val result = enumerationByName<MatchResult>("result", 32).default(MatchResult.DRAW)
    val status = enumerationByName<MatchStatus>("status", 32)
    val note = text("note").nullable()
    val judgeTeam = reference("judge_team_id", Teams, onDelete = null).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class MatchEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MatchEntity>(Matches)

    var location by LocationEntity optionalReferencedOn Matches.location
    var game by GameEntity referencedOn Matches.game
    var sport by SportEntity referencedOn Matches.sport
    var startAt by Matches.startAt
    var leftTeam by TeamEntity optionalReferencedOn Matches.leftTeam
    var rightTeam by TeamEntity optionalReferencedOn Matches.rightTeam
    var leftScore by Matches.leftScore
    var rightScore by Matches.rightScore
    var result by Matches.result
    var status by Matches.status
    var note by Matches.note
    var judgeTeam by TeamEntity optionalReferencedOn Matches.judgeTeam
    var createdAt by Matches.createdAt
    var updatedAt by Matches.updatedAt

    //  for tournament format
    var parents by MatchEntity.via(TournamentPath.child, TournamentPath.parent)
    var children by MatchEntity.via(TournamentPath.parent, TournamentPath.child)

    fun serializableModel(): Match {
        return Match(
            id.value,
            location?.id?.value,
            game.id.value,
            sport.id.value,
            startAt.toString(),
            leftTeam?.id?.value,
            rightTeam?.id?.value,
            leftScore,
            rightScore,
            result,
            status,
            note,
            judgeTeam?.id?.value,
            parents.toList().map { it.id.value },
            children.toList().map { it.id.value },
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
enum class MatchStatus(val status: String) {
    @SerialName("standby")
    STANDBY("standby"),

    @SerialName("in_progress")
    IN_PROGRESS("in_progress"),

    @SerialName("finished")
    FINISHED("finished"),

    @SerialName("cancelled")
    CANCELED("cancelled"),
}

@Serializable
enum class MatchResult(val result: String) {
    @SerialName("left_win")
    LEFT_WIN("left_win"),

    @SerialName("right_win")
    RIGHT_WIN("right_win"),

    @SerialName("draw")
    DRAW("draw"),
}

@Serializable
data class Match(
    val id: Int,
    val locationId: Int?,
    val gameId: Int,
    val sportId: Int,
    val startAt: String,
    val leftTeamId: Int?,
    val rightTeamId: Int?,
    val leftScore: Int,
    val rightScore: Int,
    val result: MatchResult,
    val status: MatchStatus,
    val note: String?,
    val judgeTeamId: Int?,
    val parents: List<Int>,
    val children: List<Int>,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedMatch(
    val locationId: Int?,
    val gameId: Int,
    val sportId: Int,
    val startAt: String,
    val leftTeamId: Int?,
    val rightTeamId: Int?,
    val leftScore: Int,
    val rightScore: Int,
    val result: MatchResult,
    val status: MatchStatus,
    val note: String?,
    val judgeTeamId: Int?,
)
