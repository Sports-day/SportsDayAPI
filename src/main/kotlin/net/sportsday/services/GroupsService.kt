package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.ClassEntity
import net.sportsday.models.Group
import net.sportsday.models.GroupEntity
import net.sportsday.models.OmittedGroup
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */

object GroupsService {

    /**
     * Get all groups
     *
     * @return [List<Group>]
     */
    fun getAll(): Result<List<Group>> {
        val models = transaction {
            GroupEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(
            models
        )
    }

    /**
     * Get group by id
     *
     * @param id[Int] group id
     * @return [List<Group>]
     */
    fun getById(id: Int): Result<Group> {
        val model = transaction {
            GroupEntity.findById(id)?.serializableModel()
        } ?: throw NotFoundException("Group not found.")

        return Result.success(
            model
        )
    }

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
        val model = transaction {
            val group = GroupEntity.findById(id) ?: throw NotFoundException("Group not found.")


            group.name = omittedGroup.name
            group.description = omittedGroup.description
            group.updatedAt = LocalDateTime.now()

            //  serialize
            group.serializableModel()
        }

        return Result.success(model)
    }

    /**
     * Delete group by id
     *
     * @param id[Int] group id
     */
    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val group = GroupEntity.findById(id) ?: throw NotFoundException("Group not found.")

            group.delete()
        }

        return Result.success(Unit)
    }
}
