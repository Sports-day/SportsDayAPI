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
object Matches: IntIdTable("matches") {
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
}

class MatchEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<MatchEntity>(Matches) {
        val getAllMatchesWithTeam: (team: TeamEntity) -> MutableList<MatchEntity> = Cache.memoize(1000 * 60 * 1) { team ->
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
    var game by GameEntity referencedOn  Matches.game
    var sport by SportEntity referencedOn Matches.sport
    var startAt by Matches.startAt
    var leftTeam by TeamEntity optionalReferencedOn Matches.leftTeam
    var rightTeam by TeamEntity optionalReferencedOn Matches.rightTeam
    var leftScore by Matches.leftScore
    var rightScore by Matches.rightScore
    var winner by TeamEntity optionalReferencedOn Matches.winner
    val status by Matches.status
    //  for tournament format
    var parents by MatchEntity.via(TournamentPath.child, TournamentPath.parent)
    var children by MatchEntity.via(TournamentPath.parent, TournamentPath.child)

    fun serializableModel(): Match {
        return Match(
            id.value,
            location.serializableModel(),
            game.serializableModel(),
            sport.serializableModel(),
            startAt.toString(),
            leftTeam?.serializableModel(),
            rightTeam?.serializableModel(),
            leftScore,
            rightScore,
            winner?.serializableModel(),
            status,
            parents.map(MatchEntity::serializableModel),
            children.map(MatchEntity::serializableModel)
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
    val location: Location,
    val game: Game,
    val sport: Sport,
    val startAt: String,
    val leftTeam: Team?,
    val rightTeam: Team?,
    val leftScore: Int,
    val rightScore: Int,
    val winner: Team?,
    val status: MatchStatus,
    val parents: List<Match>,
    val children: List<Match>
)