package net.sportsday.models

import kotlinx.serialization.Serializable
import net.sportsday.utils.SmartCache
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */
object Groups : IntIdTable("groups") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class GroupEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : SmartCache<GroupEntity, Group> (
        entityName = "group",
        table = Groups,
        duration = 5.minutes,
        serializer = { it.serializableModel() },
    ) {}

    var name by Groups.name
    var description by Groups.description
    var createdAt by Groups.createdAt
    var updatedAt by Groups.updatedAt
    val classes by ClassEntity referrersOn Classes.group

    fun serializableModel(): Group {
        return Group(
            id.value,
            name,
            description,
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class Group(
    val id: Int,
    val name: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedGroup(
    val name: String,
    val description: String?,
)
