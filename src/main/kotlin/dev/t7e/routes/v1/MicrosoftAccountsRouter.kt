package dev.t7e.routes.v1

import dev.t7e.plugins.Role
import dev.t7e.plugins.withRole
import dev.t7e.services.MicrosoftAccountsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Created by testusuke on 2023/03/06
 * @author testusuke
 */

fun Route.microsoftAccountsRouter() {
    route("/microsoft-accounts") {
        authenticate {
            withRole(Role.ADMIN) {
                /**
                 * Get all microsoft accounts
                 */
                get {
                    val accounts = MicrosoftAccountsService.getAllMicrosoftAccounts()

                    call.respond(accounts.getOrDefault(listOf()))
                }

                /**
                 * Get specific microsoft account
                 */
                get("{id?}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
                    val account = MicrosoftAccountsService.getMicrosoftAccount(id).getOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)

                    call.respond(account)
                }

                /**
                 * Delete specific microsoft account
                 */
                delete("{id?}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.NotFound)
                    MicrosoftAccountsService.deleteMicrosoftAccount(id)
                        .onFailure {
                            call.respond(HttpStatusCode.NotFound)
                        }
                        .onSuccess {
                            call.respond(HttpStatusCode.OK)
                        }
                }
            }
        }
    }
}