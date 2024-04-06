package net.sportsday.services

import net.sportsday.models.Permission
import net.sportsday.models.PermissionList

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */
object PermissionsService {
    fun getAll(): Result<List<Permission>> {
        val permissions = PermissionList.getAll()

        return Result.success(
            permissions
        )
    }
}
