package com.example.readingapp.repo

import com.example.readingapp.model.DISPLAY_NAME
import com.example.readingapp.model.USER_ID
import com.example.readingapp.testing.TestData
import com.example.readingapp.testing.TestData.UID
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FireRepositoryTest {

    private val users = mockk<CollectionReference>()
    private val userRef = mockk<DocumentReference>()
    private val repository = FireRepository(
        queryBook = mockk(relaxed = true),
        queryUser = users,
        querySession = mockk(relaxed = true),
        currentUserId = { UID }
    )

    init {
        every { users.document(UID) } returns userRef
    }

    @Test
    fun fetchUser_readsDocumentKeyedByUid() = runTest {
        every { userRef.get() } returns Tasks.forResult(userSnapshot(TestData.DISPLAY_NAME))

        repository.fetchUser(UID)

        assertEquals(TestData.DISPLAY_NAME, repository.user.value?.displayName)
        // Never scans the whole collection.
        verify(exactly = 0) { users.get() }
    }

    @Test
    fun fetchUser_noDocument_returnsNull() = runTest {
        every { userRef.get() } returns Tasks.forResult(missingSnapshot())

        repository.fetchUser(UID)

        assertNull(repository.user.value)
    }

    @Test
    fun clearUserData_removesCachedUserAndBooks() = runTest {
        every { userRef.get() } returns Tasks.forResult(userSnapshot(TestData.DISPLAY_NAME))
        repository.fetchUser(UID)

        repository.clearUserData()

        assertNull(repository.user.value)
        assertTrue(repository.savedBooks.value.isEmpty())
    }

    private fun userSnapshot(@Suppress("SameParameterValue") displayName: String) =
        mockk<DocumentSnapshot>(relaxed = true).also { doc ->
            every { doc.exists() } returns true
            every { doc.id } returns UID
            every { doc.get(USER_ID) } returns UID
            every { doc.get(DISPLAY_NAME) } returns displayName
            every { doc.contains(any<String>()) } returns false
        }

    private fun missingSnapshot() =
        mockk<DocumentSnapshot>().also { doc -> every { doc.exists() } returns false }
}
