package dev.t7e.services

import dev.t7e.models.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/10
 * @author testusuke
 */
object UsersService : StandardService<UserEntity, User>(
    objectName = "User",
    _getAllObjectFunction = { UserEntity.getAll() },
    _getObjectByIdFunction = { UserEntity.getById(it) },
    fetchFunction = { UserEntity.fetch(it) }
) {

    fun create(omittedUser: OmittedUser): Result<User> = transaction {
        val classEntity = ClassEntity.getById(omittedUser.classId) ?: throw NotFoundException("invalid class id")

        val entity = UserEntity.new {
            this.name = omittedUser.name
            this.studentId = omittedUser.studentId
            this.gender = omittedUser.gender
            this.classEntity = classEntity.first
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }

        Result.success(
            entity
                .serializableModel()
                .apply {
                    fetchFunction(this.id)
                }
        )
    }

    fun update(id: Int, omittedUser: OmittedUser): Result<User> = transaction {
        val userEntity = UserEntity.getById(id)?.first ?: throw NotFoundException("invalid user id")
        val classEntity = ClassEntity.getById(omittedUser.classId)?.first ?: throw NotFoundException("invalid class id")

        userEntity.name = omittedUser.name
        userEntity.studentId = omittedUser.studentId
        userEntity.gender = omittedUser.gender
        userEntity.classEntity = classEntity
        userEntity.updatedAt = LocalDateTime.now()

        Result.success(
            userEntity
                .serializableModel()
                .apply {
                    fetchFunction(this.id)
                }
        )
    }

    fun getTeams(id: Int): Result<List<Team>> = transaction {
        val teams = UserEntity.getUserTeams(id) ?: throw NotFoundException("invalid user id")

        Result.success(teams.map { it.second })
    }

    fun getLinkedMicrosoftAccount(id: Int): Result<List<MicrosoftAccount>> = transaction {
        val microsoftAccounts = UserEntity.getUserMicrosoftAccounts(id) ?: throw NotFoundException("invalid user id")

        Result.success(microsoftAccounts.map { it.second })
    }

}