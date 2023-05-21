package dev.t7e.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Created by testusuke on 2023/03/03
 * @author testusuke
 */
object TournamentPath: Table("tournament_path") {
    val parent = reference("parent_match", Matches, onDelete = ReferenceOption.CASCADE)
    val child = reference("child_match", Matches, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(parent, child, name = "pk_tournament_path")
}