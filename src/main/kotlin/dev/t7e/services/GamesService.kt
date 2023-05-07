package dev.t7e.services

import dev.t7e.models.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/05/05
 * @author testusuke
 */
object GamesService: StandardService<GameEntity, Game>(
    objectName = "game",
    _getAllObjectFunction = { GameEntity.getAll() },
    _getObjectByIdFunction = { GameEntity.getById(it) },
    fetchFunction = { GameEntity.fetch(it) }
) {

    fun create(omittedGame: OmittedGame): Result<Game> = transaction {
        val sport = SportEntity.getById(omittedGame.sportId)?.first ?: throw NotFoundException("invalid sport id")

        val entity = GameEntity.new {
            this.name = omittedGame.name
            this.description = omittedGame.description
            this.sport = sport
            this.type = omittedGame.type
            this.weight = omittedGame.weight
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }

        Result.success(
            entity.serializableModel().apply {
                fetchFunction(this.id)
            }
        )
    }

    fun update(id: Int, omittedGame: OmittedGame): Result<Game> = transaction {
        val entity = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        val sport = SportEntity.getById(omittedGame.sportId)?.first ?: throw NotFoundException("invalid sport id")

        entity.name = omittedGame.name
        entity.description = omittedGame.description
        entity.sport = sport
        entity.type = omittedGame.type
        entity.weight = omittedGame.weight
        entity.updatedAt = LocalDateTime.now()

        Result.success(
            entity.serializableModel().apply {
                fetchFunction(this.id)
            }
        )
    }

    /**
     * Get entries of game
     *
     * @param id game id
     * @return entries
     */
    fun getEntries(id: Int): Result<List<Team>> = transaction {
        val entries = GameEntity.getGameEntries(id)?.map { it.second } ?: throw NotFoundException("invalid game id")

        Result.success(entries)
    }

    /**
     * Enter game
     *
     * @param id game id
     * @param teamIds team ids
     * @return teams
     */
    fun enterGame(id: Int, teamIds: List<Int>): Result<List<Team>> = transaction {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        val teams = teamIds.mapNotNull {
            TeamEntity.getById(it)?.first
        }

        game.teams = SizedCollection(listOf(game.teams.toList(), teams).flatten().distinct())
        game.updatedAt = LocalDateTime.now()

        //  fetch
        fetchFunction(id)

        Result.success(
            game.teams.map { it.serializableModel() }
        )
    }

    /**
     * cancel entry
     *
     * @param id game id
     * @param teamId team id
     * @return teams
     */
    fun cancelEntry(id: Int, teamId: Int): Result<List<Team>> = transaction {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        val team = TeamEntity.getById(teamId)?.first ?: throw NotFoundException("invalid team id")

        game.teams = SizedCollection(game.teams.filterNot { it.id.value == team.id.value })
        game.updatedAt = LocalDateTime.now()

        //  fetch
        fetchFunction(id)

        Result.success(
            game.teams.map { it.serializableModel() }
        )
    }

    /**
     * Get matches of game
     *
     * @param id game id
     * @return matches
     */
    fun getMatches(id: Int): Result<List<Match>> = transaction {
        val matches = GameEntity.getGameMatches(id)?.map { it.second } ?: throw NotFoundException("invalid game id")

        Result.success(matches)
    }

    /**
     * delete all matches of game
     *
     * @param id game id
     */
    fun deleteAllMatches(id: Int): Result<Unit> = transaction {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")

        //  delete
        game.matches.forEach {
            it.delete()
        }

        //  fetch
        fetchFunction(id)

        Result.success(Unit)
    }


    /**
     * Make league matches automatically
     *
     * @param id game id
     * @return matches
     */
    fun makeLeagueMatches(id: Int, defaultLocationId: Int?): Result<List<Match>> = transaction {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        //  check if game type is league
        if (game.type != GameType.LEAGUE) {
            throw BadRequestException("invalid game type")
        }

        val teams = game.teams.toList()
        //  check if teams count is enough to make league matches
        if (teams.size < 2) {
            throw BadRequestException("two more teams are required to make league matches")
        }

        val location = defaultLocationId?.let {
            LocationEntity.getById(it)?.first ?: throw NotFoundException("invalid location id")
        }

        val matches = mutableListOf<MatchEntity>()

        for (i in teams.indices) {
            for (j in i + 1 until teams.size) {
                matches.add(
                    MatchEntity.new {
                        this.location = location
                        this.game = game
                        this.sport = game.sport
                        this.startAt = LocalDateTime.now()
                        this.leftTeam = teams[i]
                        this.rightTeam = teams[j]
                        this.status = MatchStatus.STANDBY
                        this.createdAt = LocalDateTime.now()
                        this.updatedAt = LocalDateTime.now()
                    }
                )
            }
        }

        //  fetch
        fetchFunction(id)

        Result.success(
            matches.map { it.serializableModel() }
        )
    }

    /**
     * Make new tree for tournament
     *
     * @param id game id
     * @param parentMatchId parent match id
     * @return match
     */
    fun makeTournamentTree(id: Int, parentMatchId: Int?): Result<Match> = transaction {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        //  check if game type is tournament
        if (game.type != GameType.TOURNAMENT) {
            throw BadRequestException("invalid game type")
        }

        val parentMatch = if (parentMatchId != null) {
            MatchEntity.getById(parentMatchId)?.first ?: throw NotFoundException("invalid parent match id")
        } else {
            null
        }

        //  check if child match count under two
        if (parentMatch != null && parentMatch.children.count() >= 2) {
            throw BadRequestException("cannot have two more child matches")
        }

        val match = MatchEntity.new {
            this.game = game
            this.sport = game.sport
            this.startAt = LocalDateTime.now()
            this.status = MatchStatus.STANDBY
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }

        parentMatch?.let {
            //  register parent
            match.parents = SizedCollection(listOf(parentMatch))

            //  add child to parent
            parentMatch.children = SizedCollection(listOf(parentMatch.children.toList(), listOf(match)).flatten().distinct())
        }

        //  fetch
        fetchFunction(id)

        Result.success(
            match.serializableModel()
        )
    }

}