package net.sportsday.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Created by testusuke on 2023/02/28
 * @author testusuke
 */

fun Route.withRole(role: Role, build: Route.() -> Unit): Route {
    val route = createChild(CustomSelector())
    val plugin = createRouteScopedPlugin("CustomAuthorization") {
        on(AuthenticationChecked) { call ->
            val principal = call.authentication.principal<UserPrincipal>()
            // custom logic
            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@on
            }
            if (principal.roles.none { it == role }) {
                call.respond(HttpStatusCode.Forbidden)
                return@on
            }
        }
    }
    route.install(plugin)
    route.build()
    return route
}

private class CustomSelector : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Transparent
}
