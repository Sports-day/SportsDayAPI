package dev.t7e.services

import dev.t7e.models.*
import dev.t7e.utils.Cache
import io.ktor.server.plugins.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/05/05
 * @author testusuke
 */
object GamesService : StandardService<GameEntity, Game>(
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
            this.calculationType = omittedGame.calculationType ?: CalculationType.DIFF_SCORE
            this.weight = omittedGame.weight
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }

        Result.success(
            entity.serializableModel().apply {
                fetchFunction(this.id)
                SportEntity.fetch(this.sportId)
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
        entity.calculationType = omittedGame.calculationType ?: CalculationType.DIFF_SCORE
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
            val matchId = it.id.value
            it.delete()

            //  delete cache
            MatchEntity.fetch(matchId)
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

        //  check if league matches already generated
        if (!game.matches.empty()) {
            throw BadRequestException("league matches already generated")
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
                    }.also {
                        //  fetch
                        MatchEntity.fetch(it.id.value)
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
            //  check if top node already exists
            if (game.matches.count() >= 1) {
                throw BadRequestException("top node already exists")
            }

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
            parentMatch.children =
                SizedCollection(listOf(parentMatch.children.toList(), listOf(match)).flatten().distinct())
        }

        //  fetch
        fetchFunction(id)
        MatchEntity.fetch(match.id.value)

        Result.success(
            match.serializableModel()
        )
    }

    /**
     * Calculate league results
     *
     * @param id game id
     */
    val calculateLeagueResults: (id: Int) -> Result<LeagueResult> = Cache.memoize(1.minutes) { id ->
        transaction {
            val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")

            //  check if game type is league
            if (game.type != GameType.LEAGUE) {
                throw BadRequestException("invalid game type")
            }

            /**
             * 勝ち点制を採用する。
             * 勝ち: 3点
             * 引き分け: 1点
             * 負け: 0点
             * 勝ち点が同じ場合は得失点差で順位を決める。
             *
             * それぞれのマッチから勝ち点と得失点差を計算し、チームごとに集計する。
             * 集計結果を元に順位を決める。
             */

            val teams = game.teams.toList()
            val unfilteredMatches = game.matches.toList()
            val matches = unfilteredMatches.filter { it.status == MatchStatus.FINISHED }

            //  create LeagueTeamResult
            val leagueTeamResults = mutableMapOf<Int, LeagueTeamResult>()
            teams.forEach { team ->
                leagueTeamResults[team.id.value] = LeagueTeamResult(
                    teamId = team.id.value,
                    rank = 0,
                    win = 0,
                    lose = 0,
                    draw = 0,
                    score = 0,
                    goal = 0,
                    loseGoal = 0,
                    goalDiff = 0
                )
            }

            //  calculate win score and diffGoal from each match
            matches.forEach { match ->
                val leftTeamResult = leagueTeamResults[match.leftTeam?.id?.value] ?: return@forEach
                val rightTeamResult = leagueTeamResults[match.rightTeam?.id?.value] ?: return@forEach

                when (match.result) {
                    //  draw
                    MatchResult.DRAW -> {
                        leftTeamResult.draw += 1
                        rightTeamResult.draw += 1

                        // score
                        leftTeamResult.score += 1
                        rightTeamResult.score += 1
                    }
                    //  left team win
                    MatchResult.LEFT_WIN -> {
                        leftTeamResult.win += 1
                        rightTeamResult.lose += 1

                        // score
                        leftTeamResult.score += 3
                    }
                    //  right team win
                    MatchResult.RIGHT_WIN -> {
                        leftTeamResult.lose += 1
                        rightTeamResult.win += 1

                        // score
                        rightTeamResult.score += 3
                    }
                }

                //  goal
                leftTeamResult.goal += match.leftScore
                leftTeamResult.loseGoal += match.rightScore
                leftTeamResult.goalDiff += match.leftScore - match.rightScore

                rightTeamResult.goal += match.rightScore
                rightTeamResult.loseGoal += match.leftScore
                rightTeamResult.goalDiff += match.rightScore - match.leftScore
            }

            //  sort by score. but if score is same, sort by goal diff
            val sortedLeagueTeamResults = leagueTeamResults.values
                .sortedWith(
                    compareByDescending<LeagueTeamResult> { it.score }
                        .apply {
                            if (game.calculationType == CalculationType.DIFF_SCORE) {
                                thenByDescending { it.goalDiff }
                            } else if (game.calculationType == CalculationType.TOTAL_SCORE) {
                                thenByDescending { it.goal }
                            }
                        }
                )
                .mapIndexed { index, leagueTeamResult ->
                    //  rank
                    leagueTeamResult.rank = index + 1

                    leagueTeamResult
                }

            //  create league result
            val leagueResult = LeagueResult(
                gameId = game.id.value,
                //  is finished
                finished = unfilteredMatches.size == matches.size,
                teams = sortedLeagueTeamResults,
                createdAt = LocalDateTime.now().toString()
            )

            Result.success(leagueResult)
        }

    }

    /**
     * update tournament tree
     *
     * @param id game id
     */
    fun updateTournamentTree(id: Int): Result<Unit> = transaction {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")

        //  check if game type is tournament
        if (game.type != GameType.TOURNAMENT) {
            throw BadRequestException("invalid game type")
        }

        //  find top node
        val topNode = game.matches.toList().find { it.parents.count() <= 0 } ?: throw BadRequestException("cannot find top node")

        //  update tree recursively
        updateTree(topNode)

        Result.success(Unit)
    }

    private fun updateTree(match: MatchEntity): TeamEntity? {
        if (match.children.count().toInt() != 2 && match.children.count().toInt() != 0) {
            throw BadRequestException("invalid tree")
        }

        if (match.children.count().toInt() == 2) {
            if (match.status == MatchStatus.FINISHED) {
                return when (match.result) {
                    MatchResult.LEFT_WIN -> {
                        match.leftTeam
                    }

                    MatchResult.RIGHT_WIN -> {
                        match.rightTeam
                    }

                    MatchResult.DRAW -> {
                        null
                    }
                }
            }

            val leftChild = match.children.first()
            val rightChild = match.children.last()

            //  update left child
            val leftChildWin = updateTree(leftChild)

            //  update right child
            val rightChildWin = updateTree(rightChild)

            //  update match
            match.leftTeam = leftChildWin
            match.rightTeam = rightChildWin
            match.updatedAt = LocalDateTime.now()

            //  fetch
            MatchEntity.fetch(match.id.value)

            return null
        } else {
            //  if not finished return null
            if (match.status != MatchStatus.FINISHED) {
                return null
            }

            return when (match.result) {
                MatchResult.LEFT_WIN -> {
                    match.leftTeam
                }

                MatchResult.RIGHT_WIN -> {
                    match.rightTeam
                }

                MatchResult.DRAW -> {
                    null
                }
            }
        }
    }

    /**
     * get tournament result
     *
     * @param id game id
     */
    val getTournamentResult: (id: Int) -> Result<TournamentResult> = Cache.memoize(1.minutes) { id ->
        transaction {
            val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")

            //  check if game type is tournament
            if (game.type != GameType.TOURNAMENT) {
                throw BadRequestException("invalid game type")
            }

            //  find top node
            val topNode = game.matches.toList().find { it.parents.count() <= 0 } ?: throw BadRequestException("cannot find top node")

            //  check if game is finished
            if (topNode.status != MatchStatus.FINISHED) {
                throw BadRequestException("game is not finished")
            }

            //  check if result is draw
            if (topNode.result == MatchResult.DRAW) {
                throw BadRequestException("game result is draw")
            }

            //  check if left or right team is null
            if (topNode.leftTeam == null || topNode.rightTeam == null) {
                throw BadRequestException("invalid tournament tree")
            }

            //  create tournament result
            val tournamentResult = TournamentResult(
                gameId = game.id.value,
                teams = listOf(
                    TournamentTeamResult(
                        teamId = if (topNode.result == MatchResult.LEFT_WIN) {
                            topNode.leftTeam?.id?.value ?: throw Exception("something went wrong. left team id is null")
                        } else {
                            topNode.rightTeam?.id?.value ?: throw Exception("something went wrong. right team id is null")
                        },
                        rank = 1
                    ),
                    TournamentTeamResult(
                        teamId = if (topNode.result == MatchResult.LEFT_WIN) {
                            topNode.rightTeam?.id?.value ?: throw Exception("something went wrong. right team id is null")
                        } else {
                            topNode.leftTeam?.id?.value ?: throw Exception("something went wrong. left team id is null")
                        },
                        rank = 2
                    )
                ),
                createdAt = LocalDateTime.now().toString()
            )

            Result.success(tournamentResult)
        }
    }
}

@Serializable
data class LeagueResult(
    val gameId: Int,
    val finished: Boolean,
    val teams: List<LeagueTeamResult>,
    val createdAt: String,
)

@Serializable
data class LeagueTeamResult(
    val teamId: Int,
    var rank: Int,
    //  3 point
    var win: Int,
    //  1 point
    var lose: Int,
    //  0 point
    var draw: Int,
    //  total score
    var score: Int,
    var goal: Int,
    var loseGoal: Int,
    var goalDiff: Int
)

@Serializable
data class TournamentResult(
    val gameId: Int,
    val teams: List<TournamentTeamResult>,
    val createdAt: String,
)

@Serializable
data class TournamentTeamResult(
    val teamId: Int,
    val rank: Int
)