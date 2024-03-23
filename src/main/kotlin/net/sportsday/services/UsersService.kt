package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/10
 * @author testusuke
 */
object UsersService {

    fun getAll(): Result<List<User>> {
        val models = transaction {
            UserEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(models)
    }

    fun getById(id: Int): Result<User> {
        val model = transaction {
            UserEntity.findById(id)?.serializableModel() ?: throw NotFoundException("User not found.")
        }

        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val user = UserEntity.findById(id) ?: throw NotFoundException("User not found.")

            user.delete()
        }

        return Result.success(Unit)
    }

    fun create(omittedUser: OmittedUser): Result<User> {
        val model = transaction {
            val classEntity = ClassEntity.findById(omittedUser.classId) ?: throw NotFoundException("invalid class id")

            val user = UserEntity.new {
                this.name = omittedUser.name
                this.studentId = omittedUser.studentId
                this.gender = omittedUser.gender
                this.classEntity = classEntity
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            user.serializableModel()
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedUser: OmittedUser): Result<User> {
        val model = transaction {
            val userEntity = UserEntity.findById(id) ?: throw NotFoundException("invalid user id")
            val classEntity =
                ClassEntity.findById(omittedUser.classId) ?: throw NotFoundException("invalid class id")

            userEntity.name = omittedUser.name
            userEntity.studentId = omittedUser.studentId
            userEntity.gender = omittedUser.gender
            userEntity.classEntity = classEntity
            userEntity.updatedAt = LocalDateTime.now()

            userEntity.serializableModel()
        }

        return Result.success(model)
    }

    fun getTeams(id: Int): Result<List<Team>> {
        val models = transaction {
            val user = UserEntity.findById(id) ?: throw NotFoundException("invalid user id")

            //  serialize
            user.teams.map { it.serializableModel() }
        }

        return Result.success(models)
    }

    fun getLinkedMicrosoftAccount(id: Int): Result<List<MicrosoftAccount>> {
        val models = transaction {
            val user = UserEntity.findById(id) ?: throw NotFoundException("invalid user id")

            user.microsoftAccounts.map {
                it.serializableModel()
            }
        }

        return Result.success(models)
    }
}
