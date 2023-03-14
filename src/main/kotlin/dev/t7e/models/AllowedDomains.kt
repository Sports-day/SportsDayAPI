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
    companion object: IntEntityClass<AllowedDomainEntity>(AllowedDomains) {
        val getAllowedDomainByDomain: (domain: String) -> AllowedDomainEntity? = Cache.memoize { domain ->
            transaction {
                AllowedDomainEntity.find{ AllowedDomains.domain eq domain}.singleOrNull()
            }
        }

        val getAllAllowedDomains: () -> List<Pair<AllowedDomainEntity, AllowedDomain>> = Cache.memoizeOneObject {
            transaction {
                AllowedDomainEntity.all().toList().map {
                    it to it.serializableModel()
                }
            }
        }

        val getAllowedDomain: (id: Int) -> Pair<AllowedDomainEntity, AllowedDomain>? = Cache.memoize { id ->
            transaction {
                AllowedDomainEntity
                    .findById(id)
                    ?.let {
                        it to it.serializableModel()
                    }
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