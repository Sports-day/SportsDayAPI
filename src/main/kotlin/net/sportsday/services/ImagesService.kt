package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

/**
 * Created by testusuke on 2023/03/16
 * @author testusuke
 */
object ImagesService {

    fun getAll(): Result<List<Image>> {
        val models = transaction {
            ImageEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(
            models
        )
    }

    fun getById(id: Int): Result<Image> {
        val model = transaction {
            ImageEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Image not found.")
        }

        return Result.success(
            model
        )
    }

    fun create(omittedImage: OmittedImage): Result<Image> {
        val model = transaction {
            ImageEntity.new {
                this.data = omittedImage.data
                this.createdAt = LocalDateTime.now()
            }.serializableModel()
        }

        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val image = ImageEntity.findById(id) ?: throw NotFoundException("Image not found.")

            image.delete()
        }

        return Result.success(Unit)
    }

    fun getImageFile(id: Int): Result<ByteArray> {
        val image = transaction {
            val imageEntity = ImageEntity.findById(id) ?: throw NotFoundException("Image not found.")

            try {
                //  check if the data is base64 encoded
                Base64.getDecoder().decode(imageEntity.data)
            } catch (e: IllegalArgumentException) {
                throw RuntimeException("Failed to decode base64 data.")
            }
        }

        return Result.success(
            image
        )
    }
}
