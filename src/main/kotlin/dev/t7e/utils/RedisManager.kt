package dev.t7e.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.UUID
import kotlin.concurrent.thread

/**
 * Created by testusuke on 2023/05/21
 * @author testusuke
 */
object RedisManager {

    const val CHANNEL = "sports-day-fetch"
    private lateinit var redisPool: JedisPool

    //  identity uuid
    private var uuid: String

    //  fetchFunction list
    private val fetchFunctionList = mutableMapOf<String, (id: Int?) -> Unit>()

    init {
        println("initializing redis manager")
        //  uuid
        uuid = UUID.randomUUID().toString()
        println("redis manager uuid: $uuid")

        if (System.getenv("REDIS_URL") == null) {
            println("No redis host specified, skipping redis initialization")
        } else {
            redisPool = JedisPool(System.getenv("REDIS_URL"))

            thread(isDaemon = true) {
                redisPool.resource.use { jedis ->
                    val listener = FetchListener(
                        uuid = uuid,
                        callback = {
                            //  fetch
                            val fetchFunction = fetchFunctionList[it.type]
                            if (fetchFunction != null) {
                                fetchFunction(it.id)
                            }
                        }
                    )
                    jedis.subscribe(listener, CHANNEL)
                }
            }
        }
    }

    fun publish(content: RedisMessageContent) {
        if (System.getenv("REDIS_URL") == null) {
            return
        }

        redisPool.resource.use{ jedis ->
            val message = RedisMessage(
                from = uuid,
                data = content
            )
            //  publish
            val encoded = Json.encodeToString(message)
            jedis.publish(CHANNEL, encoded)
        }
    }

    fun registerFetchFunction(type: String, function: (id: Int?) -> Unit) {
        fetchFunctionList[type] = function
    }

}

class FetchListener(private val uuid: String, private val callback: (RedisMessageContent) -> Unit): JedisPubSub() {
    override fun onMessage(channel: String?, message: String?) {
        if (channel != RedisManager.CHANNEL || message == null) {
            return
        }

        val messageObject = Json.decodeFromString(RedisMessage.serializer(), message)

        //  uuid check
        if (uuid == messageObject.from) {
            return
        }

        //  callback
        callback(messageObject.data)

        //  log
        println("${messageObject.data.type}:${messageObject.data.id ?: "all"} from ${messageObject.from}")
    }

    override fun onSubscribe(channel: String?, subscribedChannels: Int) {
        println("subscribed to $channel")
    }

    override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
        println("unsubscribed from $channel")
    }
}

@Serializable
data class RedisMessageContent(
    val type: String,
    val id: Int?
)

@Serializable
data class RedisMessage(
    val from: String,
    val data: RedisMessageContent
)