package dev.t7e.services

import dev.t7e.models.AllowedDomain
import dev.t7e.models.AllowedDomainEntity
import dev.t7e.models.OmittedAllowedDomain
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/13
 * @author testusuke
 */
object AllowedDomainsService: StandardService<AllowedDomainEntity, AllowedDomain>(
    objectName = "allowed domain",
    _getAllObjectFunction = { AllowedDomainEntity.getAllAllowedDomains() },
    _getObjectByIdFunction = { AllowedDomainEntity.getAllowedDomain(it) }
) {

    fun create(omittedAllowedDomain: OmittedAllowedDomain): Result<AllowedDomain> = transaction {
        Result.success(
            AllowedDomainEntity.new {
                this.domain = omittedAllowedDomain.domain
                this.description = omittedAllowedDomain.description
                this.createdAt = LocalDateTime.now()
            }.serializableModel()
        )
    }

    fun update(id: Int, omittedAllowedDomain: OmittedAllowedDomain): Result<AllowedDomain> = transaction {
        val entity = AllowedDomainEntity.getAllowedDomain(id) ?: throw NotFoundException("invalid id")

        entity.first.domain = omittedAllowedDomain.domain
        entity.first.description = omittedAllowedDomain.description

        Result.success(entity.first.serializableModel())
    }
}