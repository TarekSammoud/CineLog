package fr.eseo.ld.ts.cinelog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import fr.eseo.ld.ts.cinelog.R
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

// Google Fonts provider for Roboto
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Roboto font for body text, labels, buttons, etc.
val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = provider,
    )
)

val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = provider,
    )
)

// CA Slalom Extended - only for titles / highlighted text
val CASlalomExtended = FontFamily(
    Font(R.font.caslalomextended_light, FontWeight.Light),
    Font(R.font.caslalomextended_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.caslalomextended_regular, FontWeight.Normal),
    Font(R.font.caslalomextended_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.caslalomextended_medium, FontWeight.Medium),
    Font(R.font.caslalomextended_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.caslalomextended_bold, FontWeight.Bold),
    Font(R.font.caslalomextended_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.caslalomextended_heavy, FontWeight.ExtraBold),
    Font(R.font.caslalomextended_heavyitalic, FontWeight.ExtraBold, FontStyle.Italic)
)

// Default Material 3 typography
val baseline = Typography()

val AppTypography = Typography(
    // Titles / highlighted text
    displayLarge = baseline.displayLarge.copy(fontFamily = CASlalomExtended),
    displayMedium = baseline.displayMedium.copy(fontFamily = CASlalomExtended),
    displaySmall = baseline.displaySmall.copy(fontFamily = CASlalomExtended),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = CASlalomExtended),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = CASlalomExtended),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = CASlalomExtended),
    titleLarge = baseline.titleLarge.copy(fontFamily = CASlalomExtended),
    titleMedium = baseline.titleMedium.copy(fontFamily = CASlalomExtended),
    titleSmall = baseline.titleSmall.copy(fontFamily = CASlalomExtended),

    // Body text, labels, buttons use Roboto with Google Fonts provider
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)
