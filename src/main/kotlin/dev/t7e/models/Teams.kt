package dev.t7e.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */
object Teams: IntIdTable("teams") {
    val name = varchar("name", 64)
    val classEntity = reference("class", Classes)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class TeamEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<TeamEntity>(Teams)

    var name by Teams.name
    var classEntity by ClassEntity referencedOn Teams.classEntity
    var users by UserEntity via TeamUsers
    var createdAt by Teams.createdAt
    var updatedAt by Teams.updatedAt

    fun serializableModel(): Team {
        return Team(
            id.value,
            name,
            classEntity.serializableModel(),
            users.map(UserEntity::serializableModel),
            createdAt.toString(),
            updatedAt.toString()
        )
    }
}

@Serializable
data class Team(
    val id: Int,
    val name: String,
    val classEntity: Class,
    val users: List<User>,
    val createdAt: String,
    val updatedAt: String
)