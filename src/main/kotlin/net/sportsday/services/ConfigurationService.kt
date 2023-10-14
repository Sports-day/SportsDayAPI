package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.utils.configuration.Key
import net.sportsday.utils.configuration.KeyValueStore

/**
 * Created by testusuke on 2023/10/15
 * @author testusuke
 */
object ConfigurationService {

    object RestrictGamePreview {
        fun isEnabled(): Result<Boolean> {
            return Result.success(KeyValueStore.get(Key.RestrictGamePreview)?.toBoolean() ?: throw NotFoundException("not found configuration key"))
        }

        fun setEnabled(enabled: Boolean): Result<Unit> {
            return Result.success(KeyValueStore.set(Key.RestrictGamePreview, enabled.toString()))
        }

        fun getPercentage(): Result<Double> {
            return Result.success(KeyValueStore.get(Key.RestrictGamePreviewPercentage)?.toDouble() ?: throw NotFoundException("not found configuration key"))
        }

        fun setPercentage(percentage: Double): Result<Unit> {
            return Result.success(KeyValueStore.set(Key.RestrictGamePreviewPercentage, percentage.toString()))
        }
    }
}
