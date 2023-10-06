package dev.t7e.services

import dev.t7e.models.*
import io.ktor.server.plugins.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/04/26
 * @author testusuke
 */
object SportsService {

    fun getAll(filter: Boolean = false): Result<List<Sport>> {
        val sports = SportEntity.getAll().map { it.second }

        val filteredSports = if (filter) {
            //  fetch tags
            val tags = TagEntity.getAll().map { it.second }

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

        return Result.success(
            filteredSports
        )
    }

    fun getById(id: Int): Result<Sport> {
        val sport = SportEntity.getById(id)?.second ?: throw NotFoundException("invalid sport id")

        return Result.success(sport)
    }

    fun deleteById(id: Int): Result<Boolean> {
        val sport = SportEntity.getById(id)?.first ?: throw NotFoundException("invalid sport id")

        transaction {
            sport.delete()
        }

        //  fetch
        SportEntity.fetch(id)

        return Result.success(true)
    }

    fun create(omittedSport: OmittedSport): Result<Sport> {
        val image = omittedSport.iconId?.let { ImageEntity.getById(it) }
        val tag = omittedSport.tagId?.let {
            TagEntity.getById(it)
        }

        val model = transaction {
            SportEntity.new {
                this.name = omittedSport.name
                this.description = omittedSport.description
                this.iconImage = image?.first
                this.weight = omittedSport.weight
                this.ruleId = omittedSport.ruleId
                this.tag = tag?.first
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }.serializableModel()
        }.apply {
            SportEntity.fetch(this.id)
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedSport: OmittedSport): Result<Sport> {
        val entity = SportEntity.getById(id)?.first ?: throw NotFoundException("invalid sport id")

        val image = omittedSport.iconId?.let { ImageEntity.getById(it) }
        val tag = omittedSport.tagId?.let {
            TagEntity.getById(it)
        }

        val model = transaction {
            entity.name = omittedSport.name
            entity.description = omittedSport.description
            entity.iconImage = image?.first
            entity.weight = omittedSport.weight
            entity.ruleId = omittedSport.ruleId
            entity.tag = tag?.first
            entity.updatedAt = LocalDateTime.now()

            entity.serializableModel()
        }.apply {
            SportEntity.fetch(this.id)
        }

        return Result.success(model)
    }

    fun getProgress(id: Int): Result<Double> {
        val sport = SportEntity.getById(id)?.second ?: throw NotFoundException("invalid sport id")

        val matches = MatchEntity.getAll()
            .map { it.second }
            .filter { it.sportId == sport.id }

        val finished = matches.filter { it.status == MatchStatus.FINISHED }
        val progress = finished.size.toDouble() / matches.size.toDouble()

        return Result.success(
            progress
        )
    }
}

@Serializable
data class SportProgressResponse(
    val progress: Double,
)
