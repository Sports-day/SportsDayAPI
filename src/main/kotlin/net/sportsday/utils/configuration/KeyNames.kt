package net.sportsday.utils.configuration

import net.sportsday.models.Configurations
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/02/22
 * @author testusuke
 */
object KeyNames {

    private val initializeKey = listOf(
        Key.RestrictGamePreview,
        Key.RestrictGamePreviewPercentage
    )

    init {

        transaction {
            val existedValues = Configurations.selectAll().map {
                it[Configurations.key] to it[Configurations.value]
            }

            val list = initializeKey.filter {
                for (pair in existedValues) {
                    if (pair.first == it.key) {
                        return@filter false
                    }
                }
                return@filter true
            }

            for (key in list) {
                Configurations.insert {
                    it[Configurations.key] = key.key
                    it[value] = key.default
                    it[updatedAt] = LocalDateTime.now()
                }
            }
        }
    }
}
