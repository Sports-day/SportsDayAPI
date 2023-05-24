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
    fetchFunction = { UserEntity.fetch(it) },
    onDeleteFunction = {
        //  Team -> User
        it.teamIds.forEach { teamId ->
            TeamEntity.fetch(teamId)
        }

        //  Microsoft Account -> User
        MicrosoftAccountEntity.getAll().forEach { pair ->
            if (pair.second.userId == it.id) {
                MicrosoftAccountEntity.fetch(pair.second.id)
            }
        }

        //  Class -> User
        ClassEntity.fetch(it.classId)
    }
) {

    fun create(omittedUser: OmittedUser): Result<User> {
        val classEntity = ClassEntity.getById(omittedUser.classId) ?: throw NotFoundException("invalid class id")


        val model = transaction {
            UserEntity.new {
                this.name = omittedUser.name
                this.studentId = omittedUser.studentId
                this.gender = omittedUser.gender
                this.classEntity = classEntity.first
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
            //  Class
            ClassEntity.fetch(omittedUser.classId)
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedUser: OmittedUser): Result<User> {
        val userEntity = UserEntity.getById(id)?.first ?: throw NotFoundException("invalid user id")
        val classEntity = ClassEntity.getById(omittedUser.classId)?.first ?: throw NotFoundException("invalid class id")

        val model = transaction {
            userEntity.name = omittedUser.name
            userEntity.studentId = omittedUser.studentId
            userEntity.gender = omittedUser.gender
            userEntity.classEntity = classEntity
            userEntity.updatedAt = LocalDateTime.now()

            userEntity.serializableModel()
        }.apply {
            fetchFunction(this.id)
            //  Class
            ClassEntity.fetch(omittedUser.classId)
        }

        return Result.success(model)
    }

    fun getTeams(id: Int): Result<List<Team>> {
        val teams = UserEntity.getUserTeams(id) ?: throw NotFoundException("invalid user id")

        return Result.success(teams.map { it.second })
    }

    fun getLinkedMicrosoftAccount(id: Int): Result<List<MicrosoftAccount>> {
        val microsoftAccounts = UserEntity.getUserMicrosoftAccounts(id) ?: throw NotFoundException("invalid user id")

        return Result.success(microsoftAccounts.map { it.second })
    }

}