package dev.t7e.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Color
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by testusuke on 2023/05/13
 * @author testusuke
 */

fun postDiscordMessage(message: DiscordMessage) {
    val webhookUrl = System.getenv("DISCORD_WEBHOOK_URL") ?: return

    try {
        sendRequest(
            url = webhookUrl,
            method = "POST",
            headers = mapOf("Content-Type" to "application/json"),
            body = Json.encodeToString(message),
        )
    } catch (e: Exception) {
        println("failed to send discord message.")
    }
}

fun Color.getColorValue(): Int {
    return this.red * 256 * 256 + this.green * 256 + this.blue
}

@Serializable
data class DiscordEmbed(
    val title: String,
    val description: String,
    val color: Int,
    val timestamp: String = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
)

@Serializable
data class DiscordMessage(
    val username: String,
    val embeds: List<DiscordEmbed>,
)

fun sendRequest(url: String, method: String = "GET", headers: Map<String, String>? = null, body: String? = null): Response {
    val conn = URL(url).openConnection() as HttpURLConnection

    with(conn) {
        requestMethod = method
        doOutput = body != null
        headers?.forEach(this::setRequestProperty)
    }

    if (body != null) {
        conn.outputStream.use {
            it.write(body.toByteArray())
        }
    }

    val responseBody = conn.inputStream.use { it.readBytes() }.toString(Charsets.UTF_8)

    return Response(conn.responseCode, conn.headerFields, responseBody)
}

data class Response(val statusCode: Int, val headers: Map<String, List<String>>? = null, val body: String? = null)
