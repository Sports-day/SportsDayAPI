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
object InformationService : StandardService<InformationEntity, InformationModel>(
    objectName = "Information",
    _getAllObjectFunction = { InformationEntity.getAll() },
    _getObjectByIdFunction = { InformationEntity.getById(it) },
    fetchFunction = { InformationEntity.fetch(it) },
) {

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
        }.apply {
            fetchFunction(this.id)
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
        val entity = InformationEntity.getById(id)?.first ?: throw NotFoundException("invalid information id")

        val model = transaction {
            entity.name = omittedInformation.name
            entity.content = omittedInformation.content
            //  serialize
            entity.serializableModel()
        }.apply {
            fetchFunction(this.id)
        }

        return Result.success(
            model,
        )
    }
}
