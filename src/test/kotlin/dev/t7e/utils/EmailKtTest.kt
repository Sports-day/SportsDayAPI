package dev.t7e.utils

import org.junit.Test

/**
 * Created by testusuke on 2023/02/21
 * @author testusuke
 */
class EmailKtTest {
    @Test
    fun getDomain() {
        val email = Email("a1234567@super.kosen-ac.jp")
        println(email.domain())
    }

    @Test
    fun getUsername() {
        val email = Email("a1234567@super.kosen-ac.jp")
        println(email.username())
    }
}