package com.example.readingapp.repo

import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.MBook
import com.example.readingapp.model.toModel
import com.example.readingapp.model.toModels
import com.example.readingapp.network.BookApi
import javax.inject.Inject

class BookRepo @Inject constructor(private val bookApi: BookApi) {
    private val itemListDataOrException = DataOrException<List<MBook>, Boolean, Exception>()
    private val bookInfoDataOrException = DataOrException<MBook, Boolean, Exception>()

    suspend fun getBooks(searchQuery: String): DataOrException<List<MBook>, Boolean, Exception> {
        try {
            itemListDataOrException.loading = true
            itemListDataOrException.data = bookApi.getAllBooks(searchQuery).items.toModels()
            if(itemListDataOrException.data!!.isNotEmpty()) itemListDataOrException.loading = false
        } catch (e: Exception) {
            itemListDataOrException.e = e
        }

        return itemListDataOrException
    }


    suspend fun getBookInfo(bookId: String): DataOrException<MBook, Boolean, Exception> {
        try {
            bookInfoDataOrException.loading = true
            bookInfoDataOrException.data = bookApi.getBookById(bookId).toModel()
            if(bookInfoDataOrException.data.toString().isNotEmpty()) bookInfoDataOrException.loading = false
        } catch (e: Exception) {
            bookInfoDataOrException.e = e
        }

        return bookInfoDataOrException
    }
}