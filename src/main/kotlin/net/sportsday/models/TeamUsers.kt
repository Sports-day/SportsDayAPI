package net.sportsday.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Created by testusuke on 2023/03/02
 * @author testusuke
 */
object TeamUsers : Table("team_users") {
    val team = reference("team", Teams, onDelete = ReferenceOption.CASCADE)
    val user = reference("user", Users, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(team, user, name = "pk_team_users")
}
