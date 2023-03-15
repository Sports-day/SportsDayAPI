package dev.t7e.services

import dev.t7e.models.Image
import dev.t7e.models.ImageEntity
import dev.t7e.models.MicrosoftAccountEntity
import dev.t7e.models.OmittedImage
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/16
 * @author testusuke
 */
object ImagesService: StandardService<ImageEntity, Image>(
    objectName = "image",
    _getAllObjectFunction = { ImageEntity.getAllImages() },
    _getObjectByIdFunction = { ImageEntity.getImage(it) }
) {

    fun create(createdBy: Int, omittedImage: OmittedImage): Result<Image> = transaction {
        val user = MicrosoftAccountEntity.findById(createdBy) ?: throw NotFoundException("invalid microsoft account")

        val entity = ImageEntity.new {
            this.name = omittedImage.name
            this.attachment = omittedImage.attachment
            this.createdAt = LocalDateTime.now()
            this.createdBy = user
        }

        Result.success(entity.serializableModel())
    }

}