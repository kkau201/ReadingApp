package com.example.readingapp.ui.user.update

import com.example.readingapp.R

data class UpdateUserUiState(
    val userId: String? = null,
    val displayNameInput: String = "",
    val bioInput: String = "",
    val selectedAvatarId: String = "",
    val avatarImgList: List<Avatar> = Avatar.entries
)

enum class Avatar(
    val id: String,
    val img: Int
) {
    AVATAR_1("avatar_1", R.drawable.avatar_1),
    AVATAR_2("avatar_2", R.drawable.avatar_2),
    AVATAR_3("avatar_3", R.drawable.avatar_3),
    AVATAR_4("avatar_4", R.drawable.avatar_4),
    AVATAR_5("avatar_5", R.drawable.avatar_5),
    AVATAR_6("avatar_6", R.drawable.avatar_6),
    AVATAR_7("avatar_7", R.drawable.avatar_7);
}