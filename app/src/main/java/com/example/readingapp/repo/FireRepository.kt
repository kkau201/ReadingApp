package com.example.readingapp.repo

import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MUser
import com.example.readingapp.model.StoredBook
import com.example.readingapp.model.toModel
import com.google.firebase.auth.FirebaseAuth
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
    private val queryUser: Query,
) {

    companion object {
        const val USER_ID = "user_id"
        const val USERS_COLLECTION = "users"
        const val BOOKS_COLLECTION = "books"
    }

    private val _user = MutableStateFlow<MUser?>(null)
    val user: StateFlow<MUser?>
        get() = _user

    private val _savedBooks = MutableStateFlow<List<MBook>>(emptyList())
    val savedBooks: StateFlow<List<MBook>>
        get() = _savedBooks

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
            dataOrException.data = queryUser.get().await().documents
                .find { it.get(USER_ID)?.equals(currentUserId) == true }
                ?.toModel()

            if (dataOrException.data != null) dataOrException.loading = false
        } catch (e: FirebaseFirestoreException) {
            dataOrException.e = e
        }

        return dataOrException
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