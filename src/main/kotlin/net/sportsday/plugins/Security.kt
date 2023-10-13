package net.sportsday.plugins

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import net.sportsday.models.LogEvents
import net.sportsday.models.MicrosoftAccountEntity
import net.sportsday.models.UserEntity
import net.sportsday.utils.Cache
import net.sportsday.utils.Email
import net.sportsday.utils.logger.Logger
import org.jetbrains.exposed.exceptions.ExposedSQLException
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
    USER("USER"),
}

data class UserPrincipal(
    val microsoftAccountId: Int,
    val microsoftAccount: MicrosoftAccountEntity,
    val roles: Set<Role> = emptySet(),
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
                    val pair = transaction {
                        val entity = MicrosoftAccountEntity.new {
                            this.email = email.toString()
                            this.name = jwt.claims["name"]?.asString() ?: "Unknown"
                            this.mailAccountName = email.username()
                            this.firstLogin = LocalDateTime.now()
                            this.lastLogin = LocalDateTime.now()
                        }

                        entity to entity.serializableModel()
                    }.apply {
                        MicrosoftAccountEntity.fetch(this.second.id)
                    }

                    pair.first
                } else {
                    //  last login update
                    MicrosoftAccountEntity.getByEmail(email.toString())?.first?.let { entity ->
                        transaction {
                            entity.lastLogin = LocalDateTime.now()
                        }
                        return@let entity
                    }
                }

                //  if not exist
                if (microsoftAccount == null) {
                    return@memoize null
                }

                // //////////////////////////
                //  link user
                // //////////////////////////
                var isChanged = false
                transaction {
                    if (microsoftAccount.user == null) {
                        //  find user
                        val emailAccountName = microsoftAccount.mailAccountName

                        if (emailAccountName != null) {
                            val user = UserEntity.getAll().firstOrNull {
                                emailAccountName.contains(it.second.studentId)
                            }

                            if (user != null) {
                                transaction {
                                    microsoftAccount.user = user.first
                                }.apply {
                                    MicrosoftAccountEntity.fetch(microsoftAccount.id.value)
                                }

                                isChanged = true
                            }
                        }
                    }
                }

                //  re-fetch
                val result = if (isChanged) {
                    MicrosoftAccountEntity.getById(microsoftAccount.id.value)?.first ?: return@memoize null
                } else {
                    microsoftAccount
                }

                //  result
                UserPrincipal(
                    result.id.value,
                    result,
                    if (result.role == Role.ADMIN) {
                        setOf(Role.ADMIN, Role.USER)
                    } else {
                        setOf(Role.USER)
                    },
                )
            } catch (e: ExposedSQLException) {
                Logger.commit(
                    "Authorization failed: ${e.message}",
                    LogEvents.ERROR,
                    null,
                )

                return@memoize null
            } catch (e: Exception) {
                return@memoize null
            }
        }
}
