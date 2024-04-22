package net.sportsday.services

import net.sportsday.models.Permission.Companion.serialize
import net.sportsday.models.PermissionList
import net.sportsday.models.SerializablePermission

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */
object PermissionsService {
    fun getAll(): Result<List<SerializablePermission>> {
        val permissions = PermissionList.getAll().map { it.serialize() }

        return Result.success(
            permissions
        )
    }
}
