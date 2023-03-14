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
    _getAllObjectFunction = { MicrosoftAccountEntity.getAllMicrosoftAccounts() },
    _getObjectByIdFunction = { MicrosoftAccountEntity.getMicrosoftAccountById(it) },
) {

    fun linkUser(accountId: Int, userId: Int): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.getMicrosoftAccountById(accountId) ?: throw NotFoundException("Microsoft account not found.")

        val user = UserEntity.getUser(userId) ?: throw NotFoundException("target User not found.")

        //  update
        account.first.user = user.first

        Result.success(account.first.serializableModel())
    }

    fun unlinkUser(accountId: Int): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.getMicrosoftAccountById(accountId) ?: throw NotFoundException("Microsoft account not found.")

        //  update
        account.first.user = null

        Result.success(account.first.serializableModel())
    }

    fun setAccountRole(accountId: Int, role: String): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.getMicrosoftAccountById(accountId)?.first ?: throw NotFoundException("Microsoft account not found")

        //  validation
        val roleObj = Role.values().find { it.value == role } ?: throw BadRequestException("invalid role name")

        //  update
        account.role = roleObj

        Result.success(account.serializableModel())
    }

}