package net.sportsday.utils.configuration

import net.sportsday.models.Configurations
import net.sportsday.utils.RedisManager
import net.sportsday.utils.RedisMessageContent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/02/22
 * @author testusuke
 */
object KeyValueStore {

    private val cachedValues = mutableMapOf<String, String>()

    init {
        //  initialize keys
        KeyNames
        //  initial fetch
        fetch()

        //  register redis
        RedisManager.registerFetchFunction("configurations") {
            fetch()
        }
    }

    fun get(key: Key): String? {
        return cachedValues[key.key]
    }

    fun getAll(): MutableMap<String, String> {
        return cachedValues
    }

    fun set(key: Key, value: String) {
        //  cached
        cachedValues[key.key] = value

        //  set
        transaction {
            val count = Configurations.select { Configurations.key eq key.key }.count()
            if (count > 0) {
                //  update
                Configurations.update({ Configurations.key eq key.key }) {
                    it[Configurations.value] = value
                    it[updatedAt] = LocalDateTime.now()
                }
            } else {
                //  insert
                Configurations.insert {
                    it[Configurations.key] = key.key
                    it[Configurations.value] = value
                    it[updatedAt] = LocalDateTime.now()
                }
            }
        }

        //  redis
        RedisManager.publish(
            RedisMessageContent(
                type = "configurations",
                id = null,
            ),
        )
    }

    fun fetch() {
        //  clear
        cachedValues.clear()

        //  fetch
        transaction {
            Configurations.selectAll().forEach {
                cachedValues[it[Configurations.key]] = it[Configurations.value]
            }
        }
    }
}
