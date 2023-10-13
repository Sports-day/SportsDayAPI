package net.sportsday.models

import kotlinx.serialization.Serializable
import net.sportsday.utils.SmartCache
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/05/29
 * @author testusuke
 */
object Information : IntIdTable("information") {
    val name = varchar("name", 64)
    val content = text("content")
}

class InformationEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : SmartCache<InformationEntity, InformationModel> (
        entityName = "information",
        table = Information,
        duration = 5.minutes,
        serializer = { it.serializableModel() },
    )

    var name by Information.name
    var content by Information.content

    fun serializableModel(): InformationModel {
        return InformationModel(
            id.value,
            name,
            content,
        )
    }
}

@Serializable
data class InformationModel(
    val id: Int,
    val name: String,
    val content: String,
)

@Serializable
data class OmittedInformationModel(
    val name: String,
    val content: String,
)
