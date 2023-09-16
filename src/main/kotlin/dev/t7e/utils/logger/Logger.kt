package dev.t7e.utils.logger

import dev.t7e.models.Log
import dev.t7e.models.LogEntity
import dev.t7e.models.LogEvents
import dev.t7e.models.MicrosoftAccountEntity
import dev.t7e.utils.DiscordEmbed
import dev.t7e.utils.DiscordMessage
import dev.t7e.utils.getColorValue
import dev.t7e.utils.postDiscordMessage
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
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
            1000L * 60L * 10L,
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
                createdAt = LocalDateTime.now().toString(),
            ),
        )
    }

    private fun push() {
        transaction {
            val embeds = mutableListOf<DiscordEmbed>()

            logs.forEach { log ->
                LogEntity.new {
                    this.logEvent = log.logEvent
                    this.microsoftAccount = log.microsoftAccount?.let { MicrosoftAccountEntity.findById(it) }
                    this.message = log.message
                    this.createdAt = LocalDateTime.parse(log.createdAt)
                }

                //  discord
                if (log.logEvent == LogEvents.ERROR) {
                    embeds.add(
                        DiscordEmbed(
                            title = "Error",
                            description = log.message,
                            color = Color.RED.getColorValue(),
                        ),
                    )
                }
            }

            if (embeds.isNotEmpty()) {
                val message = DiscordMessage(
                    username = "SportsDayAPI",
                    embeds = embeds,
                )
                //  send
                postDiscordMessage(message)
            }

            //  clear logs
            logs.clear()
        }
    }
}
