package net.sportsday.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */

object Roles: IntIdTable("roles") {
    val name = varchar("name", 64)
    val description = varchar("description", 128)
    val default = bool("default").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class RoleEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<RoleEntity>(Roles)

    var name by Roles.name
    var description by Roles.description
    var default by Roles.default
    var createdAt by Roles.createdAt
    var updatedAt by Roles.updatedAt
    val permissions by RolePermissionEntity referrersOn RolePermissions.role

    fun serializableModel(): Role {
        return Role(
            id.value,
            name,
            description,
            default,
            permissions.mapNotNull { Permission.getByName(it.permission) },
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class Role(
    val id: Int,
    val name: String,
    val description: String,
    val default: Boolean,
    val permissions: List<Permission>,
    val createdAt: String,
    val updatedAt: String,
)
