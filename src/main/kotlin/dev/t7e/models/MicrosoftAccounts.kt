package dev.t7e.models

import dev.t7e.utils.Cache
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */

object MicrosoftAccounts: IntIdTable("microsoft_accounts") {
    val email = varchar("email", 320).uniqueIndex()
    val name = varchar("name", 128)
    val mailAccountName = varchar("mail_account_name", 128).nullable()
    val user = reference("user", Users).nullable()
    val firstLogin = datetime("first_login")
    val lastLogin = datetime("last_login")
}

class MicrosoftAccount(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<MicrosoftAccount>(MicrosoftAccounts) {
        val getMicrosoftAccount: (email: String) -> MicrosoftAccount? = Cache.memoize(1000 * 60 * 1) { email ->
            transaction {
                MicrosoftAccount.find{ MicrosoftAccounts.email eq email }.singleOrNull()
            }
        }

        val existMicrosoftAccount: (email: String) -> Boolean = Cache.memoize(1000 * 60 * 1) { email ->
            getMicrosoftAccount(email) != null
        }
    }

    var email by MicrosoftAccounts.email
    var name by MicrosoftAccounts.name
    var mailAccountName by MicrosoftAccounts.mailAccountName
    var user by User optionalReferencedOn MicrosoftAccounts.user
    var firstLogin by MicrosoftAccounts.firstLogin
    var lastLogin by MicrosoftAccounts.lastLogin
}