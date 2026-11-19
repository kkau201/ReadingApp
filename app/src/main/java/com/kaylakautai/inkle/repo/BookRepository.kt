package com.kaylakautai.inkle.repo

import com.kaylakautai.inkle.data.RemoteResult
import com.kaylakautai.inkle.data.asResult
import com.kaylakautai.inkle.model.MBook
import com.kaylakautai.inkle.model.toModel
import com.kaylakautai.inkle.model.toModels
import com.kaylakautai.inkle.network.BookApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookRepository @Inject constructor(private val bookApi: BookApi) {
    suspend fun getBooksByQuery(q: String): Flow<RemoteResult<List<MBook>>> {
        return flow {
            emit(bookApi.getBooksByQuery(q).items?.toModels() ?: emptyList())
        }.asResult()
    }

    suspend fun getBookInfo(bookId: String): Flow<RemoteResult<MBook>> {
        return flow {
            emit(bookApi.getBookById(bookId).toModel())
        }.asResult()
    }
}