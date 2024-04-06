package net.sportsday.services

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.sportsday.models.Permission
import net.sportsday.models.RoleEntity
import net.sportsday.models.Roles
import net.sportsday.models.UserEntity
import net.sportsday.utils.Cache
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

/**
 * Created by testusuke on 2024/04/06
 * @author testusuke
 */

fun Route.withPermission(permission: Permission, build: Route.() -> Unit): Route {
    val route = createChild(CustomSelector())
    val plugin = createRouteScopedPlugin("CustomAuthorization") {
        on(AuthenticationChecked) { call ->
            val principal = call.authentication.principal<UserIdPrincipal>()
            //  user is not authenticated
            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@on
            }

            val userId = principal.name.toInt()
            //  get permissions from role
            val permissions = AuthorizationService.getUserPermissions(userId)

            //  user is not found
            if (permissions == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@on
            }

            //  check permission
            if (!permissions.contains(permission)) {
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

object AuthorizationService {
    val getUserPermissions: (userId: Int) -> List<Permission>? = Cache.memoize(1.minutes) { userId ->
        val user = transaction {
            UserEntity.findById(userId)
        }
        //  user is not found
        if (user == null) {
            return@memoize null
        }

        //  get role
        val role = transaction {
            //  get role. if role is null, get default role
            user.role ?: RoleEntity.find {
                Roles.default eq true
            }.firstOrNull()
        }

        //  role is not found
        if (role == null) {
            return@memoize null
        }

        //  get permissions from role
        transaction {
            role.permissions.mapNotNull { Permission.getByName(it.permission) }
        }
    }
}