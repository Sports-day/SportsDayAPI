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
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */
object Groups : IntIdTable("groups") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val createdAt = datetime("created_at")
}

class GroupEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GroupEntity>(Groups) {
        val getAllGroups = Cache.memoizeOneObject(1.minutes) {
            transaction {
                GroupEntity.all().toList()
            }
        }

        val getGroup: (id: Int) -> GroupEntity? = Cache.memoize(1.minutes) { id ->
            transaction {
                GroupEntity.findById(id)
            }
        }
    }

    var name by Groups.name
    var description by Groups.description
    var createdAt by Groups.createdAt

    fun serializableModel(): Group {
        return Group(
            id.value,
            name,
            description,
            createdAt.toString()
        )
    }
}

@Serializable
data class Group(
    val id: Int,
    val name: String,
    val description: String?,
    val createdAt: String
)

@Serializable
data class OmittedGroup(
    val name: String,
    val description: String?
)
