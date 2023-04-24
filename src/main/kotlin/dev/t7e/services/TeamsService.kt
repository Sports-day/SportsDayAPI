package dev.t7e.services

import dev.t7e.models.*
import io.ktor.server.plugins.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/03/19
 * @author testusuke
 */
object TeamsService : StandardService<TeamEntity, Team>(
    objectName = "Team",
    _getAllObjectFunction = { TeamEntity.getAll() },
    _getObjectByIdFunction = { TeamEntity.getById(it) },
    fetchFunction = { TeamEntity.fetch(it) }
) {

    fun create(omittedTeam: OmittedTeam): Result<Team> = transaction {
        val classEntity =
            ClassEntity.getById(omittedTeam.classId)?.first ?: throw BadRequestException("invalid class id")

        Result.success(
            TeamEntity.new {
                this.name = omittedTeam.name
                this.classEntity = classEntity
            }
                .serializableModel()
                .apply {
                    fetchFunction(this.id)
                }
        )
    }

    fun update(id: Int, omittedTeam: OmittedTeam): Result<Team> = transaction {
        val team = TeamEntity.getById(id)?.first ?: throw NotFoundException("Team not found.")
        val classEntity =
            ClassEntity.getById(omittedTeam.classId)?.first ?: throw BadRequestException("invalid class id")

        team.name = omittedTeam.name
        team.classEntity = classEntity

        Result.success(
            team
                .serializableModel()
                .apply {
                    fetchFunction(this.id)
                }
        )
    }

    fun getUsers(id: Int): Result<List<User>> = transaction {
        val users = TeamEntity.getTeamUsers(id)?.map { it.second } ?: throw NotFoundException("Team not found.")

        Result.success(users)
    }

    fun addUsers(id: Int, userIds: List<Int>): Result<Team> = transaction {
        val team = TeamEntity.getById(id)?.first ?: throw NotFoundException("Team not found.")
        val users = userIds.mapNotNull {
            UserEntity.getById(it)?.first
        }

        team.users = SizedCollection(listOf(team.users.toList(), users).flatten().distinct())

        Result.success(
            team
                .serializableModel()
                .apply {
                    fetchFunction(this.id)
                }
        )
    }

    fun removeUser(id: Int, userId: Int): Result<Team> = transaction {
        val team = TeamEntity.getById(id)?.first ?: throw NotFoundException("Team not found.")

        team.users = SizedCollection(
            team.users.filterNot {
                it.id.value == userId
            }
        )

        Result.success(
            team
                .serializableModel()
                .apply {
                    fetchFunction(id)
                }
        )
    }
}

@Serializable
data class OmittedTeamUsers(
    val users: List<Int>
)