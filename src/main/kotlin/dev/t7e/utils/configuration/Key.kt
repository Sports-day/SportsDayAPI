package dev.t7e.utils.configuration

/**
 * Created by testusuke on 2023/02/22
 * @author testusuke
 */
sealed class Key(val key: String, val default: String) {

    /**
     * This configuration is used to determine whether the game preview is restricted.
     */
    object RestrictGamePreview : Key("restrict_game_preview", false.toString())

    /**
     * This configuration is used to determine percentage of the game preview.
     */
    object RestrictGamePreviewPercentage : Key("restrict_game_preview_percentage", 0.6.toString())
}
