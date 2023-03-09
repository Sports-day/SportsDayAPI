package dev.t7e.services

import io.ktor.server.plugins.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */
open class StandardService<T : IntEntity, R>(
    private val objectName: String,
    private val _getAllObjectFunction: () -> List<T>,
    private val _getObjectByIdFunction: (id: Int) -> T?,
    private val _serialize: (T) -> R
) {

    open fun getAll(): Result<List<R>> {
        return Result.success(
            transaction {
                _getAllObjectFunction().map(_serialize)
            }
        )
    }

    open fun getById(id: Int): Result<R> {
        val obj = _getObjectByIdFunction(id) ?: throw NotFoundException("$objectName not found.")

        return Result.success(
            transaction {
                _serialize(obj)
            }
        )
    }
    open fun deleteById(id: Int): Result<Boolean> = transaction {
        _getObjectByIdFunction(id)?.delete() ?: throw NotFoundException("$objectName not found.")

        Result.success(true)
    }
}