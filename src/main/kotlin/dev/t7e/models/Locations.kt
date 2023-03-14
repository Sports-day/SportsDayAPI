package dev.t7e.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */
object Locations: IntIdTable("locations") {
    val name = varchar("name", 64)
}

class LocationEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<LocationEntity>(Locations)

    var name by Locations.name

    fun serializableModel(): Location {
        return Location(
            id.value,
            name
        )
    }
}

@Serializable
data class Location(
    val id: Int,
    val name: String
)