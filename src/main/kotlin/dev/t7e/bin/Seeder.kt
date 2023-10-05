package dev.t7e.bin

import dev.t7e.models.*
import dev.t7e.services.GamesService
import dev.t7e.utils.DatabaseManager
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/09/07
 * @author testusuke
 */
fun main(args: Array<String>) {
    println("executing seeder...")

    //  initialize database
    DatabaseManager

    //  drop
    println("Dropping tables...")
    dropTables()

    //  initialize tables
    println("Creating tables...")
    initializeTables()

    //  create models
    transaction {
        //  group
        val group = GroupEntity.new {
            name = "General"
            description = "General group"
            createdAt = java.time.LocalDateTime.now()
            updatedAt = java.time.LocalDateTime.now()
        }

        //  class
        val classes = listOf(
            ClassEntity.new {
                name = "Class A"
                description = "A1 class"
                this.group = group
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            ClassEntity.new {
                name = "Class B"
                description = "B1 class"
                this.group = group
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            ClassEntity.new {
                name = "Class C"
                description = "C1 class"
                this.group = group
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            }
        )

        //  user
        val users = listOf(
            UserEntity.new {
                name = "testusuke"
                studentId = "2011240"
                gender = GenderType.MALE
                classEntity = classes[0]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "Olivia"
                studentId = "xxxxxxx"
                gender = GenderType.FEMALE
                classEntity = classes[0]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "Emma"
                studentId = "xxxxxxx"
                gender = GenderType.FEMALE
                classEntity = classes[0]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "nayu"
                studentId = "2011124"
                gender = GenderType.MALE
                classEntity = classes[1]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "Amelia"
                studentId = "xxxxxxx"
                gender = GenderType.FEMALE
                classEntity = classes[1]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "Oliver"
                studentId = "xxxxxxx"
                gender = GenderType.MALE
                classEntity = classes[1]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "Luna"
                studentId = "xxxxxxx"
                gender = GenderType.FEMALE
                classEntity = classes[2]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "Ethan"
                studentId = "xxxxxxx"
                gender = GenderType.MALE
                classEntity = classes[2]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            UserEntity.new {
                name = "James"
                studentId = "xxxxxxx"
                gender = GenderType.MALE
                classEntity = classes[2]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
        )

        //  Team
        val teams = listOf(
            TeamEntity.new {
                name = "Team A-1"
                description = "Team A-1"
                classEntity = classes[0]
                this.users = SizedCollection(users[0], users[1])
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            TeamEntity.new {
                name = "Team A-2"
                description = "Team A-2"
                classEntity = classes[0]
                this.users = SizedCollection(users[2])
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            TeamEntity.new {
                name = "Team B-1"
                description = "Team B-1"
                classEntity = classes[1]
                this.users = SizedCollection(users[3], users[4])
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            TeamEntity.new {
                name = "Team B-2"
                description = "Team B-2"
                classEntity = classes[1]
                this.users = SizedCollection(users[5])
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            TeamEntity.new {
                name = "Team C-1"
                description = "Team C-1"
                classEntity = classes[2]
                this.users = SizedCollection(users[6], users[7])
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            TeamEntity.new {
                name = "Team C-2"
                description = "Team C-2"
                classEntity = classes[2]
                this.users = SizedCollection(users[8])
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
        )

        //  tag
        val tags = listOf(
            TagEntity.new {
                name = "晴天時"
                enabled = true
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            TagEntity.new {
                name = "雨天時"
                enabled = false
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
        )

        //  sports
        val sports = listOf(
            SportEntity.new {
                name = "Soccer"
                description = "Soccer"
                iconImage = null
                weight = 10
                ruleId = 1
                tag = tags[0]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
            SportEntity.new {
                name = "Basketball"
                description = "Basketball"
                iconImage = null
                weight = 5
                ruleId = 1
                tag = tags[1]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            },
        )

        //  game
        val games = listOf(
            GameEntity.new {
                name = "Soccer League 1"
                description = "Soccer Game"
                sport = sports[0]
                type = GameType.LEAGUE
                calculationType = CalculationType.TOTAL_SCORE
                weight = 10
                tag = tags[0]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
                this.teams = SizedCollection(teams[0], teams[2], teams[4], teams[5])
            },
            GameEntity.new {
                name = "Soccer League 2"
                description = "Soccer Game"
                sport = sports[0]
                type = GameType.LEAGUE
                calculationType = CalculationType.TOTAL_SCORE
                weight = 5
                tag = tags[1]
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
                this.teams = SizedCollection(teams[1], teams[3], teams[4], teams[5])
            },
            GameEntity.new {
                name = "BasketBall League 1"
                description = "BasketBall Game"
                sport = sports[1]
                type = GameType.LEAGUE
                calculationType = CalculationType.TOTAL_SCORE
                weight = 10
                tag = null
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
                this.teams = SizedCollection(teams[1], teams[3], teams[4], teams[5])
            },
        )

        //  location
        val locations = listOf(
            LocationEntity.new {
                name = "Soccer Field 1"
            },
            LocationEntity.new {
                name = "Soccer Field 2"
            },
            LocationEntity.new {
                name = "Basketball Court"
            },
        )

        //  create league match
        listOf(
            GamesService.makeLeagueMatches(games[0].id.value, locations[0].id.value),
            GamesService.makeLeagueMatches(games[1].id.value, locations[1].id.value),
            GamesService.makeLeagueMatches(games[2].id.value, locations[2].id.value),
        ).forEach {
            if (it.isFailure) {
                println("Failed to generate. error: ${it.exceptionOrNull()?.message}")
            }
        }
    }

    println("Seeder successfully executed.")
}
