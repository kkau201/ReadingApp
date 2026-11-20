package com.kaylakautai.inkle.repo

import android.util.Log
import com.kaylakautai.inkle.data.DataOrException
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.MSession
import com.kaylakautai.inkle.model.MUser
import com.kaylakautai.inkle.model.StoredBook
import com.kaylakautai.inkle.model.StoredSession
import com.kaylakautai.inkle.model.toModel
import com.kaylakautai.inkle.model.toSession
import com.kaylakautai.inkle.model.toStoredDocument
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Where a book's reader has got to, saved alongside a session change. A null [page] removes it. */
data class PageUpdate(val bookId: String, val page: Int?)

class FireRepository @Inject constructor(
    private val queryBook: Query,
    private val queryUser: CollectionReference,
    private val querySession: CollectionReference,
    /** The signed-in user's uid (from AuthRepository), or null. */
    private val currentUserId: () -> String?,
    /** Whether the device has a connection right now. */
    private val hasConnection: () -> Boolean = { true },
) {

    fun isOnline(): Boolean = hasConnection()

    /**
     * Offline, read straight from the device cache. The default source would first wait
     * several seconds for the server before falling back to it.
     */
    private fun readSource(): Source = if (isOnline()) Source.DEFAULT else Source.CACHE

    companion object {
        const val USERS_COLLECTION = "users"
        const val BOOKS_COLLECTION = "books"
        const val SESSIONS_COLLECTION = "sessions"
        private const val TAG = "FireRepository"
    }

    val user: StateFlow<MUser?>
        field = MutableStateFlow<MUser?>(null)

    val savedBooks: StateFlow<List<MBook>>
        field = MutableStateFlow<List<MBook>>(emptyList())

    /** The user's reading sessions, newest first. */
    val sessions: StateFlow<List<MSession>>
        field = MutableStateFlow<List<MSession>>(emptyList())

    private var listeningFor: String? = null
    private val listeners = mutableListOf<ListenerRegistration>()

    /**
     * Keeps [user], [savedBooks] and [sessions] up to date as the documents change, including
     * from another device, until [clearUserData]. Calling it again for the same user does nothing.
     */
    fun startListening(userId: String) {
        if (listeningFor == userId) return
        stopListening()
        listeningFor = userId
        listeners += queryUser.document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) onListenerFailed("User", e)
            if (snapshot == null || snapshot.metadata.isIncomplete()) return@addSnapshotListener
            // Only a confirmed missing document clears the profile, as in fetchUser.
            if (!snapshot.exists() && snapshot.metadata.isFromCache) return@addSnapshotListener
            user.value = snapshot.takeIf { it.exists() }?.toModel()
        }
        listeners += queryBook.whereEqualTo(StoredBook.USER_ID, userId).addSnapshotListener { snapshot, e ->
            if (e != null) onListenerFailed("Books", e)
            if (snapshot == null || snapshot.metadata.isIncomplete()) return@addSnapshotListener
            savedBooks.value = snapshot.documents.map { it.toStoredBook() }
        }
        listeners += querySession.whereEqualTo(StoredSession.USER_ID, userId).addSnapshotListener { snapshot, e ->
            if (e != null) onListenerFailed("Sessions", e)
            if (snapshot == null || snapshot.metadata.isIncomplete()) return@addSnapshotListener
            sessions.value = snapshot.documents.map { it.toSession() }.newestFirst()
        }
    }

    /**
     * Online, a snapshot served from the cache before the server answers may hold only the
     * documents this device happened to cache, so wait for the server's. Offline, the cache
     * is all there is.
     */
    private fun SnapshotMetadata.isIncomplete(): Boolean = isFromCache && isOnline()

    /** Firestore stops a listener after an error, so the next [startListening] attaches afresh. */
    private fun onListenerFailed(what: String, e: Exception) {
        Log.w(TAG, "$what listener failed", e)
        listeningFor = null
    }

    private fun stopListening() {
        listeners.forEach { it.remove() }
        listeners.clear()
        listeningFor = null
    }

    /**
     * Drops everything cached for the signed-in user. This repository is a singleton, so call
     * this on sign-out or the next user to sign in on the device would briefly see this data.
     */
    fun clearUserData() {
        stopListening()
        user.value = null
        savedBooks.value = emptyList()
        sessions.value = emptyList()
    }

    suspend fun fetchUser(userId: String?): DataOrException<MUser?, Exception> {
        val result = getUserDetailsFromDatabase(userId)

        // A failed read says nothing about the profile, so keep the last one rather than
        // blanking every screen showing it. A missing document (no error) does clear it.
        if (result.e == null) user.update { result.data }

        return result
    }

    suspend fun fetchSavedBooksByUser(): DataOrException<List<MBook>, Exception> {
        val userId = currentUserId() ?: user.value?.userId
        if (userId.isNullOrBlank()) {
            savedBooks.update { emptyList() }
            return DataOrException(data = emptyList())
        }
        return fetchSavedBooksByUser(userId)
    }

    suspend fun fetchSavedBooksByUser(userId: String): DataOrException<List<MBook>, Exception> {
        val result = getAllBooksFromDatabase(userId)
        val savedBooks = result.data?.filter { it.userId == userId } ?: emptyList()
        this.savedBooks.update { savedBooks }
        result.data = savedBooks
        return result
    }

    suspend fun getAllBooksFromDatabase(userId: String): DataOrException<List<MBook>, Exception> {
        val dataOrException = DataOrException<List<MBook>, Exception>()

        try {
            dataOrException.data = queryBook
                .whereEqualTo(StoredBook.USER_ID, userId)
                .get(readSource())
                .await()
                .documents
                .map { documentSnapshot ->
                    documentSnapshot.removeUnusedFields()
                    documentSnapshot.toStoredBook()
                }
        } catch (e: FirebaseFirestoreException) {
            dataOrException.e = e
        }

        return dataOrException
    }

    private fun DocumentSnapshot.removeUnusedFields() {
        val extra = data?.keys?.filter { it !in StoredBook.fields }.orEmpty()
        if (extra.isEmpty()) return
        reference.update(extra.associateWith { FieldValue.delete() })
    }

    private fun DocumentSnapshot.toStoredBook(): MBook {
        val authorList = (get(StoredBook.AUTHORS) as? List<*>)?.filterIsInstance<String>()
        return MBook(
            id = id,
            title = getString(StoredBook.TITLE),
            authors = authorList,
            pageCount = getLong(StoredBook.PAGE_COUNT)?.toInt(),
            currentPage = getLong(StoredBook.CURRENT_PAGE)?.toInt(),
            notes = getString(StoredBook.NOTES),
            rating = getDouble(StoredBook.RATING),
            startedReading = getTimestamp(StoredBook.STARTED_READING),
            finishedReading = getTimestamp(StoredBook.FINISHED_READING),
            finishedOn = getString(StoredBook.FINISHED_ON),
            userId = getString(StoredBook.USER_ID),
            googleBookId = getString(StoredBook.GOOGLE_BOOK_ID)
        )
    }

    private suspend fun getUserDetailsFromDatabase(currentUserId: String?): DataOrException<MUser?, Exception> {
        val dataOrException = DataOrException<MUser?, Exception>()
        try {
            // User documents are keyed by the Firebase Auth uid: users/{uid}.
            val userDocument = currentUserId
                ?.let { queryUser.document(it).get(readSource()).await() }
                ?.takeIf { it.exists() }
            userDocument?.let { document ->
                val unused = listOf("profession", "profile_color").filter { document.contains(it) }
                if (unused.isNotEmpty()) {
                    document.reference.update(unused.associateWith { FieldValue.delete() })
                }
            }
            dataOrException.data = userDocument?.toModel()
        } catch (e: FirebaseFirestoreException) {
            dataOrException.e = e
        }

        return dataOrException
    }

    suspend fun fetchSessions(userId: String): List<MSession> {
        val fetched = querySession
            .whereEqualTo(StoredSession.USER_ID, userId)
            .get(readSource())
            .await()
            .documents
            .map { it.toSession() }
            .newestFirst()
        sessions.value = fetched
        return fetched
    }

    private fun List<MSession>.newestFirst() = sortedByDescending { it.readAt?.seconds ?: 0L }

    /**
     * Adds [session] and, in the same batch, moves its book to [page] (see [PageUpdate]). Both
     * are saved or neither is, so the journal and the book's progress can't disagree.
     * Throws if the server rejects it.
     */
    suspend fun logSession(session: MSession, page: PageUpdate?): WriteResult {
        val batch = querySession.firestore.batch()
        batch.set(querySession.document(), session.toStoredDocument())
        page?.let { batch.setPage(it) }
        return batch.commit().awaitWrite(isOnline())
    }

    /**
     * Deletes the session and, in the same batch, moves its book back to [page] if given.
     * Throws if the server rejects it.
     */
    suspend fun deleteSession(sessionId: String, page: PageUpdate? = null): WriteResult {
        val batch = querySession.firestore.batch()
        batch.delete(querySession.document(sessionId))
        page?.let { batch.setPage(it) }
        return batch.commit().awaitWrite(isOnline())
    }

    private fun WriteBatch.setPage(change: PageUpdate) {
        val book = querySession.firestore.collection(BOOKS_COLLECTION).document(change.bookId)
        // Optional fields are removed rather than stored empty (firestore.rules).
        update(book, StoredBook.CURRENT_PAGE, change.page?.takeIf { it > 0 } ?: FieldValue.delete())
    }

    /** Exactly one of [onSaved] or [onError] is called. */
    fun updateBook(
        bookId: String,
        bookUpdates: Map<String, Any?>,
        onSaved: (WriteResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val updates = bookUpdates.mapValues { (_, value) ->
            when (value) {
                null -> FieldValue.delete()
                is String if value.isBlank() -> FieldValue.delete()
                else -> value
            }
        }
        FirebaseFirestore.getInstance()
            .collection(BOOKS_COLLECTION)
            .document(bookId)
            .update(updates)
            .onWriteDone(isOnline(), onSaved, onError)
    }

    /**
     * Updates fields of users/{userId}; a null value removes the field (as optional fields
     * must be absent rather than null). Throws if the server rejects it.
     */
    suspend fun updateUser(userId: String, userUpdates: Map<String, Any?>): WriteResult {
        val updates = userUpdates.mapValues { (_, value) -> value ?: FieldValue.delete() }
        return queryUser.document(userId).update(updates).awaitWrite(isOnline())
    }
}