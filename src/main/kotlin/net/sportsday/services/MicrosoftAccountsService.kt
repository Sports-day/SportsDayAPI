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
object MicrosoftAccountsService : StandardService<MicrosoftAccountEntity, MicrosoftAccount>(
    objectName = "Microsoft account",
    _getAllObjectFunction = { MicrosoftAccountEntity.getAll() },
    _getObjectByIdFunction = { MicrosoftAccountEntity.getById(it) },
    fetchFunction = { MicrosoftAccountEntity.fetch(it) },
    onDeleteFunction = {
        //  User -> MicrosoftAccount
        it.userId?.let { userId ->
            UserEntity.fetch(userId)
        }
    },
) {

    fun linkUser(accountId: Int, userId: Int): Result<MicrosoftAccount> {
        val account = MicrosoftAccountEntity.getById(accountId) ?: throw NotFoundException("Microsoft account not found.")

        val user = UserEntity.getById(userId) ?: throw NotFoundException("target User not found.")

        val model = transaction {
            //  update
            account.first.user = user.first

            //  serialize
            account.first.serializableModel()
        }

        //  fetch
        fetchFunction(accountId)
        UserEntity.fetch(userId)

        return Result.success(model)
    }

    fun unlinkUser(accountId: Int): Result<MicrosoftAccount> {
        val account = MicrosoftAccountEntity.getById(accountId) ?: throw NotFoundException("Microsoft account not found.")

        val model = transaction {
            //  update
            account.first.user = null
            //  link later disable
            account.first.linkLater = false

            //  serialize
            account.first.serializableModel()
        }.apply {
            //  fetch
            fetchFunction(accountId)
            UserEntity.fetch(account.second.userId)
        }

        return Result.success(model)
    }

    fun setAccountRole(accountId: Int, role: String): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.getById(accountId)?.first ?: throw NotFoundException("Microsoft account not found")

        //  validation
        val roleObj = Role.values().find { it.value == role } ?: throw BadRequestException("invalid role name")

        //  update
        account.role = roleObj

        //  fetch
        fetchFunction(accountId)

        Result.success(account.serializableModel())
    }

    fun linkLater(accountId: Int): Result<MicrosoftAccount> {
        val account = MicrosoftAccountEntity.getById(accountId)?.first ?: throw NotFoundException("Microsoft account not found")

        val model = transaction {
            if (account.user != null) {
                throw BadRequestException("already linked")
            }

            //  update
            account.linkLater = true

            //  serialize
            account.serializableModel()
        }

        //  fetch
        fetchFunction(accountId)

        return Result.success(model)
    }
}
