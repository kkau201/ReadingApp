package com.kaylakautai.inkle.ui.user.update

import com.kaylakautai.inkle.ui.model.Avatar

data class UpdateUserUiState(
    val userId: String? = null,
    val displayNameInput: String = "",
    /** Set when Save is tapped with a blank name; cleared when the name is edited. */
    val displayNameError: Boolean = false,
    val bioInput: String = "",
    val selectedAvatar: Avatar? = null,
    val avatarImgList: List<Avatar> = Avatar.entries
)

