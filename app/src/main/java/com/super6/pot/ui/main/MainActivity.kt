package com.super6.pot.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.ui.chat.ChatActivity
import com.super6.pot.ui.chat.ChatUtilNativeBaseActivity
import com.super6.pot.ui.main.navhosts.MainNavHost
import com.super6.pot.ui.managers.NetworkConnectivityManager
import com.super6.pot.ui.theme.AppTheme
import com.super6.pot.ui.utils.SafeDrawingBox
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

