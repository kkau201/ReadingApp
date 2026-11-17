package com.example.readingapp.testing

import com.example.readingapp.model.AVATAR_URL
import com.example.readingapp.model.DISPLAY_NAME
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MUser
import com.example.readingapp.model.QUOTE
import com.example.readingapp.model.READING_GOAL
import com.example.readingapp.model.StoredBook
import com.example.readingapp.model.USER_ID
import com.example.readingapp.repo.FireRepository
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk

/**
 * Mocked Firestore collections behind a real [FireRepository], so tests also cover how
 * documents are read and mapped. Change the properties to change what Firestore "holds".
 */
class FakeFirestore(currentUserId: () -> String? = { TestData.UID }) {

    /** The users/{uid} document; null means it doesn't exist. */
    var profile: MUser? = TestData.profile

    /** Every book document for the user. */
    var books: List<MBook> = TestData.books

    /** When set, reading users fails with it. */
    var userError: FirebaseFirestoreException? = null

    /** When set, reading books fails with it. */
    var booksError: FirebaseFirestoreException? = null

    /** Every users/{uid} update written, in order. Successful ones are applied to [profile]. */
    val userUpdates = mutableListOf<Map<String, Any?>>()

    /** When set, updating users fails with it. */
    var userWriteError: FirebaseFirestoreException? = null

    /** When set, user updates stay in progress until it's completed, to test "while saving". */
    var pendingUserWrite: TaskCompletionSource<Void>? = null

    val users = mockk<CollectionReference>()
    private val bookQuery = mockk<Query>()

    val repository = FireRepository(
        queryBook = bookQuery,
        queryUser = users,
        querySession = mockk(relaxed = true),
        currentUserId = currentUserId
    )

    init {
        every { users.document(any()) } answers {
            val uid = firstArg<String>()
            mockk<DocumentReference>().also { ref ->
                every { ref.get() } answers {
                    userError?.let { Tasks.forException(it) }
                        ?: Tasks.forResult(userSnapshot(uid, profile?.takeIf { it.userId == uid }))
                }
                every { ref.update(any<Map<String, Any>>()) } answers {
                    val updates = firstArg<Map<String, Any?>>()
                    userUpdates += updates
                    userWriteError?.let { return@answers Tasks.forException<Void>(it) }
                    profile = profile?.withUpdates(updates)
                    pendingUserWrite?.task ?: Tasks.forResult<Void>(null)
                }
            }
        }
        every { bookQuery.whereEqualTo(StoredBook.USER_ID, any()) } answers {
            val uid = secondArg<String>()
            mockk<Query>().also { query ->
                every { query.get() } answers {
                    booksError?.let { Tasks.forException(it) } ?: Tasks.forResult(
                        mockk<QuerySnapshot>().also { snapshot ->
                            every { snapshot.documents } returns
                                books.filter { it.userId == uid }.map(::bookSnapshot)
                        }
                    )
                }
            }
        }
    }

    companion object {
        private fun MUser.withUpdates(updates: Map<String, Any?>) = copy(
            displayName = updates[DISPLAY_NAME] as? String ?: displayName,
            quote = updates[QUOTE] as? String ?: quote,
            avatarUrl = updates[AVATAR_URL] as? String ?: avatarUrl,
            readingGoal = (updates[READING_GOAL] as? Number)?.toInt() ?: readingGoal
        )

        fun unavailable(message: String = "Firestore unavailable") =
            FirebaseFirestoreException(message, FirebaseFirestoreException.Code.UNAVAILABLE)

        private fun userSnapshot(uid: String, user: MUser?): DocumentSnapshot =
            mockk<DocumentSnapshot>().also { doc ->
                every { doc.exists() } returns (user != null)
                if (user == null) return@also
                every { doc.id } returns uid
                every { doc.contains(any<String>()) } returns false
                every { doc.get(USER_ID) } returns user.userId
                every { doc.get(DISPLAY_NAME) } returns user.displayName
                every { doc.get(AVATAR_URL) } returns user.avatarUrl
                every { doc.get(QUOTE) } returns user.quote
                every { doc.get(READING_GOAL) } returns user.readingGoal.toLong()
            }

        private fun bookSnapshot(book: MBook): DocumentSnapshot =
            mockk<DocumentSnapshot>().also { doc ->
                every { doc.id } returns book.id!!
                every { doc.data } returns null
                every { doc.get(StoredBook.AUTHORS) } returns book.authors
                every { doc.getString(StoredBook.TITLE) } returns book.title
                every { doc.getString(StoredBook.NOTES) } returns book.notes
                every { doc.getString(StoredBook.FINISHED_ON) } returns book.finishedOn
                every { doc.getString(StoredBook.USER_ID) } returns book.userId
                every { doc.getString(StoredBook.GOOGLE_BOOK_ID) } returns book.googleBookId
                every { doc.getLong(StoredBook.PAGE_COUNT) } returns book.pageCount?.toLong()
                every { doc.getDouble(StoredBook.RATING) } returns book.rating
                every { doc.getTimestamp(StoredBook.STARTED_READING) } returns book.startedReading
                every { doc.getTimestamp(StoredBook.FINISHED_READING) } returns book.finishedReading
            }
    }
}
