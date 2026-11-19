package com.example.readingapp.ui.user.update

import com.example.readingapp.model.DEFAULT_READING_GOAL
import com.example.readingapp.ui.model.Avatar

data class UpdateUserUiState(
    val userId: String? = null,
    val displayNameInput: String = "",
    /** Set when Save is tapped with a blank name; cleared when the name is edited. */
    val displayNameError: Boolean = false,
    val bioInput: String = "",
    val readingGoal: Int = DEFAULT_READING_GOAL,
    /** This year's reward for reaching the goal; blank for none. */
    val rewardInput: String = "",
    val selectedAvatar: Avatar? = null,
    val avatarImgList: List<Avatar> = Avatar.entries
)

