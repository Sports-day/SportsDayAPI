package dev.t7e.utils

/**
 * Created by testusuke on 2023/02/21
 * @author testusuke
 */
class Email(private val email: String) {

    /**
     * get domain name from email
     * @return domain
     */
    fun domain(): String? {
        return Regex("(?<=@)(.*)\$").find(email)?.value
    }

    /**
     * check if this email is able to access to API
     * @return true if it is able to
     */
    fun isAccessibleDomain(): Boolean {
        // TODO: check accessible domain with match domain to DB
        return true
    }
}
