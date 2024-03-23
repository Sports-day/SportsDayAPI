package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.AllowedDomain
import net.sportsday.models.AllowedDomainEntity
import net.sportsday.models.OmittedAllowedDomain
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/03/13
 * @author testusuke
 */
object AllowedDomainsService {

    fun getAll(): Result<List<AllowedDomain>> {
        val models = transaction {
            AllowedDomainEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(
            models,
        )
    }

    fun getById(id: Int): Result<AllowedDomain> {
        val model = transaction {
            AllowedDomainEntity.findById(id)?.serializableModel()
        } ?: throw NotFoundException("invalid id")

        return Result.success(
            model,
        )
    }

    fun create(omittedAllowedDomain: OmittedAllowedDomain): Result<AllowedDomain> {
        val model = transaction {
            AllowedDomainEntity.new {
                this.domain = omittedAllowedDomain.domain
                this.description = omittedAllowedDomain.description
                this.createdAt = LocalDateTime.now()
            }.serializableModel()
        }

        return Result.success(
            model,
        )
    }

    fun update(id: Int, omittedAllowedDomain: OmittedAllowedDomain): Result<AllowedDomain> {
        val entity = AllowedDomainEntity.findById(id) ?: throw NotFoundException("invalid id")

        val model = transaction {
            entity.domain = omittedAllowedDomain.domain
            entity.description = omittedAllowedDomain.description
            //  serialize
            entity.serializableModel()
        }

        return Result.success(
            model,
        )
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val entity = AllowedDomainEntity.findById(id) ?: throw NotFoundException("invalid id")

            //  delete entity
            entity.delete()
        }

        return Result.success(Unit)
    }
}
