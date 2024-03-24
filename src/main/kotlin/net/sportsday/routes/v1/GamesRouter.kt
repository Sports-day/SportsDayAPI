package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import net.sportsday.models.OmittedGame
import net.sportsday.services.GamesService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.DataResponse
import net.sportsday.utils.MessageResponse
import net.sportsday.utils.respondOrInternalError

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */
fun Route.gamesRouter() {
    route("/games") {
        /**
         * Get all games
         */
        get {
            val games = GamesService.getAll(
                call.request.queryParameters["filter"] == "true",
            )

            call.respond(
                HttpStatusCode.OK,
                DataResponse(games.getOrDefault(listOf())),
            )
        }

        /**
         * Create new game
         */
        post {
            val requestBody = call.receive<OmittedGame>()

            GamesService
                .create(requestBody)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "created game",
                            it,
                        ),
                    )
                }
        }

        route("/{id?}") {
            /**
             * Get game by id
             */
            get {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                GamesService
                    .getById(id)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataResponse(it),
                        )
                    }
            }

            /**
             * Update game
             */
            put {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")
                val requestBody = call.receive<OmittedGame>()

                GamesService
                    .update(id, requestBody)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "updated game",
                                it,
                            ),
                        )
                    }
            }

            /**
             * Delete game
             */
            delete {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                GamesService
                    .deleteById(id)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            MessageResponse(
                                "deleted game",
                            ),
                        )
                    }
            }
        }

        route("/matches") {
            /**
             * Get matches
             */
            get {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                GamesService
                    .getMatches(
                        id,
                        call.request.queryParameters["restrict"] == "true",
                    )
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataResponse(it),
                        )
                    }
            }

            /**
             * Delete all matches
             */
            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")

                GamesService
                    .deleteAllMatches(id)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            MessageResponse(
                                "deleted matches",
                            ),
                        )
                    }
            }
        }

        route("/entries") {
            /**
             * Get entries
             */
            get {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                GamesService
                    .getEntries(id)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataResponse(it),
                        )
                    }
            }

            /**
             * enter game
             */
            post {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")
                val requestBody = call.receive<EntryRequest>()

                GamesService
                    .enterGame(id, requestBody.teamIds)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "entered game",
                                it,
                            ),
                        )
                    }
            }

            /**
             * leave game
             */
            delete("/{teamId}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")
                val teamId = call.parameters["teamId"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid teamId parameter")

                GamesService
                    .cancelEntry(id, teamId)
                    .respondOrInternalError {
                        call.respond(
                            HttpStatusCode.OK,
                            DataMessageResponse(
                                "left game",
                                it,
                            ),
                        )
                    }
            }
        }
    }

    route("/tournament") {
        /**
         * Make tournament tree
         */
        post {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")
            val parent = call.receive<TournamentTreeCreateRequest>()

            GamesService
                .makeTournamentTree(id, parent.parentId)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "made tournament tree",
                            it,
                        ),
                    )
                }
        }

        /**
         * Update tournament tree recursively
         */
        post("/update-tree") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")

            GamesService
                .updateTournamentTree(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("updated tournament tree"),
                    )
                }
        }

        /**
         * Get tournament result
         */
        get("/result") {
            val id =
                call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

            GamesService
                .getTournamentResult(id)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "calculated tournament result",
                            it,
                        ),
                    )
                }
        }
    }

    route("/league") {
        /**
         * Make league matches
         */
        post {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("invalid id parameter")
            val location = call.receive<LocationRequest>()

            GamesService
                .makeLeagueMatches(id, location.locationId)
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "made league matches",
                            it,
                        ),
                    )
                }
        }

        /**
         * Get league result
         */
        get("/result") {
            val id =
                call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

            GamesService
                .calculateLeagueResults(
                    id,
                    call.request.queryParameters["restrict"] == "true",
                )
                .respondOrInternalError {
                    call.respond(
                        HttpStatusCode.OK,
                        DataMessageResponse(
                            "calculated league results",
                            it,
                        ),
                    )
                }
        }
    }
}

@Serializable
data class EntryRequest(
    val teamIds: List<Int>,
)

@Serializable
data class LocationRequest(
    val locationId: Int?,
)

@Serializable
data class TournamentTreeCreateRequest(
    val parentId: Int?,
)
