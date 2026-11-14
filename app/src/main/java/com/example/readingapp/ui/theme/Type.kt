package com.example.readingapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.readingapp.R

private val NanumMyeongjo = FontFamily(
    Font(R.font.nanummyeongjo_regular, FontWeight.Normal),
    Font(R.font.nanummyeongjo_regular, FontWeight.Medium),
    Font(R.font.nanummyeongjo_bold, FontWeight.SemiBold),
    Font(R.font.nanummyeongjo_bold, FontWeight.Bold)
)

private val ArapeyItalic = FontFamily(
    Font(R.font.arapey_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.arapey_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.arapey_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.arapey_italic, FontWeight.Bold, FontStyle.Italic)
)

private val Lato = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_medium, FontWeight.Medium),
    Font(R.font.lato_semibold, FontWeight.SemiBold),
    Font(R.font.lato_bold, FontWeight.Bold)
)

private fun type(
    family: FontFamily,
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    tracking: Float = 0f,
    style: FontStyle = FontStyle.Normal
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontStyle = style,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp
)

val Typography = Typography(
    displayLarge = type(NanumMyeongjo, 40, 46, FontWeight.SemiBold, -1f),
    displayMedium = type(NanumMyeongjo, 36, 42, FontWeight.SemiBold, -0.8f),
    displaySmall = type(NanumMyeongjo, 34, 40, FontWeight.SemiBold, -0.8f),
    headlineLarge = type(NanumMyeongjo, 28, 34, FontWeight.SemiBold, -0.5f),
    headlineMedium = type(NanumMyeongjo, 24, 30, FontWeight.SemiBold, -0.4f),
    headlineSmall = type(NanumMyeongjo, 20, 26, FontWeight.SemiBold, -0.3f),
    titleLarge = type(NanumMyeongjo, 18, 24, FontWeight.SemiBold, -0.3f),
    titleMedium = type(ArapeyItalic, 16, 22, FontWeight.Normal, style = FontStyle.Italic),
    titleSmall = type(ArapeyItalic, 14, 20, FontWeight.Normal, style = FontStyle.Italic),
    bodyLarge = type(Lato, 16, 24, FontWeight.Normal, 0.1f),
    bodyMedium = type(Lato, 14, 20, FontWeight.Normal, 0.1f),
    bodySmall = type(Lato, 12, 16, FontWeight.Normal, 0.1f),
    labelLarge = type(Lato, 14, 18, FontWeight.Medium, 0.1f),
    labelMedium = type(Lato, 12, 16, FontWeight.Medium, 0.4f),
    labelSmall = type(Lato, 11, 14, FontWeight.SemiBold, 1.1f)
)
