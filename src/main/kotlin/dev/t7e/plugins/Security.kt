package dev.t7e.plugins

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.t7e.models.AdminUser
import dev.t7e.models.MicrosoftAccount
import dev.t7e.utils.Email
import io.ktor.server.auth.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.net.URL
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime

//  Key Provider
val azureADKeyURI =
    URL("https://login.microsoftonline.com/${System.getenv("AZURE_AD_TENANT_ID") ?: "common"}/discovery/keys")

//  cache
val cache = mutableMapOf<String, UserPrincipal?>()

fun Application.configureSecurity() {

    authentication {
        bearer("azure-ad") {
            realm = "Access to the / route"
            authenticate { bearerCredential ->
                //  cache
                if (cache.containsKey(bearerCredential.token)) {
                    println("Auth: use cache.")

                    return@authenticate cache[bearerCredential.token]
                }

                val jwt = JWT.decode(bearerCredential.token)
                val keyId = jwt.keyId
                //  cached jwk provider
                val provider = GuavaCachedJwkProvider(UrlJwkProvider(azureADKeyURI))
                val jwk = provider.get(keyId)
                //  create public key
                val publicKey: RSAPublicKey = jwk.publicKey as RSAPublicKey
                //  validate
                val algorithm = Algorithm.RSA256(publicKey)
                try {
                    algorithm.verify(jwt)

                    jwt.claims.forEach { (key, value) ->
                        println("$key: $value")
                    }

                    //  check email
                    val plainEmail = jwt.claims["email"]?.asString() ?: return@authenticate null
                    val email = Email(plainEmail)
                    //  check if allowed domain
                    if (!email.isAllowedDomain()) return@authenticate null

                    //  get microsoft user (or create user)
                    val microsoftAccount = if (!MicrosoftAccount.existMicrosoftAccount(email.toString())) {
                        //  create
                        transaction {
                            MicrosoftAccount.new {
                                this.email = email.toString()
                                this.name = jwt.claims["name"]?.asString() ?: "Unknown"
                                this.mailAccountName = email.username()
                                this.firstLogin = LocalDateTime.now()
                                this.lastLogin = LocalDateTime.now()
                            }
                        }
                    } else {
                        //  get
                        MicrosoftAccount.getMicrosoftAccount(email.toString())
                    }

                    //  if not exist
                    if (microsoftAccount == null) {
                        return@authenticate null
                    }

                    println("Authorized: ${microsoftAccount.name}")

                    //  cache
                    cache[bearerCredential.token] = UserPrincipal(
                        microsoftAccount,
                        if (AdminUser.isAdminUserByEmail(email.toString())) setOf(Role.ADMIN) else setOf(Role.USER)
                    )
                    cache[bearerCredential.token]
                } catch (e: Exception) {
                    println("Failed to validate token.")
                    e.printStackTrace()

                    return@authenticate null
                }
            }
        }
    }
}


enum class Role(val value: String) {
    ADMIN("ADMIN"),
    USER("USER")
}

data class UserPrincipal(
    val microsoftAccount: MicrosoftAccount,
    val roles: Set<Role> = emptySet()
) : Principal