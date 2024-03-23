package net.sportsday.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/02/27
 * @author testusuke
 */
object Users : IntIdTable("users") {
    val name = varchar("name", 64)
    val studentId = varchar("student_id", 32)
    val gender = enumerationByName<GenderType>("gender", 10)
    val classEntity = reference("class", Classes, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<UserEntity>(Users)

    var name by Users.name
    var studentId by Users.studentId
    var gender by Users.gender
    var classEntity by ClassEntity referencedOn Users.classEntity
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
    var teams by TeamEntity via TeamUsers
    val microsoftAccounts by MicrosoftAccountEntity optionalReferrersOn MicrosoftAccounts.user

    fun serializableModel(): User {
        return User(
            id.value,
            name,
            studentId,
            gender,
            classEntity.id.value,
            teams.map { it.id.value },
            createdAt.toString(),
            updatedAt.toString(),
        )
    }
}

@Serializable
enum class GenderType(val gender: String) {
    @SerialName("male")
    MALE("male"),

    @SerialName("female")
    FEMALE("female"),
}

@Serializable
data class User(
    val id: Int,
    val name: String,
    val studentId: String,
    val gender: GenderType,
    val classId: Int,
    val teamIds: List<Int>,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class OmittedUser(
    val name: String,
    val studentId: String,
    val gender: GenderType,
    val classId: Int,
)
