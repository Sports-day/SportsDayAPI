package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */
object RolesService {
    fun getAll(): Result<List<Role>> {
        val roles = transaction {
            RoleEntity.all().map { it.serializableModel() }
        }

        return Result.success(
            roles
        )
    }

    fun getById(id: Int): Result<Role> {
        val role = transaction {
            RoleEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Role not found.")
        }

        return Result.success(role)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val role = RoleEntity.findById(id) ?: throw NotFoundException("Role not found.")

            role.delete()
        }

        return Result.success(Unit)
    }

    fun create(omittedRole: OmittedRole): Result<Role> {
        val model = transaction {
            val role = RoleEntity.new {
                this.name = omittedRole.name
                this.description = omittedRole.description
                this.default = omittedRole.default
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            role.serializableModel()
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedRole: OmittedRole): Result<Role> {
        val model = transaction {
            val roleEntity = RoleEntity.findById(id) ?: throw NotFoundException("Role not found.")

            roleEntity.name = omittedRole.name
            roleEntity.description = omittedRole.description
            roleEntity.default = omittedRole.default
            roleEntity.updatedAt = LocalDateTime.now()

            roleEntity.serializableModel()
        }

        return Result.success(model)
    }

    fun addPermission(roleId: Int, permissionName: String): Result<Role> {
        val model = transaction {
            val role = RoleEntity.findById(roleId) ?: throw NotFoundException("Role not found.")
            val permission = PermissionList.getByName(permissionName) ?: throw NotFoundException("Permission not found.")

            //  check if permission is already added
            if (role.permissions.any { it.permission == permissionName }) {
                throw BadRequestException("Permission already added.")
            }

            RolePermissionEntity.new {
                this.role = role
                this.permission = permission.name
            }

            role.updatedAt = LocalDateTime.now()

            role.serializableModel()
        }

        return Result.success(model)
    }

    fun removePermission(roleId: Int, permissionName: String): Result<Role> {
        val model = transaction {
            val role = RoleEntity.findById(roleId) ?: throw NotFoundException("Role not found.")

            //  check if permission not found
            if (PermissionList.getByName(permissionName) == null) {
                throw NotFoundException("Permission not found.")
            }

            //  check if permission is already added
            val rolePermission = role.permissions.find { it.permission == permissionName } ?: throw BadRequestException(
                "Role does not have permission."
            )

            rolePermission.delete()

            role.updatedAt = LocalDateTime.now()

            role.serializableModel()
        }

        return Result.success(model)
    }
}
