package com.lts360.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lts360.R
import com.lts360.compose.ui.theme.viewmodels.ThemeViewModel
import org.koin.androidx.compose.koinViewModel


private val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)


data class CustomColorScheme(
    val navigationBarColor: Color,
    val linkColor: Color,
    val chatTextLinkColor: Color,
    val searchBarColor: Color,
    val grayVariant: Color,
    val moreActionsContainerColor: Color,
    val shimmerContainer: Color,
    val shimmerColor: Color,
    val serviceSurfaceContainer: Color,
    val colorScheme: ColorScheme,

    val textVariant1:Color,
    val grayTextVariant1:Color,

    val jobUserProfileBackground:Color
)

private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFF00BFFF),
    secondary = Color(0xFF1DA1F2),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    surfaceContainer = Color(0xFF121212),
    primaryContainer = Color(0xFF00BFFF),
    secondaryContainer = Color(0xFF1E1E1E),
    onSecondaryContainer = Color.White,
    onSurface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    error = Color.Red,
    surfaceContainerLow = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF1E1E1E)
)

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF00BFFF),
    secondary = Color(0xFF1DA1F2),
    background = Color.White,
    surface = Color.White,
    surfaceContainer = Color.White,
    secondaryContainer = Color(red = 232, green = 222, blue = 248),
    primaryContainer = Color(0xFF00BFFF),
    onSecondaryContainer = Color(0xFF00BFFF),
    onSurface = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    error = Color.Red,
    surfaceContainerLow = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceVariant = Color(0xFF1E1E1E)

    )


val CustomDarkColorScheme = CustomColorScheme(
    navigationBarColor = Color.Black,
    linkColor = Color(0xFFBBDEFB),
    chatTextLinkColor = Color(0xFF1E88E5),
    searchBarColor = Color(0xFF1E1E1E),
    grayVariant = Color(0xFF1E1E1E),
    moreActionsContainerColor = Color(0xFF1E1E1E),
    shimmerContainer = Color(0xFF1E1E1E),
    shimmerColor = Color.Black,
    serviceSurfaceContainer = Color(0xFF1E1E1E),
    textVariant1 = Color.White,
    grayTextVariant1=Color.White,

    jobUserProfileBackground = Color.Black,

    colorScheme = DarkColorPalette
)

val CustomLightColorScheme = CustomColorScheme(
    navigationBarColor = Color.White,
    linkColor = Color(0xFF1E88E5),
    chatTextLinkColor = Color(0xFF1E88E5),
    searchBarColor = Color(0xFFF1F3F4),
    grayVariant = Color(0xFFF1F3F4),

    moreActionsContainerColor = Color(0xFFF8F8F8),
    shimmerContainer = Color.LightGray,
    shimmerColor = Color.White,
    serviceSurfaceContainer = Color(0xFFFFFAF6),

    textVariant1 = Color(0xFF4F5563),
    grayTextVariant1=Color.DarkGray,

    jobUserProfileBackground = Color(0xFFE5F3FD),

    colorScheme = LightColorPalette
)


val LocalCustomColorScheme = staticCompositionLocalOf { CustomLightColorScheme }

val LocalCustomIcons = staticCompositionLocalOf { LightIcons }


val CustomFontFamily = FontFamily(
    Font(R.font.roboto),
    Font(R.font.roboto_bold, FontWeight.Bold)
)


val customTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = CustomFontFamily),
    displayMedium = Typography().displayMedium.copy(fontFamily = CustomFontFamily),
    displaySmall = Typography().displaySmall.copy(fontFamily = CustomFontFamily),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = CustomFontFamily),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = CustomFontFamily),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = CustomFontFamily),
    titleLarge = Typography().titleLarge.copy(fontFamily = CustomFontFamily),
    titleMedium = Typography().titleMedium.copy(fontFamily = CustomFontFamily),
    titleSmall = Typography().titleSmall.copy(fontFamily = CustomFontFamily),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = CustomFontFamily),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = CustomFontFamily),
    bodySmall = Typography().bodySmall.copy(fontFamily = CustomFontFamily),
    labelLarge = Typography().labelLarge.copy(fontFamily = CustomFontFamily),
    labelMedium = Typography().labelMedium.copy(fontFamily = CustomFontFamily),
    labelSmall = Typography().labelSmall.copy(fontFamily = CustomFontFamily)
)


@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val viewModel: ThemeViewModel = koinViewModel()
    val themeMode by viewModel.themeMode.collectAsState()

    val darkTheme = when (themeMode) {
        1 -> true
        0 -> false
        else -> isSystemInDarkTheme()
    }


    val customColorScheme = if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme
    val icons = if (darkTheme) LightIcons else DarkIcons

    CompositionLocalProvider(
        LocalCustomColorScheme provides customColorScheme,
        LocalCustomIcons provides icons
    ) {
        MaterialTheme(
            colorScheme = customColorScheme.colorScheme,
            shapes = Shapes,
            typography = customTypography,
            content = content
        )
    }
}


val MaterialTheme.customColorScheme: CustomColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColorScheme.current


val MaterialTheme.icons: LocalIcons
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomIcons.current


