package dev.t7e.utils

/**
 * Created by testusuke on 2023/03/09
 * @author testusuke
 */

suspend inline fun <T> Result<T>.respondOrInternalError(fn: (T) -> Unit) {
    onFailure {
        throw it
    }
    onSuccess {
        fn(it)
    }
}