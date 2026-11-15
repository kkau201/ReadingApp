package com.example.readingapp.repo

import com.example.readingapp.model.DISPLAY_NAME
import com.example.readingapp.model.USER_ID
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FireRepositoryTest {

    private val userDocument = mockk<DocumentSnapshot>(relaxed = true).also { doc ->
        every { doc.id } returns "doc-1"
        every { doc.get(USER_ID) } returns "uid-1"
        every { doc.get(DISPLAY_NAME) } returns "Jane Austen"
        every { doc.contains(any<String>()) } returns false
    }
    private val queryUser = mockk<Query> {
        every { get() } returns Tasks.forResult(
            mockk<QuerySnapshot> { every { documents } returns listOf(userDocument) }
        )
    }
    private val repository = FireRepository(
        queryBook = mockk(relaxed = true),
        queryUser = queryUser,
        querySession = mockk<CollectionReference>(relaxed = true)
    )

    @Test
    fun clearUserData_removesCachedUserAndBooks() = runTest {
        repository.fetchUser("uid-1")
        assertEquals("Jane Austen", repository.user.value?.displayName)

        repository.clearUserData()

        assertNull(repository.user.value)
        assertTrue(repository.savedBooks.value.isEmpty())
    }
}
