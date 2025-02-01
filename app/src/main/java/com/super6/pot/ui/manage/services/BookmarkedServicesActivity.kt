package com.super6.pot.ui.manage.services

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.super6.pot.R
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.ui.services.BookmarkedDetailedServiceInfoScreen
import com.super6.pot.ui.services.BookmarkedFeedUserDetailedServiceInfoScreen
import com.super6.pot.ui.services.BookmarkedImagesSliderScreen
import com.super6.pot.ui.services.bookmark.BookmarkedServicesScreen
import com.super6.pot.ui.services.bookmark.BookmarkedServicesViewModel
import com.super6.pot.ui.services.FeedUserImagesSliderScreen
import com.super6.pot.ui.chat.viewmodels.IsolatedChatActivityViewModel
import com.super6.pot.ui.chat.IsolatedChatScreen
import com.super6.pot.ui.services.ServiceOwnerProfileViewModel
import com.super6.pot.ui.auth.navhost.slideComposable
import com.super6.pot.ui.chat.IsolatedChatActivity
import com.super6.pot.ui.main.navhosts.routes.BookmarkedDetailedService
import com.super6.pot.ui.main.navhosts.routes.BookmarkedImagesSliderDetailedService
import com.super6.pot.ui.main.navhosts.routes.BookmarkedServices
import com.super6.pot.ui.main.navhosts.routes.ChatWindow
import com.super6.pot.ui.main.navhosts.routes.FeedUserDetailedService
import com.super6.pot.ui.main.navhosts.routes.FeedUserImagesSliderDetailedService
import com.super6.pot.ui.main.navhosts.routes.ServiceOwnerProfile
import com.super6.pot.ui.main.navhosts.routes.UserProfileSerializer
import com.super6.pot.ui.main.profile.BookmarkedServiceOwnerProfileScreen
import com.super6.pot.ui.theme.AppTheme
import com.super6.pot.ui.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BookmarkedServicesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {

                        val context = LocalContext.current

                        var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }

                        val navController =
                            rememberBookMarkedServicesCustomBottomNavController(lastEntry)

                        val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

                        LaunchedEffect(currentBackStackEntryAsState) {

                            lastEntry = navController.currentBackStackEntry?.destination?.route
                        }

                        NavHost(navController, BookmarkedServices) {

                            slideComposable<BookmarkedServices> {
                                BookmarkedServicesScreen({
                                    navController.navigate(BookmarkedDetailedService)
                                }, {
                                    navController.navigate(
                                        ServiceOwnerProfile(it),
                                        NavOptions.Builder().setLaunchSingleTop(true).build()
                                    )
                                }, { this@BookmarkedServicesActivity.finish() })
                            }

                            slideComposable<BookmarkedDetailedService> {
                                val viewModel: BookmarkedServicesViewModel =
                                    hiltViewModel(navController.getBackStackEntry<BookmarkedServices>())
                                BookmarkedDetailedServiceInfoScreen(
                                    navController,
                                    onNavigateUpSlider = {
                                        navController.navigate(
                                            BookmarkedImagesSliderDetailedService(
                                                it
                                            )
                                        )
                                    },
                                    navigateUpChat = { chatId, recipientId, feedUserProfile ->
                                        context.startActivity(
                                            Intent(context, IsolatedChatActivity::class.java)
                                                .apply {
                                                    putExtra(
                                                        "feed_user_profile",
                                                        UserProfileSerializer.serializeFeedUserProfileInfo(
                                                            feedUserProfile
                                                        )
                                                    )
                                                    putExtra("chat_id", chatId)
                                                    putExtra("recipient_id", recipientId)

                                                })
                                    },
                                    onNavigateUpForceJoinNow = {

                                    }, viewModel
                                )
                            }

                            slideComposable<BookmarkedImagesSliderDetailedService> {
                                val selectedImagePosition =
                                    it.toRoute<BookmarkedImagesSliderDetailedService>().selectedImagePosition

                                val viewModel: BookmarkedServicesViewModel =
                                    hiltViewModel(navController.getBackStackEntry<BookmarkedServices>())

                                val selectedItem by viewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedImagesSliderScreen(
                                        navController,
                                        selectedImagePosition,
                                        viewModel,
                                        { navController.popBackStack() }
                                    )
                                }
                            }

                            slideComposable<ServiceOwnerProfile> {

                                val viewModel: BookmarkedServicesViewModel =
                                    hiltViewModel(navController.getBackStackEntry<BookmarkedServices>())
                                val selectedItem by viewModel.selectedItem.collectAsState()
                                selectedItem?.let {
                                    BookmarkedServiceOwnerProfileScreen(
                                        navController,
                                        onNavigateUpChat = { chatUser, chatId, recipientId, feedUserProfile ->
                                            context.startActivity(
                                                Intent(context, IsolatedChatActivity::class.java)
                                                    .apply {
                                                        putExtra(
                                                            "feed_user_profile",
                                                            UserProfileSerializer.serializeFeedUserProfileInfo(
                                                                feedUserProfile
                                                            )
                                                        )
                                                        putExtra("chat_id", chatId)
                                                        putExtra("recipient_id", recipientId)

                                                    })
                                        },
                                        {
                                            navController.navigate(FeedUserDetailedService())
                                        }, viewModel
                                    )
                                }

                            }

                            slideComposable<FeedUserDetailedService> {
                                val viewModel: BookmarkedServicesViewModel =
                                    hiltViewModel(navController.getBackStackEntry<BookmarkedServices>())
                                val selectedItem by viewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedFeedUserDetailedServiceInfoScreen(
                                        navController,
                                        {
                                            navController.navigate(
                                                FeedUserImagesSliderDetailedService(it)
                                            )
                                        },
                                        { chatUser, chatId, recipientId, feedUserProfile ->

                                            context.startActivity(
                                                Intent(context, IsolatedChatActivity::class.java)
                                                    .apply {
                                                        putExtra(
                                                            "feed_user_profile",
                                                            UserProfileSerializer.serializeFeedUserProfileInfo(
                                                                feedUserProfile
                                                            )
                                                        )
                                                        putExtra("chat_id", chatId)
                                                        putExtra("recipient_id", recipientId)

                                                    })

                                        }, {

                                        }, viewModel
                                    )
                                }

                            }

                            slideComposable<FeedUserImagesSliderDetailedService> {
                                val selectedImagePosition =
                                    it.toRoute<FeedUserImagesSliderDetailedService>().selectedImagePosition
                                val viewModel: ServiceOwnerProfileViewModel =
                                    hiltViewModel(navController.getBackStackEntry<ServiceOwnerProfile>())
                                val selectedItem by viewModel.selectedItem.collectAsState()
                                selectedItem?.let {
                                    FeedUserImagesSliderScreen(
                                        navController,
                                        selectedImagePosition,
                                        viewModel,
                                        { navController.popBackStack() }
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

