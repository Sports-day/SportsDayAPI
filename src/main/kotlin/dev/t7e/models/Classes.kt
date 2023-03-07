package dev.t7e.models

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
object Classes: IntIdTable("classes") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val group = reference("group", Groups)
    val createdAt = datetime("created_at")
}

class ClassEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ClassEntity>(Classes)

    var name by Classes.name
    var description by Classes.description
    var group by GroupEntity referencedOn Classes.group
    var createdAt by Classes.createdAt

    fun serializableModel(): Class {
        return Class(
            id.value,
            name,
            description,
            group.serializableModel(),
            createdAt.toString()
        )
    }
}

@Serializable
data class Class(
    val id: Int,
    val name: String,
    val description: String?,
    val group: Group,
    val createdAt: String
)