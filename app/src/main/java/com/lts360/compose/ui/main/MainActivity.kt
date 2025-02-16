package com.lts360.compose.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.app.database.AppDatabase
import com.lts360.compose.ui.chat.ChatActivity
import com.lts360.compose.ui.chat.ChatUtilNativeBaseActivity
import com.lts360.compose.ui.main.navhosts.MainNavHost
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ChatUtilNativeBaseActivity() {


    @Inject
    lateinit var networkConnectivityManager: NetworkConnectivityManager

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var appDatabase: AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if(savedInstanceState==null){
            val senderId = intent.getLongExtra("sender_id", -1)
            if (senderId != -1L) {
                startActivity(
                    Intent(this, ChatActivity::class.java)
                        .apply { putExtra("sender_id", senderId) }
                )
            }
        }


        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox{
                        MainNavHost()
                    }
                }
            }

        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val senderId = intent.getLongExtra("sender_id", -1)
        if (senderId != -1L) {
            startActivity(
                Intent(this, ChatActivity::class.java)
                    .apply { putExtra("sender_id", senderId) }
            )
        }

    }


}

