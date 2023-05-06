package dev.t7e.models

import org.jetbrains.exposed.sql.Table

/**
 * Created by testusuke on 2023/05/05
 * @author testusuke
 */

object Entries: Table("game_entries") {
    val game = reference("game", Games)
    val team = reference("team", Teams)

    override val primaryKey = PrimaryKey(game, team, name = "pk_game_entries")
}
