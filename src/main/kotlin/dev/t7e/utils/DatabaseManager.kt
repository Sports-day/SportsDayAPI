package dev.t7e.utils

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * Created by testusuke on 2023/02/22
 * @author testusuke
 */
object DatabaseManager {
    val database: Database

    init {
        val host = System.getenv("DATABASE_HOST")
        val port = System.getenv("DATABASE_PORT")
        val user = System.getenv("DATABASE_USER")
        val password = System.getenv("DATABASE_PASSWORD")
        val db = System.getenv("DATABASE_DB")

//        val config = DatabaseConfig.invoke {
//            maxEntitiesToStoreInCachePerEntity = 0
//        }

        //  connect
        database = Database.connect(
            "jdbc:mysql://$host:$port/$db",
            driver = "com.mysql.cj.jdbc.Driver",
            user = user,
            password = password,
//            databaseConfig = config
        )

        //  set default db
        TransactionManager.defaultDatabase = database
    }
}