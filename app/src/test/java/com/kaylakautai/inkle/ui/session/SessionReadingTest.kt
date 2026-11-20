package com.kaylakautai.inkle.ui.session

import com.kaylakautai.inkle.model.StoredBook
import com.kaylakautai.inkle.model.StoredSession
import com.kaylakautai.inkle.repo.PageUpdate
import com.kaylakautai.inkle.testing.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Turning progress entries into sittings, and working out where a reader is. */
class SessionReadingTest {

    private fun valid(pages: Int, reached: Int) = ReadingEntry.Valid(pages = pages, pageReached = reached)
    private fun invalid(error: PagesError) = ReadingEntry.Invalid(error)

    // region readingEntry: pages read

    @Test
    fun pagesRead_addsToWhereTheyWere() {
        assertEquals(valid(12, 32), readingEntry(SessionLogBy.PAGES, "12", place = 20, pageCount = null))
    }

    @Test
    fun pagesRead_blankOrZero_isMissing() {
        assertEquals(invalid(PagesError.MISSING), readingEntry(SessionLogBy.PAGES, "", 20, null))
        assertEquals(invalid(PagesError.MISSING), readingEntry(SessionLogBy.PAGES, "0", 20, null))
    }

    @Test
    fun pagesRead_overTheSessionLimit_isTooMany() {
        val tooMany = (StoredSession.MAX_PAGES + 1).toString()

        assertEquals(invalid(PagesError.TOO_MANY), readingEntry(SessionLogBy.PAGES, tooMany, 0, null))
    }

    // endregion

    // region readingEntry: page reached

    @Test
    fun pageReached_countsThePagesSinceWhereTheyWere() {
        assertEquals(valid(15, 35), readingEntry(SessionLogBy.REACHED, "35", place = 20, pageCount = null))
    }

    @Test
    fun pageReached_fromTheStart_countsEveryPage() {
        assertEquals(valid(30, 30), readingEntry(SessionLogBy.REACHED, "30", place = 0, pageCount = null))
    }

    @Test
    fun pageReached_notPastWhereTheyAre_isNotAfter() {
        assertEquals(invalid(PagesError.NOT_AFTER), readingEntry(SessionLogBy.REACHED, "20", 20, null))
        assertEquals(invalid(PagesError.NOT_AFTER), readingEntry(SessionLogBy.REACHED, "5", 20, null))
    }

    @Test
    fun pageReached_pastTheHighestPage_isTooMany() {
        val tooFar = (StoredBook.MAX_PAGE + 1).toString()

        assertEquals(invalid(PagesError.TOO_MANY), readingEntry(SessionLogBy.REACHED, tooFar, 0, null))
    }

    @Test
    fun pageReached_pastTheBooksLength_isAllowed() {
        // Google Books page counts often don't match the reader's edition.
        assertEquals(valid(30, 330), readingEntry(SessionLogBy.REACHED, "330", place = 300, pageCount = 320))
    }

    // endregion

    // region readingEntry: percent

    @Test
    fun percent_isTurnedIntoAPage() {
        assertEquals(valid(80, 100), readingEntry(SessionLogBy.PERCENT, "50", place = 20, pageCount = 200))
    }

    @Test
    fun percent_rounds_andAHundredIsTheLastPage() {
        assertEquals(valid(111, 111), readingEntry(SessionLogBy.PERCENT, "33", place = 0, pageCount = 337))
        assertEquals(valid(37, 337), readingEntry(SessionLogBy.PERCENT, "100", place = 300, pageCount = 337))
    }

    @Test
    fun percent_overAHundred_isTooMany() {
        assertEquals(invalid(PagesError.TOO_MANY), readingEntry(SessionLogBy.PERCENT, "101", 0, 200))
    }

    @Test
    fun percent_notPastWhereTheyAre_isNotAfter() {
        assertEquals(invalid(PagesError.NOT_AFTER), readingEntry(SessionLogBy.PERCENT, "10", 50, 200))
    }

    @Test
    fun percent_withoutTheBooksLength_isMissing() {
        assertEquals(invalid(PagesError.MISSING), readingEntry(SessionLogBy.PERCENT, "50", 0, null))
    }

    // endregion

    // region readingPlace

    private val reading = TestData.readingBook
    private val sessions = listOf(
        TestData.session, // 20 pages of this book
        TestData.session.copy(id = "s2", pages = 5),
        TestData.session.copy(id = "other", googleBookId = "gb-other", pages = 500)
    )

    @Test
    fun readingPlace_savedPage_winsOverTheSessions() {
        assertEquals(120, reading.copy(currentPage = 120).readingPlace(sessions))
    }

    @Test
    fun readingPlace_noSavedPage_addsUpThisBooksSessions() {
        assertEquals(25, reading.readingPlace(sessions))
    }

    @Test
    fun readingPlace_bookNotStarted_isZero() {
        assertEquals(0, TestData.libraryBook.copy(currentPage = 40).readingPlace(sessions))
    }

    // endregion

    // region pageAfterRemoving

    private val onPage32 = reading.copy(currentPage = 32)

    @Test
    fun removingTheLatestSitting_goesBackToWhereItBegan() {
        val latest = TestData.session.copy(pages = 12, endPage = 32)

        assertEquals(PageUpdate(reading.id!!, 20), onPage32.pageAfterRemoving(latest))
    }

    @Test
    fun removingTheOnlySitting_clearsTheSavedPage() {
        val only = TestData.session.copy(pages = 32, endPage = 32)

        assertEquals(PageUpdate(reading.id!!, null), onPage32.pageAfterRemoving(only))
    }

    @Test
    fun removingAnOlderSitting_changesNothing() {
        assertNull(onPage32.pageAfterRemoving(TestData.session.copy(pages = 20, endPage = 20)))
    }

    @Test
    fun removingASittingWithNoEndPage_changesNothing() {
        assertNull(onPage32.pageAfterRemoving(TestData.session))
    }

    // endregion
}
