package dev.t7e.models

import dev.t7e.utils.Cache
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
    val updatedAt = datetime("updated_at")
}

class AllowedDomain(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<AllowedDomain>(AllowedDomains) {
        val getAllowedDomainByDomain: (domain: String) -> AllowedDomain? = Cache.memoize(1000 * 60 * 1) { domain ->
            transaction {
                AllowedDomain.find{ AllowedDomains.domain eq domain}.singleOrNull()
            }
        }
    }

    var domain by AllowedDomains.domain
    var description by AllowedDomains.description
    var createdAt by AllowedDomains.createdAt
    var updatedAt by AllowedDomains.updatedAt
}