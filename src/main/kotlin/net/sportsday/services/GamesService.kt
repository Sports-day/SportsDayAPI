package net.sportsday.services

import io.ktor.server.plugins.*
import kotlinx.serialization.Serializable
import net.sportsday.models.*
import net.sportsday.utils.Cache
import net.sportsday.utils.configuration.Key
import net.sportsday.utils.configuration.KeyValueStore
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/05/05
 * @author testusuke
 */
object GamesService {

    fun getAll(filter: Boolean = false): Result<List<Game>> {
        val games = GameEntity.getAll().map { it.second }

        val filteredGames = if (filter) {
            //  fetch tags
            val tags = TagEntity.getAll().map { it.second }

            games.filter {
                //  if tagId is null, contain it
                if (it.tagId == null) {
                    return@filter true
                }

                //  if tag is not found, return true
                val tag = tags.find { tag ->
                    tag.id == it.tagId
                } ?: return@filter true

                //  return tag.enabled
                tag.enabled
            }
        } else {
            games
        }

        return Result.success(
            filteredGames
        )
    }

    fun getById(id: Int): Result<Game> {
        val game = GameEntity.getById(id)?.second ?: throw NotFoundException("invalid game id")

        return Result.success(game)
    }

    fun deleteById(id: Int): Result<Boolean> {
        val game = GameEntity.getById(id) ?: throw NotFoundException("invalid game id")

        transaction {
            game.first.delete()
        }

        //  fetch
        GameEntity.fetch(id)
        SportEntity.fetch(game.second.sportId)

        return Result.success(true)
    }

    fun create(omittedGame: OmittedGame): Result<Game> {
        val sport = SportEntity.getById(omittedGame.sportId)?.first ?: throw NotFoundException("invalid sport id")
        val tag = omittedGame.tagId?.let {
            TagEntity.getById(it)
        }

        val model = transaction {
            GameEntity.new {
                this.name = omittedGame.name
                this.description = omittedGame.description
                this.sport = sport
                this.type = omittedGame.type
                this.calculationType = omittedGame.calculationType ?: CalculationType.DIFF_SCORE
                this.weight = omittedGame.weight
                this.tag = tag?.first
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }.serializableModel()
        }.apply {
            GameEntity.fetch(this.id)
            SportEntity.fetch(this.sportId)
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedGame: OmittedGame): Result<Game> {
        val entity = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        val sport = SportEntity.getById(omittedGame.sportId)?.first ?: throw NotFoundException("invalid sport id")
        val tag = omittedGame.tagId?.let {
            TagEntity.getById(it)
        }

        val model = transaction {
            entity.name = omittedGame.name
            entity.description = omittedGame.description
            entity.sport = sport
            entity.type = omittedGame.type
            entity.calculationType = omittedGame.calculationType ?: CalculationType.DIFF_SCORE
            entity.weight = omittedGame.weight
            entity.tag = tag?.first
            entity.updatedAt = LocalDateTime.now()
            //  serialize
            entity.serializableModel()
        }.apply {
            GameEntity.fetch(this.id)
        }

        return Result.success(model)
    }

    /**
     * Get entries of game
     *
     * @param id game id
     * @return entries
     */
    fun getEntries(id: Int): Result<List<Team>> {
        val entries = GameEntity.getGameEntries(id)?.map { it.second } ?: throw NotFoundException("invalid game id")

        return Result.success(entries)
    }

    /**
     * Enter game
     *
     * @param id game id
     * @param teamIds team ids
     * @return teams
     */
    fun enterGame(id: Int, teamIds: List<Int>): Result<List<Team>> {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        val teams = teamIds.mapNotNull {
            TeamEntity.getById(it)?.first
        }

        val teamModels = transaction {
            game.teams = SizedCollection(listOf(game.teams.toList(), teams).flatten().distinct())
            game.updatedAt = LocalDateTime.now()

            //  serialize
            game.teams.map { it.serializableModel() }
        }.apply {
            transaction {
                //  fetch
                GameEntity.fetch(id)
                teams.forEach { team ->
                    TeamEntity.fetch(team.id.value)
                }
            }
        }

        return Result.success(teamModels)
    }

    /**
     * cancel entry
     *
     * @param id game id
     * @param teamId team id
     * @return teams
     */
    fun cancelEntry(id: Int, teamId: Int): Result<List<Team>> {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        val team = TeamEntity.getById(teamId)?.first ?: throw NotFoundException("invalid team id")

        val teamModels = transaction {
            game.teams = SizedCollection(game.teams.filterNot { it.id.value == team.id.value })
            game.updatedAt = LocalDateTime.now()
            //  serialize
            game.teams.map { it.serializableModel() }
        }

        //  fetch
        GameEntity.fetch(id)
        TeamEntity.fetch(teamId)

        return Result.success(teamModels)
    }

    /**
     * Get matches of game
     *
     * @param id game id
     * @return matches
     */
    fun getMatches(id: Int, restrict: Boolean = false): Result<List<Match>> {
        val matches = GameEntity.getGameMatches(id)?.map { it.second } ?: throw NotFoundException("invalid game id")

        if (restrict) {
            assertRestrictedGame(matches)
        }

        return Result.success(matches)
    }

    /**
     * delete all matches of game
     *
     * @param id game id
     */
    fun deleteAllMatches(id: Int): Result<Unit> {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")

        transaction {
            //  delete
            game.matches.forEach {
                it.delete()
            }
        }

        //  delete cache
        MatchEntity.fetch()
        //  fetch
        GameEntity.fetch(id)

        return Result.success(Unit)
    }

    /**
     * Make league matches automatically
     *
     * @param id game id
     * @return matches
     */
    fun makeLeagueMatches(id: Int, defaultLocationId: Int?): Result<List<Match>> {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")
        val location = defaultLocationId?.let {
            LocationEntity.getById(it)?.first ?: throw NotFoundException("invalid location id")
        }

        val matchModels = transaction {
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
                        },
                    )
                }
            }

            matches.map { it.serializableModel() }
        }

        //  fetch
        MatchEntity.fetch()
        GameEntity.fetch(id)

        return Result.success(matchModels)
    }

    /**
     * Make new tree for tournament
     *
     * @param id game id
     * @param parentMatchId parent match id
     * @return match
     */
    fun makeTournamentTree(id: Int, parentMatchId: Int?): Result<Match> {
        val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")

        val model = transaction {
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

            match.serializableModel()
        }

        //  fetch
        GameEntity.fetch(id)
        MatchEntity.fetch(model.id)

        return Result.success(model)
    }

    /**
     * Calculate league results
     *
     * @param id game id
     */
    fun calculateLeagueResults(id: Int, restrict: Boolean = false): Result<LeagueResult> {
        val result = transaction {
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
            val unfilteredMatches = GameEntity.getGameMatches(id)?.map { it.second } ?: throw NotFoundException("invalid game id")
            val matches = unfilteredMatches.filter { it.status == MatchStatus.FINISHED }

            //  restrict preview
            if (restrict) {
                assertRestrictedGame(unfilteredMatches)
            }

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
                    goalDiff = 0,
                )
            }

            //  calculate win score and diffGoal from each match
            matches.forEach { match ->
                val leftTeamResult = leagueTeamResults[match.leftTeamId] ?: return@forEach
                val rightTeamResult = leagueTeamResults[match.rightTeamId] ?: return@forEach

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

            var lastResult: LeagueTeamResult? = null
            var lastRank = 0
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
                        },
                )
                .map { leagueTeamResult ->
                    if (lastResult == null) {
                        leagueTeamResult.rank = 1
                    } else {
                        //  if score is same, rank is same
                        if (game.calculationType == CalculationType.TOTAL_SCORE) {
                            if (
                                lastResult!!.score == leagueTeamResult.score &&
                                lastResult!!.goal == leagueTeamResult.goal
                            ) {
                                leagueTeamResult.rank = lastRank
                            } else {
                                leagueTeamResult.rank = lastRank + 1
                            }
                        } else {
                            if (
                                lastResult!!.score == leagueTeamResult.score &&
                                lastResult!!.goalDiff == leagueTeamResult.goalDiff
                            ) {
                                leagueTeamResult.rank = lastRank
                            } else {
                                leagueTeamResult.rank = lastRank + 1
                            }
                        }
                    }

                    //  last result
                    lastResult = leagueTeamResult
                    lastRank = leagueTeamResult.rank

                    leagueTeamResult
                }

            //  create league result
            LeagueResult(
                gameId = game.id.value,
                //  is finished
                finished = unfilteredMatches.size == matches.size,
                teams = sortedLeagueTeamResults,
                createdAt = LocalDateTime.now().toString(),
            )
        }

        return Result.success(result)
    }

    /**
     * update tournament tree
     *
     * @param id game id
     */
    fun updateTournamentTree(id: Int): Result<Unit> {
        transaction {
            val game = GameEntity.getById(id)?.first ?: throw NotFoundException("invalid game id")

            //  check if game type is tournament
            if (game.type != GameType.TOURNAMENT) {
                throw BadRequestException("invalid game type")
            }

            //  find top node
            val topNode =
                game.matches.toList().find { it.parents.count() <= 0 }
                    ?: throw BadRequestException("cannot find top node")

            //  update tree recursively
            updateTree(topNode)
        }

        GameEntity.fetch(id)
        //  fetch matches
        val matches = GameEntity.getGameMatches(id)
        matches?.forEach { match ->
            MatchEntity.fetch(match.second.id)
        }

        return Result.success(Unit)
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
//            MatchEntity.fetch(match.id.value)

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
            val topNode = game.matches.toList().find { it.parents.count() <= 0 }
                ?: throw BadRequestException("cannot find top node")

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
                            topNode.rightTeam?.id?.value
                                ?: throw Exception("something went wrong. right team id is null")
                        },
                        rank = 1,
                    ),
                    TournamentTeamResult(
                        teamId = if (topNode.result == MatchResult.LEFT_WIN) {
                            topNode.rightTeam?.id?.value
                                ?: throw Exception("something went wrong. right team id is null")
                        } else {
                            topNode.leftTeam?.id?.value ?: throw Exception("something went wrong. left team id is null")
                        },
                        rank = 2,
                    ),
                ),
                createdAt = LocalDateTime.now().toString(),
            )

            Result.success(tournamentResult)
        }
    }

    private fun assertRestrictedGame(matches: List<Match>) {
        if (KeyValueStore.get(Key.RestrictGamePreview).toBoolean()) {
            val finishedMatchCount = matches.count { it.status == MatchStatus.FINISHED }
            val percentage = finishedMatchCount.toDouble() / matches.size.toDouble()

            if (percentage >= (KeyValueStore.get(Key.RestrictGamePreviewPercentage)?.toDoubleOrNull() ?: 0.5)) {
                throw GamePreviewRestrictedException("this game preview is currently restricted.")
            }
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
    var goalDiff: Int,
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
    val rank: Int,
)

class GamePreviewRestrictedException(message: String) : Exception(message)
