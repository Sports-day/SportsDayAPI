package net.sportsday.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2024/03/28
 * @author testusuke
 */

object TeamTags : IntIdTable("team_tags") {
    val name = varchar("name", 64)
    val sport = reference("sport", Sports, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class TeamTagEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TeamTagEntity>(TeamTags)

    var name by TeamTags.name
    var sport by SportEntity optionalReferencedOn TeamTags.sport
    var createdAt by TeamTags.createdAt
    var updatedAt by TeamTags.updatedAt
    val teams by TeamEntity optionalReferrersOn Teams.teamTag

    fun serializableModel(): TeamTag {
        return TeamTag(
            id.value,
            name,
            sport?.id?.value,
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class TeamTag(
    val id: Int,
    val name: String,
    val sportId: Int?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedTeamTag(
    val name: String,
    val sportId: Int?,
)
