package dev.t7e.utils.logger

import dev.t7e.models.Log
import dev.t7e.models.LogEntity
import dev.t7e.models.LogEvents
import dev.t7e.models.MicrosoftAccountEntity
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.Collections.synchronizedList
import kotlin.concurrent.fixedRateTimer

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */
object Logger {

    private val logs = synchronizedList(mutableListOf<Log>())

    init {
        println("initializing logger system")

        fixedRateTimer(
            "LoggerPushTimer",
            true,
            1000L * 60L * 1L,
            1000L * 60L * 10L
        ) {
            push()
        }
    }

    fun commit(message: String, event: LogEvents, causedBy: MicrosoftAccountEntity?) {
        logs.add(
            Log(
                id = 0,
                logEvent = event,
                microsoftAccount = causedBy?.id?.value,
                message = message,
                createdAt = LocalDateTime.now().toString()
            )
        )
    }

    private fun push() {
        transaction {
            logs.forEach { log ->
                LogEntity.new {
                    this.logEvent = log.logEvent
                    this.microsoftAccount = log.microsoftAccount?.let { MicrosoftAccountEntity.findById(it) }
                    this.message = log.message
                    this.createdAt = LocalDateTime.parse(log.createdAt)
                }
            }

            //  clear logs
            logs.clear()
        }
    }

}