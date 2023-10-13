package net.sportsday.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */
object Logs : IntIdTable("logs") {
    val logEvent = enumerationByName<LogEvents>("log_event", 32)
    val microsoftAccount = reference("microsoft_account", MicrosoftAccounts, onDelete = ReferenceOption.SET_NULL).nullable()
    val message = text("message")
    val createdAt = datetime("created_at")
}

class LogEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LogEntity>(Logs)

    var logEvent by Logs.logEvent
    var microsoftAccount by MicrosoftAccountEntity optionalReferencedOn Logs.microsoftAccount
    var message by Logs.message
    var createdAt by Logs.createdAt
}

enum class LogEvents(val event: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    ERROR("error"),
    INFO("info"),
    DEBUG("debug"),
    WARN("warn"),
}

@Serializable
data class Log(
    val id: Int,
    val logEvent: LogEvents,
    val microsoftAccount: Int?,
    val message: String,
    val createdAt: String,
)
