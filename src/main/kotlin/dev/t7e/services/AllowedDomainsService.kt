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
object AllowedDomainsService : StandardService<AllowedDomainEntity, AllowedDomain>(
    objectName = "allowed domain",
    _getAllObjectFunction = { AllowedDomainEntity.getAll() },
    _getObjectByIdFunction = { AllowedDomainEntity.getById(it) },
    fetchFunction = { AllowedDomainEntity.fetch(it) }
) {

    fun create(omittedAllowedDomain: OmittedAllowedDomain): Result<AllowedDomain> {
        val model = transaction {
            AllowedDomainEntity.new {
                this.domain = omittedAllowedDomain.domain
                this.description = omittedAllowedDomain.description
                this.createdAt = LocalDateTime.now()
            }.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(
            model
        )
    }

    fun update(id: Int, omittedAllowedDomain: OmittedAllowedDomain): Result<AllowedDomain> {
        val entity = AllowedDomainEntity.getById(id) ?: throw NotFoundException("invalid id")

        val model = transaction {
            entity.first.domain = omittedAllowedDomain.domain
            entity.first.description = omittedAllowedDomain.description
            //  serialize
            entity.first.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(
            model
        )
    }
}