package dev.t7e.models

import dev.t7e.utils.Cache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/02/27
 * @author testusuke
 */
object Users : IntIdTable("users") {
    val name = varchar("name", 64)
    val studentId = varchar("student_id", 32)
    val classEntity = reference("class", Classes)
    val createdAt = datetime("created_at")
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users) {
        val getAllUsers = Cache.memoizeOneObject {
            transaction {
                UserEntity.all().toList().map {
                    it to it.serializableModel()
                }
            }
        }

        val getUser: (id: Int) -> Pair<UserEntity, User>? = Cache.memoize { id ->
            transaction {
                UserEntity
                    .findById(id)
                    ?.let {
                        it to it.serializableModel()
                    }
            }
        }

        val getUserTeams: (id: Int) -> List<Pair<TeamEntity, Team>>? = Cache.memoize { id ->
            getUser(id)?.let {
                transaction {
                    it.first.teams.map { team ->
                        team to team.serializableModel()
                    }
                }
            }
        }

        val getMicrosoftAccounts: (id: Int) -> List<Pair<MicrosoftAccountEntity, MicrosoftAccount>>? =
            Cache.memoize { id ->
                getUser(id)?.let {
                    transaction {
                        it.first.microsoftAccounts.map { ms ->
                            ms to ms.serializableModel()
                        }
                    }
                }
            }
    }

    var name by Users.name
    var studentId by Users.studentId
    var classEntity by ClassEntity referencedOn Users.classEntity
    var createdAt by Users.createdAt
    var teams by TeamEntity via TeamUsers
    val microsoftAccounts by MicrosoftAccountEntity optionalReferrersOn MicrosoftAccounts.user


    fun serializableModel(): User {
        return User(
            id.value,
            name,
            studentId,
            classEntity.id.value,
            createdAt.toString()
        )
    }
}

@Serializable
data class User(
    val id: Int,
    val name: String,
    val studentId: String,
    val classId: Int,
    val createdAt: String
)

@Serializable
data class OmittedUser(
    val name: String,
    val studentId: String,
    val classId: Int
)