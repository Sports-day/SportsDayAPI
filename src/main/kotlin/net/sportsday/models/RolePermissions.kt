package net.sportsday.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */

object RolePermissions : IntIdTable("role_permissions") {
    val role = reference("role", Roles, onDelete = ReferenceOption.CASCADE)
    val permission = varchar("permission", 64)
}

class RolePermissionEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<RolePermissionEntity>(RolePermissions)

    var role by RoleEntity referencedOn RolePermissions.role
    var permission by RolePermissions.permission
}
