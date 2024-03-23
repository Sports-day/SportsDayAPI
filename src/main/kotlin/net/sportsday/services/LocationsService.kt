package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.Location
import net.sportsday.models.LocationEntity
import net.sportsday.models.OmittedLocation
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */
object LocationsService {

    fun getAll(): Result<List<Location>> {
        val models = transaction {
            LocationEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(models)
    }

    fun getById(id: Int): Result<Location> {
        val model = transaction {
            LocationEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Location not found.")
        }

        return Result.success(model)
    }

    fun create(omittedLocation: OmittedLocation): Result<Location> {
        val model = transaction {
            LocationEntity.new {
                this.name = omittedLocation.name
            }.serializableModel()
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedLocation: OmittedLocation): Result<Location> {
        val model = transaction {
            val entity = LocationEntity.findById(id) ?: throw NotFoundException("invalid location id")

            entity.name = omittedLocation.name

            //  serialize
            entity.serializableModel()
        }

        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val location = LocationEntity.findById(id) ?: throw NotFoundException("Location not found.")

            //  delete
            location.delete()
        }

        return Result.success(Unit)
    }
}
