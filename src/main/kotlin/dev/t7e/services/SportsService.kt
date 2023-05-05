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
    fun create(omittedSport: OmittedSport): Result<Sport> = transaction {
        val image = omittedSport.iconId?.let { ImageEntity.getById(it) }

        val entity = SportEntity.new {
            this.name = omittedSport.name
            this.description = omittedSport.description
            this.iconImage = image?.first
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }

        Result.success(
            entity.serializableModel().apply {
                fetchFunction(this.id)
            }
        )
    }

    fun update(id: Int, omittedSport: OmittedSport): Result<Sport> = transaction {
        val entity = SportEntity.getById(id)?.first ?: throw NotFoundException("invalid sport id")

        val image = omittedSport.iconId?.let { ImageEntity.getById(it) }

        entity.name = omittedSport.name
        entity.description = omittedSport.description
        entity.iconImage = image?.first
        entity.updatedAt = LocalDateTime.now()


        Result.success(
            entity.serializableModel().apply {
                fetchFunction(this.id)
            }
        )
    }
}