package net.sportsday.services

import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/16
 * @author testusuke
 */
object ImagesService : StandardService<ImageEntity, Image>(
    objectName = "image",
    _getAllObjectFunction = { ImageEntity.getAll() },
    _getObjectByIdFunction = { ImageEntity.getById(it) },
    fetchFunction = { ImageEntity.fetch(it) },
    onDeleteFunction = {
        //  Sport -> Image
        SportEntity.getAll().forEach { pair ->
            if (pair.second.iconId == it.id) {
                SportEntity.fetch(pair.second.id)
            }
        }
    },
) {

    fun create(createdBy: MicrosoftAccountEntity, omittedImage: OmittedImage): Result<Image> {
        val model = transaction {
            ImageEntity.new {
                this.name = omittedImage.name
                this.attachment = omittedImage.attachment
                this.createdAt = LocalDateTime.now()
                this.createdBy = createdBy
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }
}
