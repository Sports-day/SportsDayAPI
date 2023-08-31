package dev.t7e.services

import dev.t7e.models.ClassEntity
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
    _getAllObjectFunction = { GroupEntity.getAll() },
    _getObjectByIdFunction = { GroupEntity.getById(it) },
    fetchFunction = { GroupEntity.fetch(it) },
    onDeleteFunction = {
        //  Class -> Group
        ClassEntity.getAll().forEach { pair ->
            if (pair.second.groupId == it.id) {
                ClassEntity.fetch(pair.second.id)
            }
        }
    },
) {

    /**
     * Create new group
     *
     * @param omittedGroup[OmittedGroup] Group content
     * @return [Group]
     */
    fun create(omittedGroup: OmittedGroup): Result<Group> {
        val model = transaction {
            GroupEntity.new {
                this.name = omittedGroup.name
                this.description = omittedGroup.description
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(
            model,
        )
    }

    /**
     * Update group
     *
     * @param id[Int] group id
     * @param omittedGroup[OmittedGroup] Group content
     * @return [Group]
     */
    fun update(id: Int, omittedGroup: OmittedGroup): Result<Group> {
        val group = GroupEntity.getById(id) ?: throw NotFoundException("Group not found.")

        val model = transaction {
            group.first.name = omittedGroup.name
            group.first.description = omittedGroup.description
            group.first.updatedAt = LocalDateTime.now()

            //  serialize
            group.first.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }
}
