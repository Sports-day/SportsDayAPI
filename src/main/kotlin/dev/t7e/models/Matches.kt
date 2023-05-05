package dev.t7e.models

import dev.t7e.utils.Cache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */
object Matches : IntIdTable("matches") {
    val location = reference("location", Locations)
    val game = reference("game", Games)
    val sport = reference("sport", Sports)
    val startAt = datetime("started_at")
    val leftTeam = reference("left_team", Teams).nullable()
    val rightTeam = reference("right_team", Teams).nullable()
    val leftScore = integer("left_score").default(0)
    val rightScore = integer("right_score").default(0)
    val winner = reference("winner", Teams).nullable()
    val status = enumerationByName<MatchStatus>("status", 32)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class MatchEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MatchEntity>(Matches) {
        val getAllMatchesWithTeam: (team: TeamEntity) -> MutableList<MatchEntity> = Cache.memoize { team ->
            transaction {
                MatchEntity.find {
                    Matches.leftTeam eq team.id or
                            (Matches.rightTeam eq team.id)
                }.sortedBy {
                    it.startAt
                }.toMutableList()
            }
        }
    }

    var location by LocationEntity referencedOn Matches.location
    var game by GameEntity referencedOn Matches.game
    var sport by SportEntity referencedOn Matches.sport
    var startAt by Matches.startAt
    var leftTeam by TeamEntity optionalReferencedOn Matches.leftTeam
    var rightTeam by TeamEntity optionalReferencedOn Matches.rightTeam
    var leftScore by Matches.leftScore
    var rightScore by Matches.rightScore
    var winner by TeamEntity optionalReferencedOn Matches.winner
    var status by Matches.status
    var createdAt by Matches.createdAt
    var updatedAt by Matches.createdAt

    //  for tournament format
    var parents by MatchEntity.via(TournamentPath.child, TournamentPath.parent)
    var children by MatchEntity.via(TournamentPath.parent, TournamentPath.child)

    fun serializableModel(): Match {
        return Match(
            id.value,
            location.id.value,
            game.id.value,
            sport.id.value,
            startAt.toString(),
            leftTeam?.id?.value,
            rightTeam?.id?.value,
            leftScore,
            rightScore,
            winner?.id?.value,
            status,
            createdAt.toString(),
            updatedAt.toString()
        )
    }
}

enum class MatchStatus(val status: String) {
    STANDBY("standby"),
    IN_PROGRESS("in_progress"),
    FINISH("finish"),
    CANCELED("cancel"),
    NONE("none")
}

@Serializable
data class Match(
    val id: Int,
    val locationId: Int,
    val gameId: Int,
    val sportId: Int,
    val startAt: String,
    val leftTeamId: Int?,
    val rightTeamId: Int?,
    val leftScore: Int,
    val rightScore: Int,
    val winnerId: Int?,
    val status: MatchStatus,
    val createdAt: String,
    val updatedAt: String
)