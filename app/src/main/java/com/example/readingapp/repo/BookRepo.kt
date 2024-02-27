package com.example.readingapp.repo

import com.example.readingapp.data.RemoteResult
import com.example.readingapp.data.asResult
import com.example.readingapp.model.MBook
import com.example.readingapp.model.toModel
import com.example.readingapp.model.toModels
import com.example.readingapp.network.BookApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookRepo @Inject constructor(private val bookApi: BookApi) {
    suspend fun getBooksByQuery(q: String): Flow<RemoteResult<List<MBook>>> {
        return flow {
            emit(bookApi.getBooksByQuery(q).items.toModels())
        }.asResult()
    }

    suspend fun getBookInfo(bookId: String): Flow<RemoteResult<MBook>> {
        return flow {
            emit(bookApi.getBookById(bookId).toModel())
        }.asResult()
    }
}