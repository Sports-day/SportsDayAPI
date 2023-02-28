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
object Groups: IntIdTable("groups") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val createdAt = datetime("created_at")
}

class Group(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Group>(Groups)

    var name by Groups.name
    var description by Groups.description
    var createdAt by Groups.createdAt
}