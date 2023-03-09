package dev.t7e.models

import dev.t7e.utils.Cache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */

object MicrosoftAccounts : IntIdTable("microsoft_accounts") {
    val email = varchar("email", 320).uniqueIndex()
    val name = varchar("name", 128)
    val mailAccountName = varchar("mail_account_name", 128).nullable()
    val user = reference("user", Users).nullable()
    val firstLogin = datetime("first_login")
    val lastLogin = datetime("last_login")
}

class MicrosoftAccountEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MicrosoftAccountEntity>(MicrosoftAccounts) {
        val getAllMicrosoftAccounts = Cache.memoizeOneObject(1.minutes) {
            transaction {
                MicrosoftAccountEntity.all().toList()
            }
        }

        val getMicrosoftAccountById: (id: Int) -> MicrosoftAccountEntity? = Cache.memoize(1.minutes) { id ->
            transaction {
                MicrosoftAccountEntity.findById(id)
            }
        }

        fun getMicrosoftAccount(email: String): MicrosoftAccountEntity? = transaction {
            MicrosoftAccountEntity.find { MicrosoftAccounts.email eq email }.singleOrNull()
        }


        fun existMicrosoftAccount(email: String): Boolean = getMicrosoftAccount(email) != null
    }

    var email by MicrosoftAccounts.email
    var name by MicrosoftAccounts.name
    var mailAccountName by MicrosoftAccounts.mailAccountName
    var user by UserEntity optionalReferencedOn MicrosoftAccounts.user
    var firstLogin by MicrosoftAccounts.firstLogin
    var lastLogin by MicrosoftAccounts.lastLogin

    fun serializableModel(): MicrosoftAccount {
        return MicrosoftAccount(
            id.value,
            email,
            name,
            mailAccountName,
            user?.id?.value,
            firstLogin.toString(),
            lastLogin.toString()
        )
    }
}

@Serializable
data class MicrosoftAccount(
    val id: Int,
    val email: String,
    val name: String,
    val mailAccountName: String?,
    val userId: Int?,
    val firstLogin: String,
    val lastLogin: String
)