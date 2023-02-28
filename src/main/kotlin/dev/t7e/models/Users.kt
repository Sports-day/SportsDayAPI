package dev.t7e.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/02/27
 * @author testusuke
 */
object Users: IntIdTable("users") {
    val name = varchar("name", 64)
    val studentId = varchar("student_id", 32)
    val classEntity = reference("class", Classes)
    val createdAt = datetime("created_at")
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<User>(Users)

    var name by Users.name
    var studentId by Users.studentId
    var classEntity by Class referencedOn Users.classEntity
    var createdAt by Users.createdAt
}