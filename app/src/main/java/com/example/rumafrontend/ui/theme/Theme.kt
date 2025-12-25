package com.example.rumafrontend.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    
    primary = PrimaryRed,                    
    onPrimary = TextLight,                   
    primaryContainer = PrimaryRed.copy(alpha = 0.2f),  
    onPrimaryContainer = PrimaryRed,         

    
    secondary = SecondaryBrown,              
    onSecondary = TextLight,                 
    secondaryContainer = SecondaryBrown.copy(alpha = 0.2f),
    onSecondaryContainer = SecondaryBrown,

    
    tertiary = WarmWood,                     
    onTertiary = TextLight,                  
    tertiaryContainer = WarmWood.copy(alpha = 0.2f),
    onTertiaryContainer = WarmWood,

    
    background = BackgroundColor,            
    onBackground = TextDark,                 

    
    surface = Color.White,                   
    onSurface = TextDark,                    
    surfaceVariant = BackgroundColor,        
    onSurfaceVariant = TextDark,             

    
    surfaceTint = PrimaryRed,                

    
    inverseSurface = TextDark,               
    inverseOnSurface = BackgroundColor,      
    inversePrimary = PrimaryRed.copy(alpha = 0.8f),

    
    error = Color(0xFFB3261E),               
    onError = Color.White,                    
    errorContainer = Color(0xFFF9DEDC),      
    onErrorContainer = Color(0xFF410E0B),    

    
    outline = SecondaryBrown.copy(alpha = 0.5f),  
    outlineVariant = SecondaryBrown.copy(alpha = 0.2f), 

    
    scrim = Color.Black.copy(alpha = 0.32f)
)

private val DarkColorScheme = darkColorScheme(
    
    primary = PrimaryRed.copy(alpha = 0.9f),  
    onPrimary = TextLight,                    
    primaryContainer = PrimaryRed.copy(alpha = 0.3f),
    onPrimaryContainer = TextLight,

    
    secondary = SecondaryBrown.copy(alpha = 0.9f),
    onSecondary = TextDark,
    secondaryContainer = SecondaryBrown.copy(alpha = 0.3f),
    onSecondaryContainer = TextLight,

    
    tertiary = WarmWood.copy(alpha = 0.9f),
    onTertiary = TextLight,
    tertiaryContainer = WarmWood.copy(alpha = 0.3f),
    onTertiaryContainer = TextLight,

    
    background = Color(0xFF1C1B1F),           
    onBackground = BackgroundColor,           

    
    surface = Color(0xFF1C1B1F),              
    onSurface = BackgroundColor,              
    surfaceVariant = Color(0xFF49454F),       
    onSurfaceVariant = BackgroundColor.copy(alpha = 0.8f),

    
    surfaceTint = PrimaryRed.copy(alpha = 0.8f),

    
    inverseSurface = BackgroundColor,
    inverseOnSurface = TextDark,
    inversePrimary = PrimaryRed,

    
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    
    outline = SecondaryBrown.copy(alpha = 0.6f),
    outlineVariant = SecondaryBrown.copy(alpha = 0.3f),

    
    scrim = Color.Black.copy(alpha = 0.5f)
)

@Composable
fun rumaFrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        
        darkTheme -> DarkColorScheme

        
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            
            window.statusBarColor = colorScheme.background.toArgb()

            
            window.navigationBarColor = colorScheme.surface.toArgb()

            
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

            
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun RumaLightTheme(content: @Composable () -> Unit) {
    rumaFrontendTheme(
        darkTheme = false,
        dynamicColor = false,
        content = content
    )
}

@Composable
fun RumaDarkTheme(content: @Composable () -> Unit) {
    rumaFrontendTheme(
        darkTheme = true,
        dynamicColor = false,
        content = content
    )
}