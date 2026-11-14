package com.example.readingapp.model

import androidx.compose.ui.graphics.Color
import com.example.readingapp.ui.user.update.Avatar
import com.example.readingapp.ui.user.update.ProfileColor
import com.google.firebase.firestore.DocumentSnapshot

const val USER_ID = "user_id"
const val DISPLAY_NAME = "display_name"
const val AVATAR_URL = "avatar_url"
const val QUOTE = "quote"
const val READING_GOAL = "reading_goal"
const val PROFILE_COLOR = "profile_color"
const val DEFAULT_READING_GOAL = 8
const val MIN_READING_GOAL = 1
const val MAX_READING_GOAL = 24

fun Int?.asReadingGoal(): Int = (this ?: DEFAULT_READING_GOAL).coerceIn(MIN_READING_GOAL, MAX_READING_GOAL)

data class MUser(
    val id: String? = null,
    val userId: String,
    val displayName: String,
    val avatarUrl: String = "",
    val quote: String = "",
    val readingGoal: Int = DEFAULT_READING_GOAL,
    val profileColorId: String = ""
) {
    fun toMap(): MutableMap<String, Any?> {
        return mutableMapOf(
            USER_ID to this.userId,
            DISPLAY_NAME to this.displayName,
            AVATAR_URL to this.avatarUrl,
            QUOTE to this.quote,
            READING_GOAL to this.readingGoal.asReadingGoal(),
            PROFILE_COLOR to this.profileColorId,
        )
    }

    val avatar: Avatar
        get() = Avatar.entries.find { it.id == avatarUrl } ?: Avatar.AVATAR_1

    val profileColor: Color
        get() = ProfileColor.fromId(profileColorId)?.color ?: avatar.color
}

fun DocumentSnapshot.toModel(): MUser {
    return MUser(
        id = this.id,
        userId = this.get(USER_ID).toString(),
        displayName = this.get(DISPLAY_NAME).toString(),
        avatarUrl = this.get(AVATAR_URL).toString(),
        quote = this.get(QUOTE).toString(),
        readingGoal = (this.get(READING_GOAL) as? Number)?.toInt().asReadingGoal(),
        profileColorId = this.get(PROFILE_COLOR)?.toString()?.takeUnless { it == "null" }.orEmpty()
    )
}