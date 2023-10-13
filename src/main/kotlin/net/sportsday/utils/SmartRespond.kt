package net.sportsday.utils

/**
 * Created by testusuke on 2023/03/09
 * @author testusuke
 */

inline fun <T> Result<T>.respondOrInternalError(fn: (T) -> Unit) {
    onFailure {
        throw it
    }
    onSuccess {
        fn(it)
    }
}
