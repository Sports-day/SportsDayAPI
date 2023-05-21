package dev.t7e.services

import dev.t7e.models.MicrosoftAccount
import dev.t7e.models.MicrosoftAccountEntity
import dev.t7e.models.UserEntity
import dev.t7e.plugins.Role
import io.ktor.server.plugins.*
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
    }
) {

    fun linkUser(accountId: Int, userId: Int): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.getById(accountId) ?: throw NotFoundException("Microsoft account not found.")

        val user = UserEntity.getById(userId) ?: throw NotFoundException("target User not found.")

        //  update
        account.first.user = user.first

        //  fetch
        fetchFunction(accountId)
        UserEntity.fetch(userId)

        Result.success(account.first.serializableModel())
    }

    fun unlinkUser(accountId: Int): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.getById(accountId) ?: throw NotFoundException("Microsoft account not found.")

        //  update
        account.first.user = null

        //  fetch
        fetchFunction(accountId)
        UserEntity.fetch(account.second.userId)

        Result.success(account.first.serializableModel())
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

}