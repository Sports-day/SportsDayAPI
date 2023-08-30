package dev.t7e.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Created by testusuke on 2023/05/05
 * @author testusuke
 */

object Entries : Table("game_entries") {
    val game = reference("game", Games, onDelete = ReferenceOption.CASCADE)
    val team = reference("team", Teams, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(game, team, name = "pk_game_entries")
}
