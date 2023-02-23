package dev.t7e.utils

/**
 * Created by testusuke on 2023/02/23
 * @author testusuke
 */
object Cache {

    //  default 5 minutes
    const val DEFAULT_CACHING_DURATION: Long = 1000 * 60 * 5

    inline fun <T, R> memoize(expireAfter: Long = DEFAULT_CACHING_DURATION, crossinline fn: (T) -> R): (T) -> R {
        val cache = mutableMapOf<T, Pair<R, Long>>()

        return {
            val now = System.currentTimeMillis()
            val (value, lastUsed) = cache[it] ?: Pair(fn(it), now)
            if (lastUsed == now || (expireAfter > 0 && now - lastUsed > expireAfter)) {
                cache[it] = Pair(fn(it), now)
            }
            value
        }
    }
}