package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/09
 * @author testusuke
 */
object ClassesService {

    fun getAll(): Result<List<ClassModel>> {
        val models = transaction {
            ClassEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(
            models,
        )
    }

    fun getById(id: Int): Result<ClassModel> {
        val model = transaction {
            ClassEntity.findById(id)?.serializableModel()
        } ?: throw NotFoundException("invalid class id")

        return Result.success(
            model,
        )
    }

    /**
     * Create new class
     *
     * @param omittedClass[OmittedClassModel] class content
     * @return [ClassModel]
     */
    fun create(omittedClass: OmittedClassModel): Result<ClassModel> {
        val model = transaction {
            val group = GroupEntity.findById(omittedClass.groupId) ?: throw NotFoundException("invalid group id")

            val entity = ClassEntity.new {
                this.name = omittedClass.name
                this.description = omittedClass.description
                this.group = group
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            entity.serializableModel()
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
        val model = transaction {
            val classEntity = ClassEntity.findById(id) ?: throw NotFoundException("invalid class id")
            val group = GroupEntity.findById(omittedClass.groupId) ?: throw NotFoundException("invalid group id")

            classEntity.name = omittedClass.name
            classEntity.description = omittedClass.description
            classEntity.group = group
            classEntity.updatedAt = LocalDateTime.now()

            //  serialize
            classEntity.serializableModel()
        }

        return Result.success(
            model,
        )
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val entity = ClassEntity.findById(id) ?: throw NotFoundException("invalid class id")

            //  delete entity
            entity.delete()
        }

        return Result.success(Unit)
    }

    /**
     * Get all users belonging to specific class
     *
     * @param id[Int] class id
     * @return [List<User>]
     */
    fun getAllUsersOfClass(id: Int): Result<List<User>> {

        val users = transaction {
            val classEntity = ClassEntity.findById(id) ?: throw NotFoundException("invalid class id")

            //  get all users
            classEntity.users.map { it.serializableModel() }
        }

        return Result.success(
            users,
        )
    }
}
