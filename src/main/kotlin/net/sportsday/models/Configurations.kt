package net.sportsday.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/02/22
 * @author testusuke
 */
object Configurations : IntIdTable("configuration") {
    val key = varchar("key", 128)
    val value = varchar("value", 1024)

    //  Note: convert UTC time before insert
    val updatedAt = datetime("updated_at")
}
