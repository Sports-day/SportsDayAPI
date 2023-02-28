package dev.t7e.models

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

class Class(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Class>(Classes)

    var name by Classes.name
    var description by Classes.description
    var group by Group referencedOn Classes.group
    var createdAt by Classes.createdAt
}