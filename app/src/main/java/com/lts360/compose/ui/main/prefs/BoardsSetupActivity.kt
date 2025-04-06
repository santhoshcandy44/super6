package com.lts360.compose.ui.main.prefs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.lts360.api.auth.managers.TokenManager
import com.lts360.compose.ui.bookmarks.BookmarksScreen
import com.lts360.compose.ui.chat.ChatScreen
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.viewmodels.HomeActivityViewModel
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoardsSetupActivity:ComponentActivity() {

    @Inject lateinit var tokenManager : TokenManager


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