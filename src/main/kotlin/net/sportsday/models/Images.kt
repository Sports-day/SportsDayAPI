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
    //  base64 encoded image
    val data = text("data")
    val createdAt = datetime("created_at")
}

class ImageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ImageEntity> (Images)

    var data by Images.data
    var createdAt by Images.createdAt

    fun serializableModel(): Image {
        return Image(
            id.value,
            data,
            createdAt.toString(),
        )
    }
}

@Serializable
data class Image(
    val id: Int,
    val data: String,
    val createdAt: String,
)

@Serializable
data class OmittedImage(
    val data: String,
)
