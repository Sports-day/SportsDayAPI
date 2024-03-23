package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/19
 * @author testusuke
 */
object TeamsService {

    fun getAll(): Result<List<Team>> {
        val models = transaction {
            TeamEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(models)
    }

    fun getById(id: Int): Result<Team> {
        val model = transaction {
            TeamEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Team not found.")
        }
        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val team = TeamEntity.findById(id) ?: throw NotFoundException("Team not found.")

            team.delete()
        }

        return Result.success(Unit)
    }


    fun create(omittedTeam: OmittedTeam): Result<Team> {
        val model = transaction {
            val classEntity =
                ClassEntity.findById(omittedTeam.classId) ?: throw BadRequestException("invalid class id")

            val team = TeamEntity.new {
                this.name = omittedTeam.name
                this.description = omittedTeam.description
                this.classEntity = classEntity
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            team.serializableModel()
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedTeam: OmittedTeam): Result<Team> {
        val model = transaction {
            val team = TeamEntity.findById(id) ?: throw NotFoundException("Team not found.")
            val classEntity = ClassEntity.findById(omittedTeam.classId)
                ?: throw BadRequestException("invalid class id")

            team.name = omittedTeam.name
            team.description = omittedTeam.description
            team.classEntity = classEntity
            team.updatedAt = LocalDateTime.now()

            team.serializableModel()
        }

        return Result.success(model)
    }

    fun getUsers(id: Int): Result<List<User>> {
        val users = transaction {
            val team = TeamEntity.findById(id) ?: throw NotFoundException("Team not found.")

            team.users.map {
                it.serializableModel()
            }
        }

        return Result.success(users)
    }

    fun addUsers(id: Int, userIds: List<Int>): Result<Team> {
        val model = transaction {
            val team = TeamEntity.findById(id) ?: throw NotFoundException("Team not found.")
            val users = userIds.mapNotNull {
                UserEntity.findById(it)
            }

            team.users = SizedCollection(listOf(team.users.toList(), users).flatten().distinct())
            team.updatedAt = LocalDateTime.now()

            team.serializableModel()
        }

        return Result.success(model)
    }

    fun removeUser(id: Int, userId: Int): Result<Team> {
        val model = transaction {
            val team = TeamEntity.findById(id) ?: throw NotFoundException("Team not found.")
            val user = UserEntity.findById(userId) ?: throw NotFoundException("User not found.")

            team.users = SizedCollection(
                team.users.filterNot {
                    it.id.value == user.id.value
                },
            )
            team.updatedAt = LocalDateTime.now()

            team.serializableModel()
        }

        return Result.success(model)
    }
}
