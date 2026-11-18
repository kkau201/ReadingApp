package com.example.readingapp.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Whether decorative animations should play. Off when the user has turned animations off
 * (Accessibility), and in previews.
 */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    return remember(context, inPreview) {
        !inPreview && Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }
}
