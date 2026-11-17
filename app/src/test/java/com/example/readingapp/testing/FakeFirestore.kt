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
import com.example.readingapp.repo.FireRepository.Companion.BOOKS_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic

/**
 * Mocked Firestore collections behind a real [FireRepository], so tests also cover how
 * documents are read and mapped. Change the properties to change what Firestore "holds".
 *
 * Writes are applied to [profile] and [books], so screens see them on their next read.
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

    /** Book documents added through [interceptDirectBookWrites], in order. */
    val addedBooks = mutableListOf<Map<String, Any>>()

    /** Ids of book documents deleted through [interceptDirectBookWrites], in order. */
    val deletedBookIds = mutableListOf<String>()

    /** Every (bookId, fields) sent to [FireRepository.updateBook], in order. */
    val bookUpdates = mutableListOf<Pair<String, Map<String, Any?>>>()

    /** When set, [FireRepository.updateBook] fails with it. */
    var bookUpdateError: Exception? = null

    /** Book ids whose delete fails, with the error to fail with. */
    val bookDeleteErrors = mutableMapOf<String, Exception>()

    /** When set, adding a book fails with it. */
    var bookAddError: Exception? = null

    val users = mockk<CollectionReference>()
    private val bookQuery = mockk<Query>()

    /**
     * The repository under test. A spy only so [FireRepository.updateBook], which writes via
     * FirebaseFirestore.getInstance(), lands in [books] instead; everything else is real.
     */
    val repository: FireRepository = spyk(
        FireRepository(
            queryBook = bookQuery,
            queryUser = users,
            querySession = mockk(relaxed = true),
            currentUserId = currentUserId
        )
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
        every { repository.updateBook(any(), any(), any(), any()) } answers {
            val id = firstArg<String>()
            val updates = secondArg<Map<String, Any?>>()
            bookUpdates += id to updates
            val error = bookUpdateError
            if (error == null) {
                books = books.map { if (it.id == id) it.withUpdates(updates) else it }
                thirdArg<(Boolean) -> Unit>().invoke(true)
            } else {
                // Like Firebase: complete (unsuccessfully), then fail.
                thirdArg<(Boolean) -> Unit>().invoke(false)
                arg<(Exception) -> Unit>(3).invoke(error)
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

    /**
     * For code that writes books via FirebaseFirestore.getInstance() rather than the repository:
     * routes those adds and deletes here. Undo with [releaseDirectWrites].
     */
    fun interceptDirectBookWrites() {
        val collection = mockk<CollectionReference>()
        every { collection.add(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            val doc = firstArg<Any>() as Map<String, Any>
            addedBooks += doc
            bookAddError?.let { return@answers immediateTask<DocumentReference>(error = it) }
            books = books + doc.toBook(id = "added-${addedBooks.size}")
            immediateTask(result = mockk<DocumentReference>())
        }
        every { collection.document(any()) } answers {
            val docId = firstArg<String>()
            val ref = mockk<DocumentReference>()
            every { ref.delete() } answers { deleteBook(docId) }
            ref
        }
        val db = mockk<FirebaseFirestore>()
        every { db.collection(BOOKS_COLLECTION) } returns collection
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns db
    }

    private fun deleteBook(docId: String): Task<Void> {
        deletedBookIds += docId
        bookDeleteErrors[docId]?.let { return immediateTask(error = it) }
        books = books.filterNot { it.id == docId }
        return immediateTask()
    }

    companion object {
        fun releaseDirectWrites() = unmockkStatic(FirebaseFirestore::class)

        @Suppress("UNCHECKED_CAST")
        private fun Map<String, Any>.toBook(id: String) = MBook(
            id = id,
            title = this[StoredBook.TITLE] as String?,
            authors = this[StoredBook.AUTHORS] as List<String>?,
            pageCount = this[StoredBook.PAGE_COUNT] as Int?,
            userId = this[StoredBook.USER_ID] as String?,
            googleBookId = this[StoredBook.GOOGLE_BOOK_ID] as String?,
            startedReading = this[StoredBook.STARTED_READING] as Timestamp?,
            finishedReading = this[StoredBook.FINISHED_READING] as Timestamp?,
            finishedOn = this[StoredBook.FINISHED_ON] as String?
        )

        private fun MBook.withUpdates(updates: Map<String, Any?>) = copy(
            startedReading = updates[StoredBook.STARTED_READING] as Timestamp?,
            finishedReading = updates[StoredBook.FINISHED_READING] as Timestamp?,
            finishedOn = updates[StoredBook.FINISHED_ON] as String?
        )

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
