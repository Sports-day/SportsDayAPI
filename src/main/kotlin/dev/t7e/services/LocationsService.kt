package dev.t7e.services

import dev.t7e.models.Location
import dev.t7e.models.LocationEntity
import dev.t7e.models.MatchEntity
import dev.t7e.models.OmittedLocation
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */
object LocationsService : StandardService<LocationEntity, Location>(
    objectName = "location",
    _getAllObjectFunction = { LocationEntity.getAll() },
    _getObjectByIdFunction = { LocationEntity.getById(it) },
    fetchFunction = { LocationEntity.fetch(it) },
    onDeleteFunction = {
        //  Location -> Match
        MatchEntity.getAll().forEach { pair ->
            if (pair.second.locationId == it.id) {
                MatchEntity.fetch(pair.second.id)
            }
        }
    }
) {

    fun create(omittedLocation: OmittedLocation): Result<Location> {
        val model = transaction {
            LocationEntity.new {
                this.name = omittedLocation.name
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedLocation: OmittedLocation): Result<Location> {
        val entity = LocationEntity.getById(id)?.first ?: throw NotFoundException("invalid location id")

        val model = transaction {
            entity.name = omittedLocation.name
            //  serialize
            entity.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }
}
