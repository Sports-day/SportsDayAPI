package dev.t7e.models

import org.jetbrains.exposed.sql.Table

/**
 * Created by testusuke on 2023/03/03
 * @author testusuke
 */
object TournamentPath: Table("tournament_path") {
    val parent = reference("parent_match", Matches)
    val child = reference("child_match", Matches)
}