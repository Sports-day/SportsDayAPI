package dev.t7e.models

import dev.t7e.utils.SmartCache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2023/02/23
 * @author testusuke
 */
object AllowedDomains: IntIdTable("allowed_domains") {
    //  maximum domain length is under 253...?
    val domain = varchar("domain", 256)
    val description = varchar("description", 128)
    val createdAt = datetime("created_at")
}

class AllowedDomainEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: SmartCache<AllowedDomainEntity, AllowedDomain> (
        entityName = "allowed domain",
        table =  AllowedDomains,
        duration = 5.minutes,
        serializer = { it.serializableModel() }
    ) {
        fun getByDomain(domain: String): Pair<AllowedDomainEntity, AllowedDomain>? {
            checkCacheLifetime()

            return cache.values.filterNotNull().find {
                it.second.domain == domain
            }
        }
    }

    var domain by AllowedDomains.domain
    var description by AllowedDomains.description
    var createdAt by AllowedDomains.createdAt

    fun serializableModel(): AllowedDomain {
        return AllowedDomain(
            id.value,
            domain,
            description,
            createdAt.toString()
        )
    }
}

@Serializable
data class AllowedDomain(
    val id: Int,
    val domain: String,
    val description: String,
    val createdAt: String
)

@Serializable
data class OmittedAllowedDomain(
    val domain: String,
    val description: String
)