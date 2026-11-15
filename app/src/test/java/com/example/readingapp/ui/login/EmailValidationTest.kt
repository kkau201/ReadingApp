package com.example.readingapp.ui.login

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidationTest {

    @Test
    fun acceptsCommonAddresses() {
        listOf(
            "jane@gmail.com",
            "jane.austen@gmail.com",
            "jane.austen+books@gmail.com",
            "j_austen-1@mail.example.co.nz",
            "JANE@EXAMPLE.COM"
        ).forEach { assertTrue(it, isValidEmail(it)) }
    }

    @Test
    fun ignoresSurroundingWhitespace() {
        assertTrue(isValidEmail("jane@gmail.com "))
        assertTrue(isValidEmail("  jane@gmail.com\n"))
    }

    @Test
    fun rejectsMalformedAddresses() {
        listOf(
            "",
            "   ",
            "jane",
            "jane@",
            "@gmail.com",
            "jane@gmail",
            "jane@@gmail.com",
            "jane austen@gmail.com",
            "jane@gmail..com"
        ).forEach { assertFalse("'$it' should be invalid", isValidEmail(it)) }
    }
}
