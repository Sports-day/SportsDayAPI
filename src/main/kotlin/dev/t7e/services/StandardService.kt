package dev.t7e.services

import io.ktor.server.plugins.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/03/07
 * @author testusuke
 */

/**
 * This extendable class include id-based R(read)D(delete) feature
 *
 * @param T the id-based table entity
 * @param R the serializable model
 * @property objectName the name of this object
 * @property _getAllObjectFunction the lambda of get all object
 * @property _getObjectByIdFunction the lambda of get object by id
 */
open class StandardService<T : IntEntity, R>(
    private val objectName: String,
    private val _getAllObjectFunction: () -> List<Pair<T, R>>,
    private val _getObjectByIdFunction: (id: Int) -> Pair<T, R>?,
) {

    /**
     * Get all object
     */
    open fun getAll(): Result<List<R>> {
        return Result.success(
            transaction {
                _getAllObjectFunction().map {
                    it.second
                }
            }
        )
    }

    /**
     * Get object by id
     *
     * @param id[Int] object id
     */
    open fun getById(id: Int): Result<R> {
        val obj = _getObjectByIdFunction(id) ?: throw NotFoundException("$objectName not found.")

        return Result.success(obj.second)
    }

    /**
     * Delete object by is
     *
     * @param id[Int] object id
     */
    open fun deleteById(id: Int): Result<Boolean> = transaction {
        _getObjectByIdFunction(id)?.first?.delete() ?: throw NotFoundException("$objectName not found.")

        Result.success(true)
    }
}