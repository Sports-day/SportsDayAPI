package net.sportsday.models

import kotlinx.serialization.Serializable
import net.sportsday.plugins.Role
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */

object MicrosoftAccounts : IntIdTable("microsoft_accounts") {
    val email = varchar("email", 320).uniqueIndex()
    val name = varchar("name", 128)
    val mailAccountName = varchar("mail_account_name", 128).nullable()
    val role = enumerationByName<Role>("role", 32).default(Role.USER)
    val user = reference("user", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val linkLater = bool("link_later").default(false)
    val firstLogin = datetime("first_login")
    val lastLogin = datetime("last_login")
}

class MicrosoftAccountEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MicrosoftAccountEntity> (MicrosoftAccounts)

    var email by MicrosoftAccounts.email
    var name by MicrosoftAccounts.name
    var mailAccountName by MicrosoftAccounts.mailAccountName
    var role by MicrosoftAccounts.role
    var user by UserEntity optionalReferencedOn MicrosoftAccounts.user
    var linkLater by MicrosoftAccounts.linkLater
    var firstLogin by MicrosoftAccounts.firstLogin
    var lastLogin by MicrosoftAccounts.lastLogin

    fun serializableModel(): MicrosoftAccount {
        return MicrosoftAccount(
            id.value,
            email,
            name,
            mailAccountName,
            role.value,
            user?.id?.value,
            linkLater,
            firstLogin.toString(),
            lastLogin.toString(),
        )
    }
}

@Serializable
data class MicrosoftAccount(
    val id: Int,
    val email: String,
    val name: String,
    val mailAccountName: String?,
    val role: String,
    val userId: Int?,
    val linkLater: Boolean,
    val firstLogin: String,
    val lastLogin: String,
)
