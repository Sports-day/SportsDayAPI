package dev.t7e.services

import dev.t7e.models.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/19
 * @author testusuke
 */
object TeamsService : StandardService<TeamEntity, Team>(
    objectName = "Team",
    _getAllObjectFunction = { TeamEntity.getAll() },
    _getObjectByIdFunction = { TeamEntity.getById(it) },
    fetchFunction = { TeamEntity.fetch(it) },
    onDeleteFunction = {
        //  User -> Team
        UserEntity.getAll().forEach { pair ->
            if (pair.second.teamIds.contains(it.id)) {
                UserEntity.fetch(pair.second.id)
            }
        }
    }
) {

    fun create(omittedTeam: OmittedTeam): Result<Team> {
        val classEntity = ClassEntity.getById(omittedTeam.classId)?.first ?: throw BadRequestException("invalid class id")

        val model = transaction {
            TeamEntity.new {
                this.name = omittedTeam.name
                this.description = omittedTeam.description
                this.classEntity = classEntity
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedTeam: OmittedTeam): Result<Team> {
        val team = TeamEntity.getById(id)?.first ?: throw NotFoundException("Team not found.")
        val classEntity = ClassEntity.getById(omittedTeam.classId)?.first ?: throw BadRequestException("invalid class id")

        val model = transaction {
            team.name = omittedTeam.name
            team.description = omittedTeam.description
            team.classEntity = classEntity
            team.updatedAt = LocalDateTime.now()

            team.serializableModel()
        }.apply {
            fetchFunction(this.id)

            //  User -> Team
            UserEntity.getAll().forEach { pair ->
                if (pair.second.teamIds.contains(this.id)) {
                    UserEntity.fetch(pair.second.id)
                }
            }
        }

        return Result.success(model)
    }

    fun getUsers(id: Int): Result<List<User>> {
        val users = TeamEntity.getTeamUsers(id)?.map { it.second } ?: throw NotFoundException("Team not found.")

        return Result.success(users)
    }

    fun addUsers(id: Int, userIds: List<Int>): Result<Team> {
        val team = TeamEntity.getById(id)?.first ?: throw NotFoundException("Team not found.")
        val users = userIds.mapNotNull {
            UserEntity.getById(it)?.first
        }

        val model = transaction {
            team.users = SizedCollection(listOf(team.users.toList(), users).flatten().distinct())
            team.updatedAt = LocalDateTime.now()

            team.serializableModel()
        }

        fetchFunction(id)
        //  re-fetch
        userIds.forEach {
            UserEntity.fetch(it)
        }

        return Result.success(model)
    }

    fun removeUser(id: Int, userId: Int): Result<Team> {
        val team = TeamEntity.getById(id)?.first ?: throw NotFoundException("Team not found.")

        val model = transaction {
            team.users = SizedCollection(
                team.users.filterNot {
                    it.id.value == userId
                }
            )
            team.updatedAt = LocalDateTime.now()

            team.serializableModel()
        }.apply {
            transaction {
                fetchFunction(id)
                //  re-fetch
                UserEntity.fetch(userId)

                team.users.forEach {
                    UserEntity.fetch(it.id.value)
                }
            }
        }

        return Result.success(model)
    }
}
