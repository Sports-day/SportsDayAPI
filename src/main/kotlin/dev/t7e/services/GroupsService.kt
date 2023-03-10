package dev.t7e.services

import dev.t7e.models.Group
import dev.t7e.models.GroupEntity
import dev.t7e.models.OmittedGroup
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */

object GroupsService : StandardService<GroupEntity, Group>(
    objectName = "Group",
    _getAllObjectFunction = { GroupEntity.getAllGroups() },
    _getObjectByIdFunction = { GroupEntity.getGroup(it) },
) {

    /**
     * Create new group
     *
     * @param omittedGroup[OmittedGroup] Group content
     * @return [Group]
     */
    fun create(omittedGroup: OmittedGroup): Result<Group> = transaction {
        Result.success(
            GroupEntity.new {
                this.name = omittedGroup.name
                this.description = omittedGroup.description
                this.createdAt = LocalDateTime.now()
            }.serializableModel()
        )
    }

    /**
     * Update group
     *
     * @param id[Int] group id
     * @param omittedGroup[OmittedGroup] Group content
     * @return [Group]
     */
    fun update(id: Int, omittedGroup: OmittedGroup): Result<Group> = transaction {
        val group = GroupEntity.getGroup(id) ?: throw NotFoundException("Group not found.")


        group.first.name = omittedGroup.name
        group.first.description = omittedGroup.description

        Result.success(group.first.serializableModel())
    }

}