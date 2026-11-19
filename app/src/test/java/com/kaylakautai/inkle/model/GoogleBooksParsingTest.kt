package com.kaylakautai.inkle.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Google Books models declare only the fields the app uses, relying on Gson (the Retrofit
 * converter) to skip the rest, and on nullable fields for books that leave some out.
 */
class GoogleBooksParsingTest {

    private val gson = Gson()

    @Test
    fun fullResult_mapsTheFieldsTheAppUses_andSkipsTheRest() {
        val book = gson.fromJson(SEARCH_RESPONSE, Book::class.java).items!!.toModels().first()

        assertEquals("gb-sapiens", book.googleBookId)
        assertEquals("Sapiens", book.title)
        assertEquals(listOf("Yuval Noah Harari"), book.authors)
        assertEquals("A brief history of humankind.", book.description)
        // Upgraded to https (cleartext traffic is disabled).
        assertEquals("https://books.google.com/books/content?id=gb-sapiens", book.imgUrl)
        assertEquals("2015-02-10", book.pubDate)
        assertEquals(464, book.pageCount)
        assertEquals(listOf("History / World"), book.genres)
        // Google's average rating isn't the user's rating.
        assertNull(book.rating)
    }

    @Test
    fun sparseResult_leavesMissingFieldsEmpty() {
        val book = gson.fromJson(SEARCH_RESPONSE, Book::class.java).items!!.toModels()[1]

        assertEquals("gb-sparse", book.googleBookId)
        assertEquals("Untitled pamphlet", book.title)
        assertNull(book.authors)
        assertNull(book.description)
        assertNull(book.imgUrl)
        assertNull(book.genres)
        // Google sends 0 for an unknown length.
        assertNull(book.pageCount)
    }

    @Test
    fun noMatches_hasNoItems() {
        val response = gson.fromJson("""{"kind": "books#volumes", "totalItems": 0}""", Book::class.java)

        assertNull(response.items)
    }

    @Test
    fun singleVolume_parsesAsAnItem() {
        val item = gson.fromJson(SINGLE_VOLUME, Item::class.java)

        val book = item.toModel()
        assertEquals("gb-sapiens", book.googleBookId)
        assertTrue(book.genres!!.isNotEmpty())
    }

    @Test
    fun volumeWithoutVolumeInfo_stillHasItsId() {
        val book = gson.fromJson("""{"id": "gb-bare"}""", Item::class.java).toModel()

        assertEquals("gb-bare", book.googleBookId)
        assertNull(book.title)
    }

    private companion object {
        val SINGLE_VOLUME = """
            {
              "kind": "books#volume",
              "id": "gb-sapiens",
              "etag": "abc",
              "selfLink": "https://www.googleapis.com/books/v1/volumes/gb-sapiens",
              "volumeInfo": {
                "title": "Sapiens",
                "authors": ["Yuval Noah Harari"],
                "publisher": "Harper",
                "publishedDate": "2015-02-10",
                "description": "A brief history of humankind.",
                "industryIdentifiers": [{"type": "ISBN_13", "identifier": "9780062316097"}],
                "readingModes": {"text": true, "image": false},
                "pageCount": 464,
                "printType": "BOOK",
                "categories": ["History / World"],
                "averageRating": 4.5,
                "ratingsCount": 1234,
                "maturityRating": "NOT_MATURE",
                "panelizationSummary": {"containsEpubBubbles": false, "containsImageBubbles": false},
                "imageLinks": {
                  "smallThumbnail": "http://books.google.com/books/content?id=gb-sapiens&zoom=5",
                  "thumbnail": "http://books.google.com/books/content?id=gb-sapiens"
                },
                "language": "en",
                "infoLink": "https://books.google.com/books?id=gb-sapiens"
              },
              "saleInfo": {
                "country": "NZ",
                "saleability": "FOR_SALE",
                "listPrice": {"amount": 25.0, "currencyCode": "NZD"},
                "retailPrice": {"amount": 20.0, "currencyCode": "NZD"},
                "offers": [{"finskyOfferType": 1, "listPrice": {"amountInMicros": 25000000}}]
              },
              "accessInfo": {
                "country": "NZ",
                "epub": {"isAvailable": true},
                "pdf": {"isAvailable": false}
              },
              "searchInfo": {"textSnippet": "A history of humankind"}
            }
        """.trimIndent()

        val SEARCH_RESPONSE = """
            {
              "kind": "books#volumes",
              "totalItems": 2,
              "items": [
                $SINGLE_VOLUME,
                {
                  "id": "gb-sparse",
                  "volumeInfo": {"title": "Untitled pamphlet", "pageCount": 0}
                }
              ]
            }
        """.trimIndent()
    }
}