package net.sportsday.services

import io.ktor.server.plugins.*
import kotlinx.serialization.Serializable
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/04/26
 * @author testusuke
 */
object SportsService {

    fun getAll(filter: Boolean = false): Result<List<Sport>> {
        val models = transaction {
            val sports = SportEntity.all().map { it.serializableModel() }

            //  fetch tags
            val tags = TagEntity.all().map { it.serializableModel() }

            //  filter sports by tag status
            if (filter) {
                sports.filter {
                    //  if tagId is null, contain it
                    if (it.tagId == null) {
                        return@filter true
                    }

                    //  if tag is not found, return true
                    val tag = tags.find { tag ->
                        tag.id == it.tagId
                    } ?: return@filter true

                    //  return tag.enabled
                    tag.enabled
                }
            } else {
                sports
            }
        }

        return Result.success(
            models
        )
    }

    fun getById(id: Int): Result<Sport> {
        val model = transaction {
            SportEntity.findById(id)?.serializableModel() ?: throw NotFoundException("invalid sport id")
        }

        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val sport = SportEntity.findById(id) ?: throw NotFoundException("invalid sport id")

            sport.delete()
        }

        return Result.success(Unit)
    }

    fun create(omittedSport: OmittedSport): Result<Sport> {
        val model = transaction {
            val image = omittedSport.iconId?.let { ImageEntity.findById(it) }
            val tag = omittedSport.tagId?.let {
                TagEntity.findById(it)
            }

            val entity = SportEntity.new {
                this.name = omittedSport.name
                this.description = omittedSport.description
                this.iconImage = image
                this.weight = omittedSport.weight
                this.ruleId = omittedSport.ruleId
                this.tag = tag
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            //  serialize
            entity.serializableModel()
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedSport: OmittedSport): Result<Sport> {
        val model = transaction {
            val entity = SportEntity.findById(id) ?: throw NotFoundException("invalid sport id")

            val image = omittedSport.iconId?.let { ImageEntity.findById(it) }
            val tag = omittedSport.tagId?.let {
                TagEntity.findById(it)
            }

            entity.name = omittedSport.name
            entity.description = omittedSport.description
            entity.iconImage = image
            entity.weight = omittedSport.weight
            entity.ruleId = omittedSport.ruleId
            entity.tag = tag
            entity.updatedAt = LocalDateTime.now()

            //  serialize
            entity.serializableModel()
        }

        return Result.success(model)
    }

    fun getProgress(id: Int): Result<Double> {
        val progress = transaction {
            val sport = SportEntity.findById(id) ?: throw NotFoundException("invalid sport id")

            val matches = MatchEntity.find {
                Matches.sport eq sport.id.value
            }
                .map { it.serializableModel() }

            val finished = matches.filter { it.status == MatchStatus.FINISHED }
            finished.size.toDouble() / matches.size.toDouble()
        }

        return Result.success(
            progress
        )
    }
}

@Serializable
data class SportProgressResponse(
    val progress: Double,
)
