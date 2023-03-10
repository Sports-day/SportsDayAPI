package dev.t7e.models

import dev.t7e.utils.Cache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/02/27
 * @author testusuke
 */
object Users: IntIdTable("users") {
    val name = varchar("name", 64)
    val studentId = varchar("student_id", 32)
    val classEntity = reference("class", Classes)
    val createdAt = datetime("created_at")
}

class UserEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<UserEntity>(Users) {
        val getAllUsers = Cache.memoizeOneObject(1.minutes) {
            transaction {
                UserEntity.all().toList().map {
                    it to it.serializableModel()
                }
            }
        }

        val getUser: (id: Int) -> Pair<UserEntity, User>? = Cache.memoize(1.minutes) { id ->
            transaction {
                UserEntity
                    .findById(id)
                    ?.let {
                        it to it.serializableModel()
                    }
            }
        }
    }

    var name by Users.name
    var studentId by Users.studentId
    var classEntity by ClassEntity referencedOn Users.classEntity
    var createdAt by Users.createdAt

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