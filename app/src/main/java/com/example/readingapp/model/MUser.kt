package com.example.readingapp.model

import com.google.firebase.firestore.QueryDocumentSnapshot

const val USER_ID = "user_id"
const val DISPLAY_NAME = "display_name"
const val AVATAR_URL = "avatar_url"
const val QUOTE = "quote"
const val PROFESSION = "profession"

data class MUser(
    val id: String? = null,
    val userId: String,
    val displayName: String,
    val avatarUrl: String = "",
    val quote: String = "",
    val profession: String = ""
) {
    fun toMap(): MutableMap<String, Any?> {
        return mutableMapOf(
            USER_ID to this.userId,
            DISPLAY_NAME to this.displayName,
            AVATAR_URL to this.avatarUrl,
            QUOTE to this.quote,
            PROFESSION to this.profession,
        )
    }
}

fun QueryDocumentSnapshot.toModel(): MUser {
    return MUser(
        id = this.id,
        userId = this.get(USER_ID).toString(),
        displayName = this.get(DISPLAY_NAME).toString(),
        avatarUrl = this.get(AVATAR_URL).toString(),
        quote = this.get(QUOTE).toString(),
        profession = this.get(PROFESSION).toString()
    )
}