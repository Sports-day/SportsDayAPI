package dev.t7e.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */

object Sports: IntIdTable("sports") {
    val name = varchar("name", 64)
    val description = text("description")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class SportEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<SportEntity>(Sports)

    var name by Sports.name
    var description by Sports.description
    var createdAt by Sports.createdAt
    var updatedAt by Sports.updatedAt

    fun serializableModel(): Sport {
        return Sport(
            id.value,
            name,
            description,
            createdAt.toString(),
            updatedAt.toString()
        )
    }
 }

@Serializable
data class Sport(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String
)