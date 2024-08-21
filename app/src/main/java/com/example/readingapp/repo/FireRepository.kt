package com.example.readingapp.repo

import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBook
import com.example.readingapp.model.MUser
import com.example.readingapp.model.toModel
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
        return fetchSavedBooksByUser(user.value?.userId.toString())
    }

    suspend fun fetchSavedBooksByUser(userId: String): DataOrException<List<MBook>, Boolean, Exception> {
        val result = getAllBooksFromDatabase()
        val savedBooks = result.data?.filter {  it.userId == userId } ?: emptyList()
        _savedBooks.update { savedBooks }

        return result
    }

    suspend fun getAllBooksFromDatabase(): DataOrException<List<MBook>, Boolean, Exception> {
        val dataOrException = DataOrException<List<MBook>, Boolean, Exception>()

        try {
            dataOrException.loading = true
            dataOrException.data = queryBook.get().await().documents.map { documentSnapshot ->
                documentSnapshot.toObject(MBook::class.java)!!
            }
            if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false
        } catch (e: FirebaseFirestoreException) {
            dataOrException.e = e
        }

        return dataOrException
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
        FirebaseFirestore.getInstance()
            .collection(BOOKS_COLLECTION)
            .document(bookId)
            .update(bookUpdates)
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