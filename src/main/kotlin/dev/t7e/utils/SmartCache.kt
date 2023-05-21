package dev.t7e.utils

import org.jetbrains.annotations.Nullable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration

/**
 * Created by testusuke on 2023/03/16
 * @author testusuke
 */

/**
 * Cache all object as primary-key to value map.
 *
 * @param T the id-based table entity
 * @param R the serializable model
 */
open class SmartCache<T : IntEntity, R>(
    private val entityName: String,
    table: IdTable<Int>,
    private val duration: Duration,
    private val serializer: (T) -> R
) : IntEntityClass<T>(table) {
    /**
     * main cache
     *
     * Int to Pair<IntEntity, Model>
     */
    protected val cache = mutableMapOf<Int, Pair<T, R>?>()

    /**
     * outside fetch functions that will be executed after main fetch function
     */
    private val fetchFunctionList = mutableListOf<(id: Int?) -> Unit>()

    private var lastUpdate: Long = 0

    init {
        println("initializing cache system for $entityName")

        //  redis
        RedisManager.registerFetchFunction(entityName) {
            fetch(it)
        }
    }

    fun fetch(id: Int? = null, redisPublish: Boolean = true) {
        transaction {
            if(id == null) {
                cache.clear()

                //  fetch all
                all().forEach { entity ->
                    cache[entity.id.value] = entity to serializer(entity)
                }
            } else {
                //  fetch by id
                val entity = findById(id)

                if (entity == null) {
                    cache[id] = null
                } else {
                    cache[id] = entity to serializer(entity)
                }
            }
        }

        //  outside fetch function
        fetchFunctionList.forEach { it(id) }

        if (id != null && redisPublish) {
            //  redis
            RedisManager.publish(
                RedisMessageContent(
                    type = entityName,
                    id = id
                )
            )
        }
    }

    /**
     * Register fetch function
     */
    protected fun registerFetchFunction(fn: (id: Int?) -> Unit) {
        fetchFunctionList.add(fn)
    }

    protected fun checkCacheLifetime() {
        val now = System.currentTimeMillis()

        if (lastUpdate == now || (duration.inWholeMilliseconds > 0 && now - lastUpdate > duration.inWholeMilliseconds)) {
            //  fetch
            fetch(redisPublish = false)

            //  update
            lastUpdate = now
        }
    }

    /**
     * Get all entity and model pairs
     *
     * @return List<Pair<IntEntity, Model>>
     */
    fun getAll(): List<Pair<T, R>> {
        //  check cache lifetime
        checkCacheLifetime()

        return cache.values.filterNotNull()
    }

    /**
     * Get entity and model pair
     *
     * @return Pair<IntEntity, Model>?
     */
    @Nullable
    fun getById(id: Int): Pair<T, R>? {
        //  check cache lifetime
        checkCacheLifetime()

        if(!cache.containsKey(id)) {
            //  fetch unknown data
            fetch(id, redisPublish = false)
        }

        return cache[id]
    }
}