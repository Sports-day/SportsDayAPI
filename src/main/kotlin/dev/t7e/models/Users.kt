package dev.t7e.models

import dev.t7e.utils.SmartCache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/02/27
 * @author testusuke
 */
object Users : IntIdTable("users") {
    val name = varchar("name", 64)
    val studentId = varchar("student_id", 32)
    val classEntity = reference("class", Classes)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : SmartCache<UserEntity, User> (
        entityName = "user",
        table = Users,
        duration = 5.minutes,
        serializer = { it.serializableModel() }
    ) {
        private val userTeamsMap = mutableMapOf<Int, List<Pair<TeamEntity, Team>>?>()
        private val userMicrosoftAccountsMap = mutableMapOf<Int, List<Pair<MicrosoftAccountEntity, MicrosoftAccount>>?>()


        fun getUserTeams(id: Int): List<Pair<TeamEntity, Team>>? {
            if (!userTeamsMap.containsKey(id)) {
                //  fetch unknown data
                fetch(id)
            }

            return userTeamsMap[id]
        }

        fun getUserMicrosoftAccounts(id: Int): List<Pair<MicrosoftAccountEntity, MicrosoftAccount>>? {
            if (!userMicrosoftAccountsMap.containsKey(id)) {
                //  fetch unknown data
                fetch(id)
            }

            return userMicrosoftAccountsMap[id]
        }

        init {
            //  user teams
            registerFetchFunction { id ->
                transaction {
                    if (id == null) {
                        userTeamsMap.clear()

                        cache.values.filterNotNull().forEach { value ->
                            val entity = value.first
                            val teams = entity.teams.map { teams ->
                                teams to teams.serializableModel()
                            }
                            userTeamsMap[entity.id.value] = teams
                        }
                    } else {
                        val entity = getById(id)

                        if (entity == null) {
                            userTeamsMap[id] = null
                        } else {
                            userTeamsMap[id] = entity.first.teams.map { team ->
                                team to team.serializableModel()
                            }
                        }
                    }
                }
            }

            //  user microsoft accounts
            registerFetchFunction { id ->
                transaction {
                    if (id == null) {
                        userMicrosoftAccountsMap.clear()

                        cache.values.filterNotNull().forEach { value ->
                            val entity = value.first
                            val microsoftAccounts = entity.microsoftAccounts.map { microsoftAccount ->
                                microsoftAccount to microsoftAccount.serializableModel()
                            }
                            userMicrosoftAccountsMap[entity.id.value] = microsoftAccounts
                        }
                    } else {
                        val entity = getById(id)

                        if (entity == null) {
                            userMicrosoftAccountsMap[id] = null
                        } else {
                            userMicrosoftAccountsMap[id] = entity.first.microsoftAccounts.map { microsoftAccount ->
                                microsoftAccount to microsoftAccount.serializableModel()
                            }
                        }
                    }
                }
            }
        }
    }

    var name by Users.name
    var studentId by Users.studentId
    var classEntity by ClassEntity referencedOn Users.classEntity
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
    var teams by TeamEntity via TeamUsers
    val microsoftAccounts by MicrosoftAccountEntity optionalReferrersOn MicrosoftAccounts.user


    fun serializableModel(): User {
        return User(
            id.value,
            name,
            studentId,
            classEntity.id.value,
            createdAt.toString(),
            updatedAt.toString()
        )
    }
}

@Serializable
data class User(
    val id: Int,
    val name: String,
    val studentId: String,
    val classId: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OmittedUser(
    val name: String,
    val studentId: String,
    val classId: Int
)