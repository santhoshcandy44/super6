package com.lts360.compose.ui.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.IsolatedChatActivityViewModel
import com.lts360.compose.ui.main.navhosts.routes.MainRoutes
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel


class IsolatedChatActivity : ComponentActivity() {

    val chatUserDao: ChatUserDao by inject()

    private val isolatedChatActivityViewModel: IsolatedChatActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                            val backStack = rememberNavBackStack(MainRoutes.ChatWindow(chatId, recipientId))

                            NavDisplay(
                                backStack = backStack,
                                entryDecorators = listOf(
                                    rememberSceneSetupNavEntryDecorator(),
                                    rememberSavedStateNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator()
                                ),
                                entryProvider = entryProvider {
                                    entry<MainRoutes.ChatWindow> { entry ->
                                        val chatViewModel: ChatViewModel = koinViewModel()
                                        val userState by isolatedChatActivityViewModel.selectedChatUser.collectAsState()

                                        userState?.let { chatUser ->
                                            IsolatedChatScreen(
                                                onNavigateUpVideoPlayer = { uri, width, height, duration ->
                                                    openPlayerActivity(
                                                        context,
                                                        uri,
                                                        width,
                                                        height,
                                                        duration
                                                    )
                                                },
                                                onNavigateImageSlider = { uri, width, height ->
                                                    openImageSliderActivity(
                                                        context,
                                                        uri,
                                                        width,
                                                        height
                                                    )
                                                },
                                                userState = chatUser,
                                                isolatedChatActivityViewModel = isolatedChatActivityViewModel,
                                                onPopBackStack = { backStack.removeLastOrNull() },
                                                viewModel = chatViewModel
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }


    }
}