package dev.t7e.plugins

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.t7e.utils.Email
import io.ktor.server.auth.*
import io.ktor.server.application.*
import java.lang.Exception
import java.net.URL
import java.security.interfaces.RSAPublicKey

//  Key Provider
val azureADKeyURI = URL("https://login.microsoftonline.com/${System.getenv("AZURE_AD_TENANT_ID") ?: "common"}/discovery/keys")

fun Application.configureSecurity() {

    authentication {
        bearer("azure-ad") {
            realm = "Access to the / route"
            authenticate {bearerCredential ->
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

                    println("allowed")

                    null
                } catch (e: Exception) {
                    println("Failed to validate token")

                    return@authenticate null
                }
            }
        }
    }
}