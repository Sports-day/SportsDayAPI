package dev.t7e.services

import dev.t7e.models.OmittedTag
import dev.t7e.models.Tag
import dev.t7e.models.TagEntity
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/10/02
 * @author testusuke
 */
object TagService : StandardService<TagEntity, Tag>(
    objectName = "tag",
    _getAllObjectFunction = { TagEntity.getAll() },
    _getObjectByIdFunction = { TagEntity.getById(it) },
    fetchFunction = { TagEntity.fetch(it) },
) {

    fun create(omittedTag: OmittedTag): Result<Tag> {
        val model = transaction {
            TagEntity.new {
                this.name = omittedTag.name
                this.enabled = omittedTag.enabled
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }

    fun update(id: Int, omittedTag: OmittedTag): Result<Tag> {
        val entity = TagEntity.getById(id)?.first ?: throw NotFoundException("invalid tag id")

        val model = transaction {
            entity.name = omittedTag.name
            entity.enabled = omittedTag.enabled

            entity.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(model)
    }
}
