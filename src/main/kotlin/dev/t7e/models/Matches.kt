package dev.t7e.models

import dev.t7e.utils.Cache
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

class Match(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Match>(Matches) {
        val getAllMatchesWithTeam: (team: Team) -> MutableList<Match> = Cache.memoize(1000 * 60 * 1) { team ->
            transaction {
                Match.find {
                    Matches.leftTeam eq team.id or
                            (Matches.rightTeam eq team.id)
                }.sortedBy {
                    it.startAt
                }.toMutableList()
            }
        }
    }

    var location by Location referencedOn Matches.location
    var game by Game referencedOn  Matches.game
    var sport by Sport referencedOn Matches.sport
    var startAt by Matches.startAt
    var leftTeam by Team optionalReferencedOn Matches.leftTeam
    var rightTeam by Team optionalReferencedOn Matches.rightTeam
    var leftScore by Matches.leftScore
    var rightScore by Matches.rightScore
    var winner by Team optionalReferencedOn Matches.winner
    val status by Matches.status
    //  for tournament format
    var parents by Match.via(TournamentPath.child, TournamentPath.parent)
    var children by Match.via(TournamentPath.parent, TournamentPath.child)
}

enum class MatchStatus(val status: String) {
    STANDBY("standby"),
    IN_PROGRESS("in_progress"),
    FINISH("finish"),
    CANCELED("cancel"),
    NONE("none")
}