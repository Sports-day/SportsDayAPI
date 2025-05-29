package net.sportsday.services

import io.ktor.server.plugins.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.sportsday.models.*
import net.sportsday.utils.Cache
import net.sportsday.utils.configuration.Key
import net.sportsday.utils.configuration.KeyValueStore
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

private val Double.format8: String get() = "%,.8f".format(this)

/**
 * Created by testusuke on 2023/05/05
 * @author testusuke
 */
object GamesService {

    fun getAll(filter: Boolean = false): Result<List<Game>> {
        val models = transaction {
            val games = GameEntity.all().map { it.serializableModel() }

            if (filter) {
                //  fetch tags
                val tags = TagEntity.all().map { it.serializableModel() }

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
        }

        return Result.success(
            models
        )
    }

    fun getById(id: Int): Result<Game> {
        val model = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

            game.serializableModel()
        }

        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

            game.delete()
        }

        return Result.success(Unit)
    }

    fun create(omittedGame: OmittedGame): Result<Game> {
        val model = transaction {
            val sport = SportEntity.findById(omittedGame.sportId) ?: throw NotFoundException("invalid sport id")
            val tag = omittedGame.tagId?.let {
                TagEntity.findById(it)
            }

            val game = GameEntity.new {
                this.name = omittedGame.name
                this.description = omittedGame.description
                this.sport = sport
                this.type = omittedGame.type
                this.calculationType = omittedGame.calculationType ?: CalculationType.DIFF_SCORE
                this.weight = omittedGame.weight
                this.tag = tag
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            game.serializableModel()
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedGame: OmittedGame): Result<Game> {
        val model = transaction {
            val entity = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")
            val sport = SportEntity.findById(omittedGame.sportId) ?: throw NotFoundException("invalid sport id")
            val tag = omittedGame.tagId?.let {
                TagEntity.findById(it)
            }

            entity.name = omittedGame.name
            entity.description = omittedGame.description
            entity.sport = sport
            entity.type = omittedGame.type
            entity.calculationType = omittedGame.calculationType ?: CalculationType.DIFF_SCORE
            entity.weight = omittedGame.weight
            entity.tag = tag
            entity.updatedAt = LocalDateTime.now()

            //  serialize
            entity.serializableModel()
        }

        return Result.success(model)
    }

    /**
     * Get entries(teams) of game
     *
     * @param id game id
     * @return entries
     */
    fun getEntries(id: Int): Result<List<Team>> {
        val models = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

            game.teams.map { it.serializableModel() }
        }
        return Result.success(models)
    }

    /**
     * Enter game
     *
     * @param id game id
     * @param teamIds team ids
     * @return teams
     */
    fun enterGame(id: Int, teamIds: List<Int>): Result<List<Team>> {
        val models = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")
            val teams = teamIds.mapNotNull {
                TeamEntity.findById(it)
            }

            game.teams = SizedCollection(listOf(game.teams.toList(), teams).flatten().distinct())
            game.updatedAt = LocalDateTime.now()

            //  serialize
            game.teams.map { it.serializableModel() }
        }

        return Result.success(models)
    }

    /**
     * cancel entry
     *
     * @param id game id
     * @param teamId team id
     * @return teams
     */
    fun cancelEntry(id: Int, teamId: Int): Result<List<Team>> {
        val models = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")
            val team = TeamEntity.findById(teamId) ?: throw NotFoundException("invalid team id")

            game.teams = SizedCollection(game.teams.filterNot { it.id.value == team.id.value })
            game.updatedAt = LocalDateTime.now()

            //  serialize
            game.teams.map { it.serializableModel() }
        }

        return Result.success(models)
    }

    /**
     * Get matches of game
     *
     * @param id game id
     * @return matches
     */
    fun getMatches(id: Int, restrict: Boolean = false): Result<List<Match>> {
        val models = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")
            val matches = game.matches.map { it.serializableModel() }

            if (restrict) {
                assertRestrictedGame(matches)
            }

            matches
        }

        return Result.success(models)
    }

    /**
     * delete all matches of game
     *
     * @param id game id
     */
    fun deleteAllMatches(id: Int): Result<Unit> {
        transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

            //  delete
            game.matches.forEach {
                it.delete()
            }
        }

        return Result.success(Unit)
    }

    /**
     * Make league matches automatically
     *
     * @param id game id
     * @return matches
     */
    fun makeLeagueMatches(id: Int, defaultLocationId: Int?): Result<List<Match>> {
        val models = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")
            val location = defaultLocationId?.let {
                LocationEntity.findById(it) ?: throw NotFoundException("invalid location id")
            }

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

        return Result.success(models)
    }

    /**
     * Make new tree for tournament
     *
     * @param id game id
     * @param parentMatchId parent match id
     * @return match
     */
    fun makeTournamentTree(id: Int, parentMatchId: Int?): Result<Match> {
        val model = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

            //  check if game type is tournament
            if (game.type != GameType.TOURNAMENT) {
                throw BadRequestException("invalid game type")
            }

            val parentMatch = if (parentMatchId != null) {
                MatchEntity.findById(parentMatchId) ?: throw NotFoundException("invalid parent match id")
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

        return Result.success(model)
    }

    /**
     * Calculate league results
     *
     * @param id game id
     */
    fun calculateLeagueResults(id: Int, restrict: Boolean = false): Result<LeagueResult> {
        val DEBUG = true
        val EPSILON = 1e-6
        fun d(msg: String) { if (DEBUG) println(msg) }

        val result = transaction {
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

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
            val unfilteredMatches = game.matches.map { it.serializableModel() }

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
                    score = 0.0,
                    goal = 0.0,
                    loseGoal = 0.0,
                    goalDiff = 0.0,
                    matchCount = 0,
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

                //  match count
                leftTeamResult.matchCount += 1
                rightTeamResult.matchCount += 1
            }

            //  convert rate
            leagueTeamResults.values.forEach {
                //  print each team result
                val matchCount = unfilteredMatches.count { match ->
                    match.leftTeamId == it.teamId || match.rightTeamId == it.teamId
                }

                if (matchCount > 0) {
                    it.score /= matchCount.toDouble()
                    it.goal /= matchCount.toDouble()
                    it.loseGoal /= matchCount.toDouble()
                    it.goalDiff /= matchCount.toDouble()
                } else {
                    it.score = -999.0
                    it.goal = -999.0
                    it.loseGoal = -999.0
                    it.goalDiff = -999.0
                }

                d("TEAM=${it.teamId} avg[score=${it.score.format8}, goal=${it.goal.format8}, diff=${it.goalDiff.format8}]")
            }

            var lastResult: LeagueTeamResult? = null
            var lastRank = 0
            //  sort by score. but if score is same, sort by goal diff
            val sortedLeagueTeamResults =
                if (game.calculationType == CalculationType.DIFF_SCORE || game.calculationType == CalculationType.TOTAL_SCORE) {
                    leagueTeamResults.values
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
                        .mapIndexed { idx, r ->
                            d("%2d位 TEAM=${r.teamId}  score=${r.score.format8}  diff=${r.goalDiff.format8}  goal=${r.goal.format8}"
                                .format(idx + 1))
                            r
                        }
                        .map { leagueTeamResult ->
                            if (lastResult != null) {
                                val scoreGap = (leagueTeamResult.score - lastResult!!.score)
                                val diffGap  = (leagueTeamResult.goalDiff - lastResult!!.goalDiff)
                                d("cmp TEAM=${leagueTeamResult.teamId}  "
                                        + "scoreGap=${scoreGap.format8}  diffGap=${diffGap.format8}  "
                                        + "=> rank=${leagueTeamResult.rank}")
                            }

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
                } else {
                    leagueTeamResults.values
                        .sortedWith(compareByDescending { it.score })
                        .map { leagueTeamResult ->
                            if (lastResult == null) {
                                leagueTeamResult.rank = 1
                            } else {
                                if (lastResult!!.score == leagueTeamResult.score) {
                                    leagueTeamResult.rank = lastRank
                                } else {
                                    leagueTeamResult.rank = lastRank + 1
                                }
                            }

                            //  last result
                            lastResult = leagueTeamResult
                            lastRank = leagueTeamResult.rank

                            leagueTeamResult
                        }
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
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

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
            val game = GameEntity.findById(id) ?: throw NotFoundException("invalid game id")

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
    var score: Double,
    var goal: Double,
    var loseGoal: Double,
    var goalDiff: Double,
    //  ignore
    @Transient
    var matchCount: Int = 0
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
