package dev.t7e.models

import dev.t7e.utils.SmartCache
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */

object Games : IntIdTable("games") {
    val name = varchar("name", 64)
    val description = varchar("description", 512)
    val sport = reference("sport", Sports, onDelete = ReferenceOption.CASCADE)
    val type = enumerationByName<GameType>("type", 32)
    val calculationType = enumerationByName<CalculationType>("calculation_type", 32).default(CalculationType.DIFF_SCORE)
    val weight = integer("weight")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class GameEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : SmartCache<GameEntity, Game> (
        entityName = "game",
        table = Games,
        duration = 5.minutes,
        serializer = { it.serializableModel() }
    ) {
        private val entriesMap = mutableMapOf<Int, List<Pair<TeamEntity, Team>>?>()
        private val matchesMap = mutableMapOf<Int, List<Pair<MatchEntity, Match>>?>()

        /**
         * Get game entries
         *
         * @param id Game ID
         * @return List of entries
         */
        fun getGameEntries(id: Int): List<Pair<TeamEntity, Team>>? {
            if (!entriesMap.containsKey(id)) {
                //  fetch unknown data
                fetch(id)
            }

            return entriesMap[id]
        }

        /**
         * Get game matches
         *
         * @param id Game ID
         * @return List of matches
         */
        fun getGameMatches(id: Int): List<Pair<MatchEntity, Match>>? {
            if (!matchesMap.containsKey(id)) {
                //  fetch unknown data
                fetch(id)
            }

            return matchesMap[id]
        }

        init {
            //  game entries
            registerFetchFunction { id ->
                transaction {
                    if (id == null) {
                        entriesMap.clear()

                        cache.values.filterNotNull().forEach { value ->
                            val entity = value.first
                            val entries = entity.teams.map { team ->
                                team to team.serializableModel()
                            }

                            entriesMap[entity.id.value] = entries
                        }
                    } else {
                        val entity = getById(id)

                        if (entity == null) {
                            entriesMap.remove(id)
                        } else {
                            entriesMap[id] = entity.first.teams.map { team ->
                                team to team.serializableModel()
                            }
                        }
                    }
                }
            }

            //  game matches
            registerFetchFunction { id ->
                transaction {
                    if (id == null) {
                        matchesMap.clear()

                        cache.values.filterNotNull().forEach { value ->
                            val entity = value.first
                            val matches = entity.matches.map { match ->
                                match to match.serializableModel()
                            }

                            matchesMap[entity.id.value] = matches
                        }
                    } else {
                        val entity = getById(id)

                        if (entity == null) {
                            matchesMap.remove(id)
                        } else {
                            matchesMap[id] = entity.first.matches.map { match ->
                                match to match.serializableModel()
                            }
                        }
                    }
                }
            }
        }
    }

    var name by Games.name
    var description by Games.description
    var sport by SportEntity referencedOn Games.sport
    var type by Games.type
    var calculationType by Games.calculationType
    var weight by Games.weight
    var createdAt by Games.createdAt
    var updatedAt by Games.updatedAt
    val matches by MatchEntity referrersOn Matches.game
    var teams by TeamEntity via Entries

    fun serializableModel(): Game {
        return Game(
            id.value,
            name,
            description,
            sport.id.value,
            type,
            calculationType,
            weight,
            createdAt.toString(),
            updatedAt.toString()
        )
    }
}

@Serializable
enum class GameType(val status: String) {
    @SerialName("tournament")
    TOURNAMENT("tournament"),

    @SerialName("league")
    LEAGUE("league")
}

@Serializable
enum class CalculationType(val type: String) {
    @SerialName("total_score")
    TOTAL_SCORE("total_score"),

    @SerialName("diff_score")
    DIFF_SCORE("diff_score")
}

@Serializable
data class Game(
    val id: Int,
    val name: String,
    val description: String,
    val sportId: Int,
    val type: GameType,
    val calculationType: CalculationType,
    val weight: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OmittedGame(
    val name: String,
    val description: String,
    val sportId: Int,
    val type: GameType,
    val calculationType: CalculationType?,
    val weight: Int
)
