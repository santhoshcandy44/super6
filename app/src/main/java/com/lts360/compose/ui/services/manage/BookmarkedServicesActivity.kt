package com.lts360.compose.ui.services.manage

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.chat.IsolatedChatActivity
import com.lts360.compose.ui.main.navhosts.routes.BookmarkedDetailedService
import com.lts360.compose.ui.main.navhosts.routes.BookmarkedImagesSliderDetailedService
import com.lts360.compose.ui.main.navhosts.routes.BookmarkedServices
import com.lts360.compose.ui.main.navhosts.routes.FeedUserDetailedService
import com.lts360.compose.ui.main.navhosts.routes.FeedUserImagesSliderDetailedService
import com.lts360.compose.ui.main.navhosts.routes.ServiceOwnerProfile
import com.lts360.compose.ui.main.navhosts.routes.UserProfileSerializer
import com.lts360.compose.ui.main.profile.BookmarkedServiceOwnerProfileScreen
import com.lts360.compose.ui.services.BookmarkedDetailedServiceInfoScreen
import com.lts360.compose.ui.services.BookmarkedFeedUserDetailedServiceInfoScreen
import com.lts360.compose.ui.services.BookmarkedImagesSliderScreen
import com.lts360.compose.ui.services.FeedUserImagesSliderScreen
import com.lts360.compose.ui.services.ServiceOwnerProfileViewModel
import com.lts360.compose.ui.services.bookmark.BookmarkedServicesScreen
import com.lts360.compose.ui.services.bookmark.BookmarkedServicesViewModel
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
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

