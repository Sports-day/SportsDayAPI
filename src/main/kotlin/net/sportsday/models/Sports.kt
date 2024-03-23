package net.sportsday.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */

object Sports : IntIdTable("sports") {
    val name = varchar("name", 64)
    val description = text("description")
    val iconImage = reference("icon_image", Images, onDelete = ReferenceOption.SET_NULL).nullable()
    val weight = integer("weight").default(0)
    val ruleId = integer("rule_id").default(0)
    val tag = reference("tag", Tags, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class SportEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SportEntity> (Sports)

    var name by Sports.name
    var description by Sports.description
    var iconImage by ImageEntity optionalReferencedOn Sports.iconImage
    var weight by Sports.weight
    var ruleId by Sports.ruleId
    var tag by TagEntity optionalReferencedOn Sports.tag
    val games by GameEntity referrersOn Games.sport
    var createdAt by Sports.createdAt
    var updatedAt by Sports.updatedAt

    fun serializableModel(): Sport {
        return Sport(
            id.value,
            name,
            description,
            iconImage?.id?.value,
            weight,
            ruleId,
            tag?.id?.value,
            games.map { it.id.value },
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class Sport(
    val id: Int,
    val name: String,
    val description: String,
    val iconId: Int?,
    val weight: Int,
    val ruleId: Int,
    val tagId: Int?,
    val gameIds: List<Int>,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedSport(
    val name: String,
    val description: String,
    val iconId: Int?,
    val weight: Int,
    val ruleId: Int,
    val tagId: Int?,
)
