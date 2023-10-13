package net.sportsday.utils

import net.sportsday.models.AllowedDomainEntity

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
    fun isAllowedDomain(): Boolean {
        val allowedDomain = this.domain()?.let {
            AllowedDomainEntity.getByDomain(it)
        }

        return allowedDomain != null
    }

    fun username(): String? {
        return Regex("([^@]+)").find(email)?.value
    }

    /**
     * get plain email
     * @return email [String]
     */
    override fun toString(): String {
        return email
    }
}
