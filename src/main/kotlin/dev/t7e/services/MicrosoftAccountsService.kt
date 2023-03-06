package dev.t7e.services

import dev.t7e.models.MicrosoftAccount
import dev.t7e.models.MicrosoftAccountEntity
import dev.t7e.models.UserEntity
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/03/06
 * @author testusuke
 */
object MicrosoftAccountsService {

    fun getAllMicrosoftAccounts(): Result<List<MicrosoftAccount>> = transaction {
        Result.success(MicrosoftAccountEntity.all().map(MicrosoftAccountEntity::serializableModel))
    }

    fun getMicrosoftAccount(accountId: Int): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.findById(accountId) ?: return@transaction Result.failure(NotFoundException("Microsoft account not found."))

        Result.success(account.serializableModel())
    }

    fun deleteMicrosoftAccount(accountId: Int): Result<Boolean> = transaction {
        MicrosoftAccountEntity.findById(accountId)?.delete() ?: return@transaction Result.failure(NotFoundException("Microsoft account not found."))
        Result.success(true)
    }

    fun linkUser(accountId: Int, userId: Int): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.findById(accountId) ?: return@transaction Result.failure(NotFoundException("Microsoft account not found."))
        val user = UserEntity.findById(userId) ?: return@transaction Result.failure(NotFoundException("target User not found."))

        //  update
        account.user = user

        Result.success(account.serializableModel())
    }

    fun unlinkUser(accountId: Int): Result<MicrosoftAccount> = transaction {
        val account = MicrosoftAccountEntity.findById(accountId) ?: return@transaction Result.failure(NotFoundException("Microsoft account not found."))

        //  update
        account.user = null

        Result.success(account.serializableModel())
    }

}