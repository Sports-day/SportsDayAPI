package dev.t7e.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/01
 * @author testusuke
 */
object Teams: IntIdTable("teams") {
    val name = varchar("name", 64)
    val classEntity = reference("class", Classes)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class Team(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Team>(Teams)

    var name by Teams.name
    var classEntity by Teams.classEntity
    var users by User via TeamUsers
}