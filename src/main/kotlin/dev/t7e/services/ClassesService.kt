package dev.t7e.services

import dev.t7e.models.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/09
 * @author testusuke
 */
object ClassesService : StandardService<ClassEntity, ClassModel>(
    objectName = "Class",
    _getAllObjectFunction = { ClassEntity.getAll() },
    _getObjectByIdFunction = { ClassEntity.getById(it) },
    fetchFunction = { ClassEntity.fetch(it) },
    onDeleteFunction = {
        //  User -> Class
        UserEntity.getAll().forEach { pair ->
            if (pair.second.classId == it.id) {
                UserEntity.fetch(pair.second.id)
            }
        }

        //  Team -> Class
        TeamEntity.getAll().forEach { pair ->
            if (pair.second.classId == it.id) {
                TeamEntity.fetch(pair.second.id)
            }
        }
    },
) {

    /**
     * Create new class
     *
     * @param omittedClass[OmittedClassModel] class content
     * @return [ClassModel]
     */
    fun create(omittedClass: OmittedClassModel): Result<ClassModel> {
        val group = GroupEntity.getById(omittedClass.groupId) ?: throw NotFoundException("invalid group id")

        val model = transaction {
            ClassEntity.new {
                this.name = omittedClass.name
                this.description = omittedClass.description
                this.group = group.first
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
     * Update class
     *
     * @param omittedClass[OmittedClassModel] class content
     * @return [ClassModel]
     */
    fun update(id: Int, omittedClass: OmittedClassModel): Result<ClassModel> {
        val classEntity = ClassEntity.getById(id) ?: throw NotFoundException("invalid class id")
        val group = GroupEntity.getById(omittedClass.groupId) ?: throw NotFoundException("invalid group id")

        val model = transaction {
            classEntity.first.name = omittedClass.name
            classEntity.first.description = omittedClass.description
            classEntity.first.group = group.first
            classEntity.first.updatedAt = LocalDateTime.now()
            //  serialize
            classEntity.first.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(
            model,
        )
    }

    /**
     * Get all users belonging to specific class
     *
     * @param id[Int] class id
     * @return [List<User>]
     */
    fun getAllUsersOfClass(id: Int): Result<List<User>> {
        val users = ClassEntity.getClassUsers(id)?.map { it.second } ?: throw NotFoundException("invalid class id")

        return Result.success(
            users,
        )
    }
}
