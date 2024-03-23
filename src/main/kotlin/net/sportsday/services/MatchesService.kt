package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/05/07
 * @author testusuke
 */
object MatchesService {

    fun getAll(): Result<List<Match>> {
        val models = transaction {
            MatchEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(
            models
        )
    }

    fun getById(id: Int): Result<Match> {
        val model = transaction {
            MatchEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Match not found.")
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedMatch: OmittedMatch): Result<Match> {
        val model = transaction {
            val match = MatchEntity.findById(id) ?: throw NotFoundException("invalid match id")
            val location = omittedMatch.locationId?.let {
                LocationEntity.findById(it) ?: throw NotFoundException("invalid location id")
            }
            val game = GameEntity.findById(omittedMatch.gameId) ?: throw NotFoundException("invalid game id")
            val sport = SportEntity.findById(omittedMatch.sportId) ?: throw NotFoundException("invalid sport id")
            val leftTeam = omittedMatch.leftTeamId?.let {
                TeamEntity.findById(it) ?: throw NotFoundException("invalid left team id")
            }
            val rightTeam = omittedMatch.rightTeamId?.let {
                TeamEntity.findById(it) ?: throw NotFoundException("invalid right team id")
            }

            //  update
            match.location = location
            match.game = game
            match.sport = sport
            match.startAt = LocalDateTime.parse(omittedMatch.startAt)
            match.leftTeam = leftTeam
            match.rightTeam = rightTeam
            match.leftScore = omittedMatch.leftScore
            match.rightScore = omittedMatch.rightScore
            match.result = omittedMatch.result
            match.status = omittedMatch.status
            match.note = omittedMatch.note
            match.judge = omittedMatch.judge

            match.updatedAt = LocalDateTime.now()

            //  serialize
            match.serializableModel()
        }

        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val match = MatchEntity.findById(id) ?: throw NotFoundException("invalid match id")

            //  delete
            match.delete()
        }

        return Result.success(Unit)
    }
}
