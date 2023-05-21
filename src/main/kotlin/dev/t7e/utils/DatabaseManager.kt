package dev.t7e.utils

import org.jetbrains.exposed.sql.Database
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

        //  connect
        database = Database.connect(
            "jdbc:mysql://$host:$port/$db",
            driver = "com.mysql.cj.jdbc.Driver",
            user = user,
            password = password
        )

        //  set default db
        TransactionManager.defaultDatabase = database
    }
}