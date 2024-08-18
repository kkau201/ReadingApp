package com.example.readingapp.repo

import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBookDetails
import com.example.readingapp.model.MUser
import com.example.readingapp.model.toModel
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireRepository @Inject constructor(
    private val queryBook: Query,
    private val queryUser: Query,
) {

    companion object {
        const val USER_ID = "user_id"
    }

    suspend fun getAllBooksFromDatabase(): DataOrException<List<MBookDetails>, Boolean, Exception> {
        val dataOrException = DataOrException<List<MBookDetails>, Boolean, Exception>()

        try {
            dataOrException.loading = true
            dataOrException.data = queryBook.get().await().documents.map { documentSnapshot ->
                documentSnapshot.toObject(MBookDetails::class.java)!!
            }
            if (!dataOrException.data.isNullOrEmpty()) dataOrException.loading = false
        } catch (e: FirebaseFirestoreException) {
            dataOrException.e = e
        }

        return dataOrException
    }

    suspend fun getUserDetailsFromDatabase(currentUserId: String?): DataOrException<MUser?, Boolean, Exception> {
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
}