package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.MicrosoftAccount
import net.sportsday.models.MicrosoftAccountEntity
import net.sportsday.models.UserEntity
import net.sportsday.plugins.Role
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/03/06
 * @author testusuke
 */
object MicrosoftAccountsService {

    fun getAll(): Result<List<MicrosoftAccount>> {
        val models = transaction {
            MicrosoftAccountEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(models)
    }

    fun getById(id: Int): Result<MicrosoftAccount> {
        val model = transaction {
            MicrosoftAccountEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Microsoft account not found.")
        }

        return Result.success(model)
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val account = MicrosoftAccountEntity.findById(id) ?: throw NotFoundException("Microsoft account not found.")

            account.delete()
        }

        return Result.success(Unit)
    }

    fun linkUser(accountId: Int, userId: Int): Result<MicrosoftAccount> {
        val model = transaction {
            val account = MicrosoftAccountEntity.findById(accountId)
                ?: throw NotFoundException("Microsoft account not found.")

            val user = UserEntity.findById(userId)
                ?: throw NotFoundException("target User not found.")

            //  update
            account.user = user

            //  serialize
            account.serializableModel()
        }

        return Result.success(model)
    }

    fun unlinkUser(accountId: Int): Result<MicrosoftAccount> {
        val model = transaction {
            val account =
                MicrosoftAccountEntity.findById(accountId) ?: throw NotFoundException("Microsoft account not found.")

            //  update
            account.user = null
            //  link later disable
            account.linkLater = false

            //  serialize
            account.serializableModel()
        }

        return Result.success(model)
    }

    fun setAccountRole(accountId: Int, role: String): Result<MicrosoftAccount> {
        val model = transaction {
            val account =
                MicrosoftAccountEntity.findById(accountId) ?: throw NotFoundException("Microsoft account not found")

            //  validation
            val roleObj = Role.values().find { it.value == role } ?: throw BadRequestException("invalid role name")

            //  update
            account.role = roleObj

            account.serializableModel()
        }

        return Result.success(model)
    }

    fun linkLater(accountId: Int): Result<MicrosoftAccount> {
        val model = transaction {
            val account =
                MicrosoftAccountEntity.findById(accountId)
                    ?: throw NotFoundException("Microsoft account not found")

            if (account.user != null) {
                throw BadRequestException("already linked")
            }

            //  update
            account.linkLater = true

            //  serialize
            account.serializableModel()
        }

        return Result.success(model)
    }
}
