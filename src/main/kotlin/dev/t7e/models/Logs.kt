package dev.t7e.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */
object Logs: IntIdTable("logs") {
    val logEvent = enumerationByName<LogEvents>("log_event", 32)
    val microsoftAccount = reference("microsoft_account", MicrosoftAccounts).nullable()
    val message = text("message")
    val createdAt = datetime("created_at")
}

class LogEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<LogEntity>(Logs)

    var logEvent by Logs.logEvent
    var microsoftAccount by MicrosoftAccountEntity optionalReferencedOn Logs.microsoftAccount
    val message by Logs.message
    val createdAt by Logs.createdAt
}

enum class LogEvents(val event: String) {

}