package com.example.readingapp.repo

import com.example.readingapp.model.MBook
import com.example.readingapp.model.toModel
import com.example.readingapp.model.toModels
import com.example.readingapp.network.BookApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookRepo @Inject constructor(private val bookApi: BookApi) {
    suspend fun getBooksByQuery(q: String): Flow<List<MBook>> {
        val bookSearchFlow: Flow<List<MBook>> = flow {
            emit(bookApi.getBooksByQuery(q).items.toModels())
        }
        return bookSearchFlow
    }

    suspend fun getBookInfo(bookId: String): Flow<MBook> {
        val bookByIdFlow: Flow<MBook> = flow {
            emit(bookApi.getBookById(bookId).toModel())
        }
        return bookByIdFlow
    }
}