package net.sportsday.services

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.sportsday.models.*
import net.sportsday.utils.Email
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.util.*

/**
 * Created by testusuke on 2024/03/25
 * @author testusuke
 */
object AuthenticationService {
    //  get info for token endpoint from environment variable
    private val tokenEndpoint = System.getenv("OIDC_TOKEN_ENDPOINT")
        ?: throw IllegalArgumentException("No open id connect token endpoint found")
    private val clientId =
        System.getenv("OIDC_CLIENT_ID") ?: throw IllegalArgumentException("No open id connect client id found")
    private val clientSecret =
        System.getenv("OIDC_CLIENT_SECRET") ?: throw IllegalArgumentException("No open id connect client secret found")
    private val redirectUrl =
        System.getenv("OIDC_REDIRECT_URL") ?: throw IllegalArgumentException("No open id connect redirect url found")
    private val jwksEndpoint = System.getenv("OIDC_JWKS_ENDPOINT")
        ?: throw IllegalArgumentException("No open id connect jwks endpoint found")
    private val userinfoEndpoint = System.getenv("OIDC_USERINFO_ENDPOINT")
        ?: throw IllegalArgumentException("No open id connect userinfo endpoint found")
    private val issuer = System.getenv("OIDC_ISSUER")
        ?: throw IllegalArgumentException("No open id connect issuer found")
    private val isSecure = System.getenv("COOKIE_SECURE")?.toBoolean() ?: false

    //  keys provider
    private val jwksProvider = GuavaCachedJwkProvider(UrlJwkProvider(URL(jwksEndpoint)))

    //  json
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getIsSecure(): Boolean = isSecure

    @OptIn(InternalAPI::class)
    private suspend fun exchangeCodeForToken(code: String, redirectURI: String): TokenResponse? {
        //  split redirect url by space
        val redirectUrlList = redirectUrl.split(" ")
        //  get redirect url that matches with parameter
        val matchedRedirectURI = redirectUrlList.firstOrNull { it == redirectURI }
            ?: return null

        //  create client
        val client = HttpClient(CIO)
        //  get token from code
        val params = Parameters.build {
            append("grant_type", "authorization_code")
            append("code", code)
            append("redirect_uri", matchedRedirectURI)
            append("client_id", clientId)
            append("client_secret", clientSecret)
        }
        val response = client.post(Url(tokenEndpoint)) {
            contentType(ContentType.Application.FormUrlEncoded)
            body = FormDataContent(params)
        }

        //  close client
        client.close()

        //  convert response to form of TokenResponse
        return Json.decodeFromString<TokenResponse>(response.body<String>())
    }

    private fun validateIdToken(idToken: String): Boolean {
        try {
            val jwt = JWT.decode(idToken)
            val keyId = jwt.keyId
            //  cached jwk provider
            val jwk = jwksProvider.get(keyId)
            //  create public key
            val publicKey: RSAPublicKey = jwk.publicKey as RSAPublicKey
            //  create algorithm
            val algorithm = Algorithm.RSA256(publicKey)
            //  create verifier
            val verifier: JWTVerifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()

            //  verify token
            verifier.verify(idToken)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private suspend fun getUserInfo(accessToken: String): UserinfoResponse {
        val client = HttpClient(CIO)

        return client.use {
            val response = it.get(userinfoEndpoint) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }

            val userInfoJson = response.body<String>()
            json.decodeFromString<UserinfoResponse>(userInfoJson)
        }
    }

    private suspend fun fetchProfilePicture(accessToken: String, imageUrl: String): String? {
        val client = HttpClient(CIO)

        return client.use {
            val response = it.get(imageUrl) {
                headers {
                    append("Authorization", "Bearer $accessToken")
                }
            }

            if (response.status != HttpStatusCode.OK) {
                return null
            }

            val bytes = response.readBytes()
            Base64.getEncoder().encodeToString(bytes)
        }
    }

    suspend fun login(code: String, redirectURI: String): User? {
        //  exchange code for token
        val tokenResponse = exchangeCodeForToken(code, redirectURI) ?: return null
        val idToken = tokenResponse.idToken
        val accessToken = tokenResponse.accessToken

        //  validation
        if (!validateIdToken(idToken)) {
            return null
        }

        //  fetch userinfo
        val userinfo = getUserInfo(accessToken)

        //  validate email
        val email = Email(userinfo.email)
        if (!email.isAllowedDomain()) {
            return null
        }

        //  fetch profile picture
        val picture = fetchProfilePicture(accessToken, userinfo.picture)

        //  find user by email
        val userModel = transaction {
            val user = UserEntity.find { Users.email eq userinfo.email }.firstOrNull()

            //  create user if not found
            if (user == null) {
                val pictureEntity = null
//                    if (picture != null) {
//                    //  create picture entity
//                    val createdPictureEntity = ImageEntity.new {
//                        this.data = picture
//                        this.createdAt = LocalDateTime.now()
//                    }
//                    createdPictureEntity
//                } else {
//                    null
//                }

                //  create user
                val createdUserEntity = UserEntity.new {
                    this.name = userinfo.name
                    this.email = userinfo.email
                    this.picture = pictureEntity
                    this.createdAt = LocalDateTime.now()
                    this.updatedAt = LocalDateTime.now()
                }
                createdUserEntity.serializableModel()
            } else {
                //  update username
                user.name = userinfo.name

                //  if there is changes in picture, update it
//                if (picture != null) {
//                    if (user.picture == null) {
//                        val createdPictureEntity = ImageEntity.new {
//                            this.data = picture
//                            this.createdAt = LocalDateTime.now()
//                        }
//                        user.picture = createdPictureEntity
//                    } else {
//                        user.picture!!.data = picture
//                    }
//                }

                //  update timestamp
                user.updatedAt = LocalDateTime.now()

                user.serializableModel()
            }
        }
        return userModel
    }

    @Serializable
    data class TokenResponse(
        @SerialName("token_type")
        val tokenType: String,
        @SerialName("scope")
        val scope: String,
        @SerialName("expires_in")
        val expiresIn: Int,
        @SerialName("ext_expires_in")
        val extExpiresIn: Int,
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("id_token")
        val idToken: String
    )

    @Serializable
    data class UserinfoResponse(
        @SerialName("name")
        val name: String,
        @SerialName("email")
        val email: String,
        @SerialName("picture")
        val picture: String
    )
}
