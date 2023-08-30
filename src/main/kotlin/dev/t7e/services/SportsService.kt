package dev.t7e.services

import dev.t7e.models.ImageEntity
import dev.t7e.models.OmittedSport
import dev.t7e.models.Sport
import dev.t7e.models.SportEntity
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/04/26
 * @author testusuke
 */
object SportsService : StandardService<SportEntity, Sport>(
    objectName = "sport",
    _getAllObjectFunction = { SportEntity.getAll() },
    _getObjectByIdFunction = { SportEntity.getById(it) },
    fetchFunction = { SportEntity.fetch(it) }
) {
    fun create(omittedSport: OmittedSport): Result<Sport> {
        val image = omittedSport.iconId?.let { ImageEntity.getById(it) }

        val model = transaction {
            SportEntity.new {
                this.name = omittedSport.name
                this.description = omittedSport.description
                this.iconImage = image?.first
                this.weight = omittedSport.weight
                this.ruleId = omittedSport.ruleId
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedSport: OmittedSport): Result<Sport> {
        val entity = SportEntity.getById(id)?.first ?: throw NotFoundException("invalid sport id")

        val image = omittedSport.iconId?.let { ImageEntity.getById(it) }

        val model = transaction {
            entity.name = omittedSport.name
            entity.description = omittedSport.description
            entity.iconImage = image?.first
            entity.weight = omittedSport.weight
            entity.ruleId = omittedSport.ruleId
            entity.updatedAt = LocalDateTime.now()

            entity.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }
}
