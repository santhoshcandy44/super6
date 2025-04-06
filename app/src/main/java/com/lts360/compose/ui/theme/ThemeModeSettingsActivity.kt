package com.lts360.compose.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.lts360.compose.ui.news.LanguageSettingsScreen
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeModeSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {

                Surface {
                    SafeDrawingBox {
                           ThemeSettingsScreen({
                            finish()
                        })
                    }
                }

            }
        }
    }
}