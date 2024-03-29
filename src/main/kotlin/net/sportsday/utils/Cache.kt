package net.sportsday.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Created by testusuke on 2023/02/23
 * @author testusuke
 */
object Cache {

    //  default 5 minutes
    val DEFAULT_CACHING_DURATION = 20.seconds

    inline fun <T, R> memoize(expireAfter: Duration = DEFAULT_CACHING_DURATION, crossinline fn: (T) -> R): (T) -> R {
        val cache = mutableMapOf<T, Pair<R, Long>>()

        return {
            val now = System.currentTimeMillis()
            val (value, lastUsed) = cache[it] ?: Pair(fn(it), now)
            if (lastUsed == now || (expireAfter.inWholeMilliseconds > 0 && now - lastUsed > expireAfter.inWholeMilliseconds)) {
                cache[it] = Pair(fn(it), now)
            }
            value
        }
    }

    inline fun <R> memoizeOneObject(expireAfter: Duration = DEFAULT_CACHING_DURATION, crossinline fn: () -> R): () -> R {
        var cache: Pair<R, Long>? = null

        return {
            val now = System.currentTimeMillis()
            val (value, lastUsed) = cache ?: Pair(fn(), now)
            if (lastUsed == now || (expireAfter.inWholeMilliseconds > 0 && now - lastUsed > expireAfter.inWholeMilliseconds)) {
                cache = Pair(fn(), now)
            }
            value
        }
    }
}
