package com.example.readingapp.network

import com.example.readingapp.model.Book
import com.example.readingapp.model.Item
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface BookApi {
    @GET("volumes")
    suspend fun getBooksByQuery(@Query("q") query: String): Book

    @GET("volumes/{bookId}")
    suspend fun getBookById(@Path("bookId") bookId: String): Item

}