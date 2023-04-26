package dev.t7e.models

import dev.t7e.utils.SmartCache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */

object Sports: IntIdTable("sports") {
    val name = varchar("name", 64)
    val description = text("description")
    val iconImage = reference("icon_image", Images).nullable()
    val createdAt = datetime("created_at")
}

class SportEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: SmartCache<SportEntity, Sport> (
        entityName = "sport",
        table = Sports,
        duration = 5.minutes,
        serializer = { it.serializableModel() }
    )

    var name by Sports.name
    var description by Sports.description
    var iconImage by ImageEntity optionalReferencedOn Sports.iconImage
    var createdAt by Sports.createdAt

    fun serializableModel(): Sport {
        return Sport(
            id.value,
            name,
            description,
            iconImage?.id?.value,
            createdAt.toString(),
        )
    }
 }

@Serializable
data class Sport(
    val id: Int,
    val name: String,
    val description: String,
    val iconId: Int?,
    val createdAt: String,
)

@Serializable
data class OmittedSport(
    val name: String,
    val description: String,
    val iconId: Int?
)