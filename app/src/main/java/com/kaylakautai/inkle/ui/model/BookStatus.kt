package com.kaylakautai.inkle.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector

enum class BookStatus(val icon: ImageVector, val statusText: String) {
    LIBRARY(Icons.Rounded.Book, "Library"),
    READING(Icons.AutoMirrored.Rounded.MenuBook, "Reading"),
    FINISHED(Icons.Rounded.CheckCircle, "Finished")
}
