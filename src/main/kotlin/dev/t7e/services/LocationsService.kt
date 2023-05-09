package dev.t7e.services

import dev.t7e.models.Location
import dev.t7e.models.LocationEntity
import dev.t7e.models.OmittedLocation
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */
object LocationsService: StandardService<LocationEntity, Location>(
    objectName = "location",
    _getAllObjectFunction = { LocationEntity.getAll() },
    _getObjectByIdFunction = { LocationEntity.getById(it) },
    fetchFunction = { LocationEntity.fetch(it) }
) {

    fun create(omittedLocation: OmittedLocation): Result<Location> = transaction {
        val entity = LocationEntity.new {
            this.name = omittedLocation.name
        }

        Result.success(
            entity.serializableModel().apply {
                fetchFunction(this.id)
            }
        )
    }

    fun update(id: Int, omittedLocation: OmittedLocation): Result<Location> = transaction {
        val entity = LocationEntity.getById(id)?.first ?: throw NotFoundException("invalid location id")

        entity.name = omittedLocation.name

        Result.success(
            entity.serializableModel().apply {
                fetchFunction(this.id)
            }
        )
    }
}