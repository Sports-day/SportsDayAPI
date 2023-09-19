package dev.t7e.bin

import dev.t7e.models.initializeTables
import dev.t7e.utils.DatabaseManager

/**
 * Created by testusuke on 2023/09/07
 * @author testusuke
 */
fun main(args: Array<String>) {
    println("executing seeder... $args")

    //  initialize database
    DatabaseManager
    //  initialize tables
    initializeTables()

    //  create models
}
