package com.example.lifesync.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.example.lifesync.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val LexendFont = FontFamily(
    Font(
        googleFont = GoogleFont("Lexend"),
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = GoogleFont("Lexend"),
        fontProvider = provider,
        weight = FontWeight.SemiBold
    ),
    Font(
        googleFont = GoogleFont("Lexend"),
        fontProvider = provider,
        weight = FontWeight.Normal
    )
)

val PlusJakartaSansFont = FontFamily(
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = provider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = provider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = provider,
        weight = FontWeight.Normal
    )
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = LexendFont,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        letterSpacing = (-0.02).em
    ),
    headlineMedium = TextStyle(
        fontFamily = LexendFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        letterSpacing = (-0.01).em
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.em
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.em
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.02.em
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.05.em
    )
)
