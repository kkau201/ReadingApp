package com.example.readingapp.repo

import com.example.readingapp.data.DataOrException
import com.example.readingapp.model.Item
import com.example.readingapp.network.BookApi
import javax.inject.Inject

class BookRepo @Inject constructor(private val bookApi: BookApi) {
    private val itemListDataOrException = DataOrException<List<Item>, Boolean, Exception>()
    private val bookInfoDataOrException = DataOrException<Item, Boolean, Exception>()

    suspend fun getBooks(searchQuery: String): DataOrException<List<Item>, Boolean, Exception> {
        try {
            itemListDataOrException.loading = true
            itemListDataOrException.data = bookApi.getAllBooks(searchQuery).items
            if(itemListDataOrException.data!!.isNotEmpty()) itemListDataOrException.loading = false
        } catch (e: Exception) {
            itemListDataOrException.e = e
        }

        return itemListDataOrException
    }


    suspend fun getBookInfo(bookId: String): DataOrException<Item, Boolean, Exception> {
        try {
            bookInfoDataOrException.loading = true
            bookInfoDataOrException.data = bookApi.getBookById(bookId)
            if(bookInfoDataOrException.data.toString().isNotEmpty()) bookInfoDataOrException.loading = false
        } catch (e: Exception) {
            bookInfoDataOrException.e = e
        }

        return bookInfoDataOrException
    }
}