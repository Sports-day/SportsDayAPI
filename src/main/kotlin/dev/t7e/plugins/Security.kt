package dev.t7e.plugins

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.t7e.models.MicrosoftAccountEntity
import dev.t7e.utils.Cache
import dev.t7e.utils.Email
import io.ktor.server.auth.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.net.URL
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

//  Key Provider
val azureADKeyURI =
    URL("https://login.microsoftonline.com/${System.getenv("AZURE_AD_TENANT_ID") ?: "common"}/discovery/keys")

fun Application.configureSecurity() {

    authentication {
        bearer {
            realm = "Access to the / route"
            authenticate { bearerCredential ->
                //  auth
                Authorization.authorize(bearerCredential)
            }
        }
    }
}


enum class Role(val value: String) {
    ADMIN("ADMIN"),
    USER("USER")
}

data class UserPrincipal(
    val microsoftAccount: MicrosoftAccountEntity,
    val roles: Set<Role> = emptySet()
) : Principal

object Authorization {
    private val provider = GuavaCachedJwkProvider(UrlJwkProvider(azureADKeyURI))

    val authorize: (bearerCredential: BearerTokenCredential) -> UserPrincipal? =
        Cache.memoize(5.minutes) { bearerCredential ->
            val jwt = JWT.decode(bearerCredential.token)
            val keyId = jwt.keyId
            //  cached jwk provider
            val jwk = provider.get(keyId)
            //  create public key
            val publicKey: RSAPublicKey = jwk.publicKey as RSAPublicKey
            //  validate
            val algorithm = Algorithm.RSA256(publicKey)
            try {
                algorithm.verify(jwt)

                //  check email
                val plainEmail = jwt.claims["email"]?.asString() ?: return@memoize null
                val email = Email(plainEmail)
                //  check if allowed domain
                if (!email.isAllowedDomain()) return@memoize null

                //  get microsoft user (or create user)
                val microsoftAccount = if (!MicrosoftAccountEntity.existMicrosoftAccount(email.toString())) {
                    //  create
                    transaction {
                        MicrosoftAccountEntity.new {
                            this.email = email.toString()
                            this.name = jwt.claims["name"]?.asString() ?: "Unknown"
                            this.mailAccountName = email.username()
                            this.firstLogin = LocalDateTime.now()
                            this.lastLogin = LocalDateTime.now()
                        }.apply {
                            MicrosoftAccountEntity.fetch(this.id.value)
                        }
                    }
                } else {
                    //  get
                    MicrosoftAccountEntity.getByEmail(email.toString())?.first
                }

                //  if not exist
                if (microsoftAccount == null) {
                    return@memoize null
                }

                //  result
                UserPrincipal(
                    microsoftAccount,
                    if (microsoftAccount.role == Role.ADMIN) setOf(Role.ADMIN, Role.USER)
                    else setOf(Role.USER)
                )
            } catch (e: Exception) {
                return@memoize null
            }
        }
}