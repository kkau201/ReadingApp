package com.kaylakautai.inkle.repo

import com.kaylakautai.inkle.testing.FakeFirestore
import com.kaylakautai.inkle.testing.TestData
import com.kaylakautai.inkle.testing.TestData.UID
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FireRepositoryTest {

    private val firestore = FakeFirestore()
    private val repository = firestore.repository

    @Test
    fun fetchUser_readsDocumentKeyedByUid() = runTest {
        repository.fetchUser(UID)

        assertEquals(TestData.DISPLAY_NAME, repository.user.value?.displayName)
        verify { firestore.users.document(UID) }
        // Never scans the whole collection.
        verify(exactly = 0) { firestore.users.get() }
    }

    @Test
    fun fetchUser_noDocument_returnsNull() = runTest {
        firestore.profile = null

        repository.fetchUser(UID)

        assertNull(repository.user.value)
    }

    @Test
    fun fetchUser_documentDeletedSinceLastRead_clearsCachedProfile() = runTest {
        repository.fetchUser(UID)
        firestore.profile = null

        repository.fetchUser(UID)

        assertNull(repository.user.value)
    }

    @Test
    fun fetchUser_readFails_keepsLastProfileAndReportsError() = runTest {
        repository.fetchUser(UID)
        firestore.userError = FakeFirestore.unavailable()

        val result = repository.fetchUser(UID)

        assertNotNull(result.e)
        assertEquals(TestData.DISPLAY_NAME, repository.user.value?.displayName)
    }

    @Test
    fun fetchSavedBooks_mapsDocumentsForSignedInUser() = runTest {
        val saved = repository.fetchSavedBooksByUser().getOrThrow()

        assertEquals(TestData.books, saved)
        assertEquals(TestData.books, repository.savedBooks.value)
    }

    // region Listening

    @Test
    fun startListening_documentsChange_updatesUserAndBooks() {
        repository.startListening(UID)
        firestore.profile = TestData.profile.copy(displayName = TestData.EDITED_DISPLAY_NAME)
        firestore.books = listOf(TestData.readingBook)

        firestore.pushChanges()

        assertEquals(TestData.EDITED_DISPLAY_NAME, repository.user.value?.displayName)
        assertEquals(listOf(TestData.readingBook), repository.savedBooks.value)
    }

    @Test
    fun startListening_onlineCacheSnapshot_isIgnoredUntilServerAnswers() {
        repository.startListening(UID)

        firestore.pushChanges(fromCache = true)

        assertNull(repository.user.value)
        assertTrue(repository.savedBooks.value.isEmpty())
    }

    @Test
    fun startListening_offline_usesCacheSnapshot() {
        firestore.online = false
        repository.startListening(UID)

        firestore.pushChanges(fromCache = true)

        assertEquals(TestData.DISPLAY_NAME, repository.user.value?.displayName)
        assertEquals(TestData.books, repository.savedBooks.value)
    }

    @Test
    fun startListening_profileMissingOnlyInCache_keepsProfile() = runTest {
        firestore.online = false
        repository.fetchUser(UID)
        repository.startListening(UID)
        firestore.profile = null

        firestore.pushChanges(fromCache = true)

        assertEquals(TestData.DISPLAY_NAME, repository.user.value?.displayName)
    }

    @Test
    fun startListening_profileDeletedOnServer_clearsProfile() = runTest {
        repository.fetchUser(UID)
        repository.startListening(UID)
        firestore.profile = null

        firestore.pushChanges()

        assertNull(repository.user.value)
    }

    @Test
    fun startListening_twiceForSameUser_attachesOnce() {
        repository.startListening(UID)
        repository.startListening(UID)

        assertEquals(2, firestore.activeListeners)
    }

    @Test
    fun startListening_afterListenersFail_attachesAgain() {
        repository.startListening(UID)
        firestore.failListeners()

        repository.startListening(UID)

        assertEquals(2, firestore.activeListeners)
    }

    @Test
    fun clearUserData_stopsListening() {
        repository.startListening(UID)

        repository.clearUserData()
        firestore.pushChanges()

        assertEquals(0, firestore.activeListeners)
        assertNull(repository.user.value)
        assertTrue(repository.savedBooks.value.isEmpty())
    }

    // endregion

    @Test
    fun clearUserData_removesCachedUserAndBooks() = runTest {
        repository.fetchUser(UID)
        repository.fetchSavedBooksByUser()

        repository.clearUserData()

        assertNull(repository.user.value)
        assertTrue(repository.savedBooks.value.isEmpty())
    }
}
