package net.sportsday.services

import net.sportsday.models.Permission

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */
object PermissionsService {
    fun getAll(): Result<List<Permission>> {
        val permissions = Permission.getAll()

        return Result.success(
            permissions
        )
    }
}
