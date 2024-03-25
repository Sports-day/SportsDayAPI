package net.sportsday.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

/**
 * Created by testusuke on 2024/03/25
 * @author testusuke
 */
object JwtConfig {

    private val secret = System.getenv("JWT_SECRET") ?: throw IllegalArgumentException("No JWT secret found")
    private val issuer = System.getenv("JWT_ISSUER") ?: throw IllegalArgumentException("No JWT issuer found")
    private val validityInMinute = System.getenv("JWT_EXPIRE_MINUTE")?.toLong() ?: throw IllegalArgumentException("No JWT expiration time found")
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Produce a token for this combination of User and Account
     */
    fun makeToken(userId: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", userId)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + (validityInMinute * 60_000))

}