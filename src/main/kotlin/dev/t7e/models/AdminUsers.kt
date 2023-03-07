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
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */
object AdminUsers: IntIdTable("admin_users") {
    //  maximum email length is under 320...?
    val email = varchar("email", 320).uniqueIndex()
    val description = varchar("description", 128)
    val issuedAt = datetime("issued_at")
}

class AdminUserEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<AdminUserEntity>(AdminUsers) {
        val getAdminUserByEmail: (email: String) -> AdminUserEntity? = Cache.memoize(1000 * 60 * 1) { email ->
            transaction {
                AdminUserEntity.find{ AdminUsers.email eq email }.singleOrNull()
            }
        }

        val isAdminUserByEmail: (email: String) -> Boolean = Cache.memoize(1000 * 60 * 1) { email ->
            getAdminUserByEmail(email) != null
        }
    }

    var email by AdminUsers.email
    var description by AdminUsers.description
    var issuedAt by AdminUsers.issuedAt

    fun serializableModel(): AdminUser {
        return AdminUser(
            id.value,
            email,
            description,
            issuedAt.toString()
        )
    }
}

@Serializable
data class AdminUser(
    val id: Int,
    val email: String,
    val description: String,
    val issuedAt: String
)