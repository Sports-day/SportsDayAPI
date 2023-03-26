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
    _getAllObjectFunction = { UserEntity.getAllUsers() },
    _getObjectByIdFunction = { UserEntity.getUser(it) }
) {

    fun create(omittedUser: OmittedUser): Result<User> = transaction {
        val classEntity = ClassEntity.getClass(omittedUser.classId) ?: throw NotFoundException("invalid class id")

        val entity = UserEntity.new {
            this.name = omittedUser.name
            this.studentId = omittedUser.studentId
            this.classEntity = classEntity.first
            this.createdAt = LocalDateTime.now()
        }

        Result.success(entity.serializableModel())
    }

    fun update(id: Int, omittedUser: OmittedUser): Result<User> = transaction {
        val userEntity = UserEntity.getUser(id)?.first ?: throw NotFoundException("invalid user id")
        val classEntity = ClassEntity.getClass(omittedUser.classId)?.first ?: throw NotFoundException("invalid class id")

        userEntity.name = omittedUser.name
        userEntity.studentId = omittedUser.studentId
        userEntity.classEntity = classEntity

        Result.success(userEntity.serializableModel())
    }

    fun getTeams(id: Int): Result<List<Team>> = transaction {
        val teams = UserEntity.getUserTeams(id) ?: throw NotFoundException("invalid user id")

        Result.success(teams.map { it.second })
    }

    fun getLinkedMicrosoftAccount(id: Int): Result<List<MicrosoftAccount>> = transaction {
        val microsoftAccounts = UserEntity.getMicrosoftAccounts(id) ?: throw NotFoundException("invalid user id")

        Result.success(microsoftAccounts.map { it.second })
    }

}