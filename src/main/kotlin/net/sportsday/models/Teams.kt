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
object Teams : IntIdTable("teams") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val classEntity = reference(
        "class",
        Classes,
        onDelete = ReferenceOption.CASCADE,
    )
    val teamTag = reference(
        "team_tag",
        TeamTags,
        onDelete = ReferenceOption.SET_NULL,
    ).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class TeamEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TeamEntity>(Teams)

    var name by Teams.name
    var description by Teams.description
    var classEntity by ClassEntity referencedOn Teams.classEntity
    var teamTag by TeamTagEntity optionalReferencedOn Teams.teamTag
    var users by UserEntity via TeamUsers
    var enteredGames by GameEntity via Entries
    var createdAt by Teams.createdAt
    var updatedAt by Teams.updatedAt

    fun serializableModel(): Team {
        return Team(
            id.value,
            name,
            description,
            classEntity.id.value,
            teamTag?.id?.value,
            users.map { it.id.value },
            enteredGames.map { it.id.value },
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class Team(
    val id: Int,
    val name: String,
    val description: String?,
    val classId: Int,
    val teamTagId: Int?,
    val userIds: List<Int>,
    val enteredGameIds: List<Int>,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedTeam(
    val name: String,
    val description: String?,
    val classId: Int,
    val teamTagId: Int?,
)

@Serializable
data class OmittedTeamUsers(
    val users: List<Int>,
)
