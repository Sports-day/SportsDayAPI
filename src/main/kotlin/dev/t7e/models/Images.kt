package dev.t7e.models

import dev.t7e.utils.Cache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/03/14
 * @author testusuke
 */

object Images: IntIdTable("images") {
    val name = varchar("name", 64)
    //  base64 encoded image
    val attachment = text("attachment")
    val createdAt = datetime("created_at")
    val createdBy = reference("created_by", MicrosoftAccounts)
}

class ImageEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ImageEntity>(Images) {
        val getAllImages = Cache.memoizeOneObject {
            transaction {
                ImageEntity.all().toList().map {
                    it to it.serializableModel()
                }
            }
        }

        val getImage: (id: Int) -> Pair<ImageEntity, Image>? = Cache.memoize { id ->
            transaction {
                ImageEntity
                    .findById(id)
                    ?.let {
                        it to it.serializableModel()
                    }
            }
        }
    }

    var name by Images.name
    var attachment by Images.attachment
    var createdAt by Images.createdAt
    var createdBy by MicrosoftAccountEntity referencedOn Images.createdBy

    fun serializableModel(): Image {
        return Image(
            id.value,
            name,
            attachment,
            createdAt.toString(),
            createdBy.id.value
        )
    }
}

@Serializable
data class Image(
    val id: Int,
    val name: String,
    val attachment: String,
    val createdAt: String,
    val createdBy: Int
)

@Serializable
data class OmittedImage(
    val name: String,
    val attachment: String
)