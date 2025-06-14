package com.lts360.compose.ui.main.prefs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.lts360.api.auth.managers.TokenManager
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import org.koin.android.ext.android.inject

class BoardsSetupActivity:ComponentActivity() {

   val tokenManager : TokenManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val isGuest = tokenManager.isGuest()

        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {
                        if(isGuest){
                            GuestBoardsPreferencesScreen{
                                this@BoardsSetupActivity.finish()
                            }
                        }else{
                            BoardsPreferencesScreen{
                                this@BoardsSetupActivity.finish()
                            }
                        }

                    }
                }
            }
        }
    }
}