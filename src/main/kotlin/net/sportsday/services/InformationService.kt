package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.InformationEntity
import net.sportsday.models.InformationModel
import net.sportsday.models.OmittedInformationModel
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/05/29
 * @author testusuke
 */
object InformationService {
    fun getAll(): Result<List<InformationModel>> {
        val models = transaction {
            InformationEntity.all().map {
                it.serializableModel()
            }
        }

        return Result.success(
            models
        )
    }

    fun getById(id: Int): Result<InformationModel> {
        val model = transaction {
            InformationEntity.findById(id)?.serializableModel() ?: throw NotFoundException("Information not found.")
        }

        return Result.success(model)
    }

    /**
     * Create new information
     *
     * @param omittedInformation[OmittedInformationModel] information content
     * @return [InformationModel]
     */
    fun create(omittedInformation: OmittedInformationModel): Result<InformationModel> {
        val model = transaction {
            InformationEntity.new {
                this.name = omittedInformation.name
                this.content = omittedInformation.content
            }.serializableModel()
        }

        return Result.success(
            model,
        )
    }

    /**
     * Update information
     *
     * @param id[Int] information id
     * @param omittedInformation[OmittedInformationModel] information content
     * @return [InformationModel]
     */
    fun update(id: Int, omittedInformation: OmittedInformationModel): Result<InformationModel> {
        val model = transaction {
            val entity = InformationEntity.findById(id) ?: throw NotFoundException("invalid information id")

            entity.name = omittedInformation.name
            entity.content = omittedInformation.content

            //  serialize
            entity.serializableModel()
        }

        return Result.success(
            model,
        )
    }

    fun deleteById(id: Int): Result<Unit> {
        transaction {
            val entity = InformationEntity.findById(id) ?: throw NotFoundException("invalid information id")

            entity.delete()
        }

        return Result.success(Unit)
    }
}
