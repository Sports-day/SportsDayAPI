package dev.t7e.services

import dev.t7e.models.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/09
 * @author testusuke
 */
object ClassesService: StandardService<ClassEntity, ClassModel>(
    objectName = "Class",
    _getAllObjectFunction = { ClassEntity.getAllClasses() },
    _getObjectByIdFunction =  { ClassEntity.getClass(it) },
) {

    /**
     * Create new class
     *
     * @param omittedClass[OmittedClassModel] class content
     * @return [ClassModel]
     */
    fun create(omittedClass: OmittedClassModel): Result<ClassModel> = transaction {
        val group = GroupEntity.getGroup(omittedClass.groupId) ?: throw NotFoundException("invalid group id")

        Result.success(
            ClassEntity.new {
                this.name = omittedClass.name
                this.description = omittedClass.description
                this.group = group.first
                this.createdAt = LocalDateTime.now()
            }.serializableModel()
        )
    }

    /**
     * Update class
     *
     * @param omittedClass[OmittedClassModel] class content
     * @return [ClassModel]
     */
    fun update(id: Int, omittedClass: OmittedClassModel): Result<ClassModel> = transaction {
        val classEntity = ClassEntity.getClass(id) ?: throw NotFoundException("invalid class id")
        val group = GroupEntity.getGroup(omittedClass.groupId) ?: throw NotFoundException("invalid group id")

        classEntity.first.name = omittedClass.name
        classEntity.first.description = omittedClass.description
        classEntity.first.group = group.first

        Result.success(classEntity.first.serializableModel())
    }

    /**
     * Get all users belonging to specific class
     *
     * @param id[Int] class id
     * @return [List<User>]
     */
    fun getAllUsersOfClass(id: Int): Result<List<User>> = transaction {
        val classEntity = ClassEntity.getClass(id) ?: throw NotFoundException("invalid class id")

        Result.success(
            classEntity.first.users.toList().map(UserEntity::serializableModel)
        )
    }

}