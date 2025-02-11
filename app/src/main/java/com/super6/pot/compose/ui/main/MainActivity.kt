package com.super6.pot.compose.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.app.database.AppDatabase
import com.super6.pot.components.utils.LogUtils.TAG
import com.super6.pot.compose.ui.auth.AuthActivity
import com.super6.pot.compose.ui.chat.ChatActivity
import com.super6.pot.compose.ui.chat.ChatUtilNativeBaseActivity
import com.super6.pot.compose.ui.main.navhosts.MainNavHost
import com.super6.pot.compose.ui.managers.NetworkConnectivityManager
import com.super6.pot.compose.ui.theme.AppTheme
import com.super6.pot.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

