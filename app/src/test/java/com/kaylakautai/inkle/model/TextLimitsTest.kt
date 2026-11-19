package com.kaylakautai.inkle.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TextLimitsTest {

    @Test
    fun limitTo_leavesShortTextUnchanged() {
        val text = "A short bio"
        assertSame(text, text.limitTo(100))
    }

    @Test
    fun limitTo_cutsLongTextToMax() {
        assertEquals("abcde", "abcdefgh".limitTo(5))
    }

    @Test
    fun limitTo_neverSplitsAnEmoji() {
        // "📚" is two chars (a surrogate pair); cutting at 5 would leave half of it.
        val text = "abcd📚"
        val limited = text.limitTo(5)

        assertEquals("abcd", limited)
        assertTrue(limited.none { it.isSurrogate() })
    }

    /** Guards against changing a limit in the app or firestore.rules but not the other. */
    @Test
    fun limitsMatchFirestoreRules() {
        val rules = rulesFile().readText()

        assertTrue(rules.contains("display_name.size() <= ${TextLimits.DISPLAY_NAME}"))
        assertTrue(rules.contains("quote.size() <= ${TextLimits.QUOTE}"))
        assertTrue(rules.contains("optionalString(data, 'notes', 1, ${TextLimits.BOOK_NOTES})"))
        assertTrue(rules.contains("optionalString(data, 'note', 1, ${TextLimits.SESSION_NOTE})"))
        assertTrue(rules.contains("authors.size() <= ${StoredBook.MAX_AUTHORS}"))
        // One isAuthorAt check per allowed author (indices 0 until MAX_AUTHORS).
        assertEquals(
            StoredBook.MAX_AUTHORS,
            Regex("""isAuthorAt\(authors, \d+\)""").findAll(rules).count()
        )
    }

    // Unit tests run with the module directory (app/) as the working directory.
    private fun rulesFile(): File =
        listOf(File("../firestore.rules"), File("firestore.rules")).first { it.exists() }
}
