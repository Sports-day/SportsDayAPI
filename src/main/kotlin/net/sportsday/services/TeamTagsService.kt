package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.OmittedTeamTag
import net.sportsday.models.SportEntity
import net.sportsday.models.TeamTag
import net.sportsday.models.TeamTagEntity
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2024/03/28
 * @author testusuke
 */
object TeamTagsService {

    fun getAll(): Result<List<TeamTag>> {
        val models = transaction {
            TeamTagEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(models)
    }

    fun getById(id: Int): Result<TeamTag> {
        val model = transaction {
            TeamTagEntity.findById(id)?.serializableModel() ?: throw NotFoundException("TeamTag not found.")
        }
        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val teamTag = TeamTagEntity.findById(id) ?: throw NotFoundException("TeamTag not found.")

            teamTag.delete()
        }

        return Result.success(Unit)
    }

    fun create(omittedTeamTag: OmittedTeamTag): Result<TeamTag> {
        val model = transaction {
            val sport = omittedTeamTag.sportId?.let {
                SportEntity.findById(it) ?: throw NotFoundException("invalid sport id")
            }

            val teamTag = TeamTagEntity.new {
                this.name = omittedTeamTag.name
                this.sport = sport
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            teamTag.serializableModel()
        }

        return Result.success(model)
    }

    fun edit(id: Int, omittedTeamTag: OmittedTeamTag): Result<TeamTag> {
        val model = transaction {
            val teamTag = TeamTagEntity.findById(id) ?: throw NotFoundException("TeamTag not found.")
            val sport = omittedTeamTag.sportId?.let {
                SportEntity.findById(it) ?: throw NotFoundException("invalid sport id")
            }

            teamTag.name = omittedTeamTag.name
            teamTag.sport = sport
            teamTag.updatedAt = LocalDateTime.now()

            teamTag.serializableModel()
        }

        return Result.success(model)
    }
}
