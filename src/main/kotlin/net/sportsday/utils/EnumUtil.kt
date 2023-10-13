package net.sportsday.utils

/**
 * Created by testusuke on 2023/05/07
 * @author testusuke
 */

inline fun <reified T : Enum<T>> safeValueOf(type: String): T? {
    return try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (e: IllegalArgumentException) {
        null
    }
}
