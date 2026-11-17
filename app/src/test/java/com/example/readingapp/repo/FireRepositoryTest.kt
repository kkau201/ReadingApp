package com.example.readingapp.repo

import com.example.readingapp.testing.FakeFirestore
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
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

    @Test
    fun clearUserData_removesCachedUserAndBooks() = runTest {
        repository.fetchUser(UID)
        repository.fetchSavedBooksByUser()

        repository.clearUserData()

        assertNull(repository.user.value)
        assertTrue(repository.savedBooks.value.isEmpty())
    }
}
