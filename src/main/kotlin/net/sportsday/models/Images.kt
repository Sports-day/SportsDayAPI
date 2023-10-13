package net.sportsday.models

import kotlinx.serialization.Serializable
import net.sportsday.utils.SmartCache
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/03/14
 * @author testusuke
 */

object Images : IntIdTable("images") {
    val name = varchar("name", 64)

    //  base64 encoded image
    val attachment = text("attachment")
    val createdAt = datetime("created_at")
    val createdBy = reference("created_by", MicrosoftAccounts, onDelete = ReferenceOption.SET_NULL).nullable()
}

class ImageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : SmartCache<ImageEntity, Image> (
        entityName = "image",
        table = Images,
        duration = 5.minutes,
        serializer = { it.serializableModel() },
    )

    var name by Images.name
    var attachment by Images.attachment
    var createdAt by Images.createdAt
    var createdBy by MicrosoftAccountEntity optionalReferencedOn Images.createdBy

    fun serializableModel(): Image {
        return Image(
            id.value,
            name,
            attachment,
            createdAt.toString(),
            createdBy?.id?.value ?: -1,
        )
    }
}

@Serializable
data class Image(
    val id: Int,
    val name: String,
    val attachment: String,
    val createdAt: String,
    val createdBy: Int,
)

@Serializable
data class OmittedImage(
    val name: String,
    val attachment: String,
)