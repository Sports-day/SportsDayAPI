package net.sportsday.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Created by testusuke on 2023/03/14
 * @author testusuke
 */

object Images : IntIdTable("images") {
    val name = varchar("name", 64)

    //  base64 encoded image
    val attachment = text("attachment")
    val createdAt = datetime("created_at")
}

class ImageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ImageEntity> (Images)

    var name by Images.name
    var attachment by Images.attachment
    var createdAt by Images.createdAt

    fun serializableModel(): Image {
        return Image(
            id.value,
            name,
            attachment,
            createdAt.toString(),
        )
    }
}

@Serializable
data class Image(
    val id: Int,
    val name: String,
    val attachment: String,
    val createdAt: String,
)

@Serializable
data class OmittedImage(
    val name: String,
    val attachment: String,
)
