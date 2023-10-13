package net.sportsday.models

import kotlinx.serialization.Serializable
import net.sportsday.utils.SmartCache
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
object Teams : IntIdTable("teams") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val classEntity = reference(
        "class",
        Classes,
        onDelete = ReferenceOption.CASCADE,
    )
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class TeamEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : SmartCache<TeamEntity, Team>(
        entityName = "team",
        table = Teams,
        duration = 5.minutes,
        serializer = { it.serializableModel() },
    ) {
        private val teamUsersMap = mutableMapOf<Int, List<Pair<UserEntity, User>>?>()

        fun getTeamUsers(id: Int): List<Pair<UserEntity, User>>? {
            if (!teamUsersMap.containsKey(id)) {
                //  fetch unknown data
                fetch(id)
            }

            return teamUsersMap[id]
        }

        init {

            //  team users
            registerFetchFunction { id ->
                transaction {
                    if (id == null) {
                        teamUsersMap.clear()

                        cache.values.filterNotNull().forEach { value ->
                            val entity = value.first
                            val users = entity.users.map { user ->
                                user to user.serializableModel()
                            }

                            teamUsersMap[entity.id.value] = users
                        }
                    } else {
                        val entity = getById(id)

                        if (entity == null) {
                            teamUsersMap[id] = null
                        } else {
                            teamUsersMap[id] = entity.first.users.map { user ->
                                user to user.serializableModel()
                            }
                        }
                    }
                }
            }
        }
    }

    var name by Teams.name
    var description by Teams.description
    var classEntity by ClassEntity referencedOn Teams.classEntity
    var users by UserEntity via TeamUsers
    var enteredGames by GameEntity via Entries
    var createdAt by Teams.createdAt
    var updatedAt by Teams.updatedAt

    fun serializableModel(): Team {
        return Team(
            id.value,
            name,
            description,
            classEntity.id.value,
            users.map { it.id.value },
            enteredGames.map { it.id.value },
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class Team(
    val id: Int,
    val name: String,
    val description: String?,
    val classId: Int,
    val userIds: List<Int>,
    val enteredGameIds: List<Int>,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedTeam(
    val name: String,
    val description: String?,
    val classId: Int,
)

@Serializable
data class OmittedTeamUsers(
    val users: List<Int>,
)