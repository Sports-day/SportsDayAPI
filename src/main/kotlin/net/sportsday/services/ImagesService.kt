package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

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
                this.name = omittedImage.name
                this.attachment = omittedImage.attachment
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
}
