package com.example.readingapp.ui.user.update

import androidx.compose.ui.graphics.Color
import com.example.readingapp.R
import com.example.readingapp.ui.theme.Blue
import com.example.readingapp.ui.theme.Green
import com.example.readingapp.ui.theme.Orange
import com.example.readingapp.ui.theme.Pink
import com.example.readingapp.ui.theme.Purple
import com.example.readingapp.ui.theme.Yellow
import com.example.readingapp.model.DEFAULT_READING_GOAL

data class UpdateUserUiState(
    val userId: String? = null,
    val displayNameInput: String = "",
    val bioInput: String = "",
    val readingGoal: Int = DEFAULT_READING_GOAL,
    val selectedAvatar: Avatar? = null,
    val selectedColor: ProfileColor = ProfileColor.PURPLE,
    val avatarImgList: List<Avatar> = Avatar.entries
)

enum class Avatar(
    val id: String,
    val img: Int,
    val color: Color
) {
    AVATAR_1("avatar_1", R.drawable.avatar_1, Pink),
    AVATAR_2("avatar_2", R.drawable.avatar_2, Yellow),
    AVATAR_3("avatar_3", R.drawable.avatar_3, Blue),
    AVATAR_4("avatar_4", R.drawable.avatar_4, Orange),
    AVATAR_5("avatar_5", R.drawable.avatar_5, Purple),
    AVATAR_6("avatar_6", R.drawable.avatar_6, Yellow),
    AVATAR_7("avatar_7", R.drawable.avatar_7, Pink);
}

enum class ProfileColor(val id: String, val color: Color) {
    PINK("pink", Pink),
    ORANGE("orange", Orange),
    YELLOW("yellow", Yellow),
    GREEN("green", Green),
    BLUE("blue", Blue),
    PURPLE("purple", Purple);

    companion object {
        fun fromId(id: String?): ProfileColor? = entries.find { it.id == id }

        fun fromColor(color: Color): ProfileColor = entries.find { it.color == color } ?: PURPLE
    }
}