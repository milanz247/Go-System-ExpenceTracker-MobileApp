package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.R

private const val GOOGLE_FONTS_PROVIDER_AUTHORITY = "com.google.android.gms.fonts"
private const val GOOGLE_FONTS_PROVIDER_PACKAGE = "com.google.android.gms"

private val fontProvider = GoogleFont.Provider(
    providerAuthority = GOOGLE_FONTS_PROVIDER_AUTHORITY,
    providerPackage = GOOGLE_FONTS_PROVIDER_PACKAGE,
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val geistGoogleFont = GoogleFont("Geist")
private val geistMonoGoogleFont = GoogleFont("Geist Mono")

// Downloaded lazily from Google Play services on first use; falls back to the
// platform default sans/mono typeface until the fetch resolves (or on devices
// without Play services), so the UI never blocks or breaks on font load.
val GeistSans = FontFamily(
    Font(googleFont = geistGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = geistGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = geistGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = geistGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold),
)

val GeistMono = FontFamily(
    Font(googleFont = geistMonoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = geistMonoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = geistMonoGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = geistMonoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold),
)
