package com.super6.pot.ui.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.super6.pot.R
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.ui.auth.navhost.slideComposable
import com.super6.pot.ui.chat.viewmodels.ChatViewModel
import com.super6.pot.ui.chat.viewmodels.IsolatedChatActivityViewModel
import com.super6.pot.ui.main.navhosts.routes.ChatWindow
import com.super6.pot.ui.main.navhosts.routes.UserProfileSerializer
import com.super6.pot.ui.theme.AppTheme
import com.super6.pot.ui.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class IsolatedChatActivity : ComponentActivity() {

    @Inject
    lateinit var chatUserDao: ChatUserDao

    val isolatedChatActivityViewModel: IsolatedChatActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val chatId = intent.getIntExtra("chat_id", -1)
        val recipientId = intent.getLongExtra("recipient_id", -1)
        val feedUserProfileString = intent.getStringExtra("feed_user_profile")

        if (chatId == -1 || recipientId == -1L || feedUserProfileString == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            val chatUser = withContext(Dispatchers.IO) {
                chatUserDao.getSpecificChatUser(chatId)
            }

            isolatedChatActivityViewModel.loadChatUser(chatUser)

            setContent {

                AppTheme {
                    Surface {
                        SafeDrawingBox {
                            val context = LocalContext.current
                            val navController = rememberNavController()
                            NavHost(
                                navController, ChatWindow(
                                    chatId, recipientId)
                            ) {
                                slideComposable<ChatWindow> { backStackEntry ->

                                    val chatViewModel: ChatViewModel = hiltViewModel()
                                    val args = backStackEntry.toRoute<ChatWindow>()
                                    val userState by isolatedChatActivityViewModel.selectedChatUser.collectAsState()

                                    userState?.let {

                                        IsolatedChatScreen(
                                            { uri, videoWidth, videoHeight, totalDuration ->
                                                openPlayerActivity(
                                                    context,
                                                    uri,
                                                    videoWidth,
                                                    videoHeight,
                                                    totalDuration
                                                )
                                            },

                                            { uri, imageWidth, imageHeight ->
                                                openImageSliderActivity(
                                                    context,
                                                    uri,
                                                    imageWidth,
                                                    imageHeight
                                                )
                                            },
                                            it,
                                            isolatedChatActivityViewModel,
                                            { navController.popBackStack() },
                                            chatViewModel,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }


    }
}