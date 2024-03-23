package net.sportsday.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/10/02
 * @author testusuke
 */
object Tags : IntIdTable("tags") {
    val name = varchar("name", 64)
    val enabled = bool("enabled").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class TagEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TagEntity> (Tags)

    var name by Tags.name
    var enabled by Tags.enabled
    var createdAt by Tags.createdAt
    var updatedAt by Tags.updatedAt

    fun serializableModel(): Tag {
        return Tag(
            id.value,
            name,
            enabled,
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class Tag(
    val id: Int,
    val name: String,
    val enabled: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedTag(
    val name: String,
    val enabled: Boolean,
)
