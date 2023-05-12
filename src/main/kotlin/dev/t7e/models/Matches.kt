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
object Matches : IntIdTable("matches") {
    val location = reference("location", Locations).nullable()
    val game = reference("game", Games)
    val sport = reference("sport", Sports)
    val startAt = datetime("started_at")
    val leftTeam = reference("left_team", Teams).nullable()
    val rightTeam = reference("right_team", Teams).nullable()
    val leftScore = integer("left_score").default(0)
    val rightScore = integer("right_score").default(0)
    val result = enumerationByName<MatchResult>("result", 32).default(MatchResult.DRAW)
    val status = enumerationByName<MatchStatus>("status", 32)
    val note = text("note").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class MatchEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : SmartCache<MatchEntity, Match>(
        entityName = "match",
        table = Matches,
        duration = 5.minutes,
        serializer = { it.serializableModel() }
    )

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
    var createdAt by Matches.createdAt
    var updatedAt by Matches.createdAt

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
            createdAt.toString(),
            updatedAt.toString()
        )
    }
}

enum class MatchStatus(val status: String) {
    STANDBY("standby"),
    IN_PROGRESS("in_progress"),
    FINISHED("finished"),
    CANCELED("cancelled")
}

enum class MatchResult(val result: String) {
    LEFT_WIN("left_win"),
    RIGHT_WIN("right_win"),
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
    val createdAt: String,
    val updatedAt: String
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
)