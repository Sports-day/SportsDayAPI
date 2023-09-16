package dev.t7e.utils

import kotlinx.serialization.Serializable

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */

@Serializable
data class DataResponse<T>(
    val data: T,
)

@Serializable
data class DataMessageResponse<M, T>(
    val message: M,
    val data: T,
)

@Serializable
data class MessageResponse<M>(
    val message: M,
)
