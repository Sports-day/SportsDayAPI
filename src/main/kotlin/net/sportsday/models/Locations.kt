package net.sportsday.models

import kotlinx.serialization.Serializable
import net.sportsday.utils.SmartCache
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */
object Locations : IntIdTable("locations") {
    val name = varchar("name", 64)
}

class LocationEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : SmartCache<LocationEntity, Location> (
        entityName = "location",
        table = Locations,
        duration = 5.minutes,
        serializer = { it.serializableModel() },
    )

    var name by Locations.name

    fun serializableModel(): Location {
        return Location(
            id.value,
            name,
        )
    }
}

@Serializable
data class Location(
    val id: Int,
    val name: String,
)

@Serializable
data class OmittedLocation(
    val name: String,
)
