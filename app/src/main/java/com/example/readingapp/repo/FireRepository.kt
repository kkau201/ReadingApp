package com.example.readingapp.repo

import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MSession
import com.example.readingapp.model.MUser
import com.example.readingapp.model.StoredBook
import com.example.readingapp.model.StoredSession
import com.example.readingapp.model.toModel
import com.example.readingapp.model.toSession
import com.example.readingapp.model.toStoredDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireRepository @Inject constructor(
    private val queryBook: Query,
    private val queryUser: CollectionReference,
    private val querySession: CollectionReference,
) {

    companion object {
        const val USERS_COLLECTION = "users"
        const val BOOKS_COLLECTION = "books"
        const val SESSIONS_COLLECTION = "sessions"
    }

    private val _user = MutableStateFlow<MUser?>(null)
    val user: StateFlow<MUser?>
        get() = _user

    private val _savedBooks = MutableStateFlow<List<MBook>>(emptyList())
    val savedBooks: StateFlow<List<MBook>>
        get() = _savedBooks

    /**
     * Drops everything cached for the signed-in user. This repository is a singleton, so call
     * this on sign-out or the next user to sign in on the device would briefly see this data.
     */
    fun clearUserData() {
        _user.value = null
        _savedBooks.value = emptyList()
    }

    suspend fun fetchUser(userId: String?): DataOrException<MUser?, Boolean, Exception> {
        val result = getUserDetailsFromDatabase(userId)

        _user.update { result.data }

        return result
    }

    suspend fun fetchSavedBooksByUser(): DataOrException<List<MBook>, Boolean, Exception> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: user.value?.userId
        if (userId.isNullOrBlank()) {
            _savedBooks.update { emptyList() }
            return DataOrException(data = emptyList())
        }
        return fetchSavedBooksByUser(userId)
    }

    suspend fun fetchSavedBooksByUser(userId: String): DataOrException<List<MBook>, Boolean, Exception> {
        val result = getAllBooksFromDatabase(userId)
        val savedBooks = result.data?.filter { it.userId == userId } ?: emptyList()
        _savedBooks.update { savedBooks }
        result.data = savedBooks
        return result
    }

    suspend fun getAllBooksFromDatabase(userId: String): DataOrException<List<MBook>, Boolean, Exception> {
        val dataOrException = DataOrException<List<MBook>, Boolean, Exception>()

        try {
            dataOrException.loading = true
            dataOrException.data = queryBook
                .whereEqualTo(StoredBook.USER_ID, userId)
                .get()
                .await()
                .documents
                .map { documentSnapshot ->
                    documentSnapshot.removeUnusedFields()
                    documentSnapshot.toStoredBook()
                }
            if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false
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
        val authorList = (get(StoredBook.AUTHORS) as? List<*>)?.mapNotNull { it as? String }
        return MBook(
            id = id,
            title = getString(StoredBook.TITLE),
            authors = authorList,
            imgUrl = getString(StoredBook.PHOTO),
            pageCount = getLong(StoredBook.PAGE_COUNT)?.toInt(),
            notes = getString(StoredBook.NOTES),
            rating = getDouble(StoredBook.RATING),
            startedReading = getTimestamp(StoredBook.STARTED_READING),
            finishedReading = getTimestamp(StoredBook.FINISHED_READING),
            finishedOn = getString(StoredBook.FINISHED_ON),
            userId = getString(StoredBook.USER_ID),
            googleBookId = getString(StoredBook.GOOGLE_BOOK_ID)
        )
    }

    private suspend fun getUserDetailsFromDatabase(currentUserId: String?): DataOrException<MUser?, Boolean, Exception> {
        val dataOrException = DataOrException<MUser?, Boolean, Exception>()
        try {
            dataOrException.loading = true
            // User documents are keyed by the Firebase Auth uid: users/{uid}.
            val userDocument = currentUserId
                ?.let { queryUser.document(it).get().await() }
                ?.takeIf { it.exists() }
            userDocument?.let { document ->
                val unused = listOf("profession", "profile_color").filter { document.contains(it) }
                if (unused.isNotEmpty()) {
                    document.reference.update(unused.associateWith { FieldValue.delete() })
                }
            }
            dataOrException.data = userDocument?.toModel()

            if (dataOrException.data != null) dataOrException.loading = false
        } catch (e: FirebaseFirestoreException) {
            dataOrException.e = e
        }

        return dataOrException
    }

    suspend fun fetchSessions(userId: String): List<MSession> {
        return querySession
            .whereEqualTo(StoredSession.USER_ID, userId)
            .get()
            .await()
            .documents
            .map { it.toSession() }
            .sortedByDescending { it.readAt?.seconds ?: 0L }
    }

    suspend fun addSession(session: MSession) {
        querySession.add(session.toStoredDocument()).await()
    }

    suspend fun deleteSession(sessionId: String) {
        querySession.document(sessionId).delete().await()
    }

    fun updateBook(
        bookId: String,
        bookUpdates: Map<String, Any?>,
        onCompleteListener: (Boolean) -> Unit,
        onErrorListener: (Exception) -> Unit
    ) {
        val updates = bookUpdates.mapValues { (_, value) ->
            when {
                value == null -> FieldValue.delete()
                value is String && value.isBlank() -> FieldValue.delete()
                else -> value
            }
        }
        FirebaseFirestore.getInstance()
            .collection(BOOKS_COLLECTION)
            .document(bookId)
            .update(updates)
            .addOnCompleteListener {
                onCompleteListener(it.isSuccessful)
            }
            .addOnFailureListener { onErrorListener(it) }
    }

    fun updateUser(
        userId: String,
        userUpdates: Map<String, Any?>,
        onCompleteListener: (Boolean) -> Unit,
        onErrorListener: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection(USERS_COLLECTION)
            .document(userId)
            .update(userUpdates)
            .addOnCompleteListener {
                onCompleteListener(it.isSuccessful)
            }
            .addOnFailureListener { onErrorListener(it) }
    }
}