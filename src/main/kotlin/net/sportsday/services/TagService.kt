package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.OmittedTag
import net.sportsday.models.Tag
import net.sportsday.models.TagEntity
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/10/02
 * @author testusuke
 */
object TagService {

    fun getAll(): Result<List<Tag>> {
        val models = transaction {
            TagEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(
            models
        )
    }

    fun getById(id: Int): Result<Tag> {
        val model = transaction {
            TagEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Tag not found.")
        }

        return Result.success(model)
    }

    fun create(omittedTag: OmittedTag): Result<Tag> {
        val model = transaction {
            val tag = TagEntity.new {
                this.name = omittedTag.name
                this.enabled = omittedTag.enabled
                this.createdAt = LocalDateTime.now()
                this.updatedAt = LocalDateTime.now()
            }

            tag.serializableModel()
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedTag: OmittedTag): Result<Tag> {
        val model = transaction {
            val entity = TagEntity.findById(id) ?: throw NotFoundException("invalid tag id")

            entity.name = omittedTag.name
            entity.enabled = omittedTag.enabled
            entity.updatedAt = LocalDateTime.now()

            entity.serializableModel()
        }
        return Result.success(model)
    }
}
