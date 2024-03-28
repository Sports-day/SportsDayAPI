package net.sportsday.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */
object Classes : IntIdTable("classes") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class ClassEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ClassEntity>(Classes)

    var name by Classes.name
    var description by Classes.description
    var createdAt by Classes.createdAt
    var updatedAt by Classes.updatedAt
    val users by UserEntity optionalReferrersOn Users.classEntity

    fun serializableModel(): ClassModel {
        return ClassModel(
            id.value,
            name,
            description,
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
data class ClassModel(
    val id: Int,
    val name: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedClassModel(
    val name: String,
    val description: String?,
)
