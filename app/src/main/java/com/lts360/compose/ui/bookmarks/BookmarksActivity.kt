package com.lts360.compose.ui.bookmarks

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.lts360.compose.ui.main.navhosts.routes.BookmarkedDetailedServiceImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.BookmarkedDetailedUsedProductListing
import com.lts360.compose.ui.main.navhosts.routes.BookmarkedDetailedUsedProductListingImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.BookmarkedServices
import com.lts360.compose.ui.main.navhosts.routes.DetailedSecondsFeedUser
import com.lts360.compose.ui.main.navhosts.routes.DetailedSecondsFeedUserImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.DetailedServiceFeedUser
import com.lts360.compose.ui.main.navhosts.routes.DetailedServiceFeedUserImagesSlider
import com.lts360.compose.ui.main.navhosts.routes.SecondsOwnerProfile
import com.lts360.compose.ui.main.navhosts.routes.ServiceOwnerProfile
import com.lts360.compose.ui.main.navhosts.routes.UserProfileSerializer
import com.lts360.compose.ui.main.profile.BookmarkedServiceOwnerProfileScreen
import com.lts360.compose.ui.services.BookmarkedDetailedServiceInfoScreen
import com.lts360.compose.ui.services.BookmarkedFeedUserDetailedServiceInfoScreen
import com.lts360.compose.ui.services.BookmarkedImagesSliderScreen
import com.lts360.compose.ui.services.FeedUserImagesSliderScreen
import com.lts360.compose.ui.services.ServiceOwnerProfileViewModel
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.usedproducts.BookmarkedDetailedUsedProductListingInfoScreen
import com.lts360.compose.ui.usedproducts.BookmarkedFeedUserUsedProductListingInfoScreen
import com.lts360.compose.ui.usedproducts.SecondsOwnerProfileViewModel
import com.lts360.compose.ui.usedproducts.manage.BookmarkedSecondsOwnerProfileScreen
import com.lts360.compose.ui.usedproducts.manage.BookmarkedSecondsSliderScreen
import com.lts360.compose.ui.usedproducts.manage.FeedUserSecondsImagesSliderScreen
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BookmarksActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {

                        val context = LocalContext.current

                        var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }

                        val navController =
                            rememberBookMarksCustomBottomNavController(lastEntry)

                        val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

                        LaunchedEffect(currentBackStackEntryAsState) {

                            lastEntry = navController.currentBackStackEntry?.destination?.route
                        }

                        NavHost(navController, BookmarkedServices) {

                            slideComposable<BookmarkedServices> {
                                BookmarksScreen({
                                    navController.navigate(BookmarkedDetailedService)
                                },
                                    {
                                        navController.navigate(
                                            ServiceOwnerProfile(it),
                                            NavOptions.Builder().setLaunchSingleTop(true).build()
                                        )
                                    },
                                    {
                                        navController.navigate(BookmarkedDetailedUsedProductListing)

                                    },
                                    { this@BookmarksActivity.finish() })
                            }

                            slideComposable<BookmarkedDetailedService> {
                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember {
                                        navController.getBackStackEntry<BookmarkedServices>()
                                    })
                                BookmarkedDetailedServiceInfoScreen(
                                    navController,
                                    onNavigateUpSlider = {
                                        navController.navigate(
                                            BookmarkedDetailedServiceImagesSlider(
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

                            slideComposable<BookmarkedDetailedServiceImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<BookmarkedDetailedServiceImagesSlider>().selectedImagePosition

                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember { navController.getBackStackEntry<BookmarkedServices>() })

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

                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember { navController.getBackStackEntry<BookmarkedServices>() })
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
                                            navController.navigate(DetailedServiceFeedUser())
                                        }, viewModel
                                    )
                                }

                            }

                            slideComposable<DetailedServiceFeedUser> {
                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember { navController.getBackStackEntry<BookmarkedServices>() })
                                val selectedItem by viewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedFeedUserDetailedServiceInfoScreen(
                                        navController,
                                        {
                                            navController.navigate(
                                                DetailedServiceFeedUserImagesSlider(it)
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

                            slideComposable<DetailedServiceFeedUserImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<DetailedServiceFeedUserImagesSlider>().selectedImagePosition


                                val viewModel: ServiceOwnerProfileViewModel =
                                    hiltViewModel(remember {
                                        navController.getBackStackEntry<ServiceOwnerProfile>()
                                    })
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


                            slideComposable<BookmarkedDetailedUsedProductListing> {
                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember { navController.getBackStackEntry<BookmarkedServices>() })
                                BookmarkedDetailedUsedProductListingInfoScreen(
                                    navController,
                                    onNavigateUpSlider = {
                                        navController.navigate(
                                            BookmarkedDetailedUsedProductListingImagesSlider(
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

                                    },
                                    {
                                        navController.navigate(
                                            SecondsOwnerProfile(
                                                it
                                            )
                                        )
                                    },

                                    viewModel
                                )
                            }

                            slideComposable<BookmarkedDetailedUsedProductListingImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<BookmarkedDetailedUsedProductListingImagesSlider>().selectedImagePosition

                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember { navController.getBackStackEntry<BookmarkedServices>() })

                                val selectedItem by viewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedSecondsSliderScreen(
                                        navController,
                                        selectedImagePosition,
                                        viewModel,
                                        { navController.popBackStack() }
                                    )
                                }
                            }

                            slideComposable<SecondsOwnerProfile> {

                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember { navController.getBackStackEntry<BookmarkedServices>() })
                                val selectedItem by viewModel.selectedItem.collectAsState()
                                selectedItem?.let {
                                    BookmarkedSecondsOwnerProfileScreen(
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
                                            navController.navigate(DetailedSecondsFeedUser())
                                        }, viewModel
                                    )
                                }

                            }

                            slideComposable<DetailedSecondsFeedUser> {
                                val viewModel: BookmarksViewModel =
                                    hiltViewModel(remember { navController.getBackStackEntry<BookmarkedServices>() })
                                val selectedItem by viewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedFeedUserUsedProductListingInfoScreen(
                                        navController,
                                        {
                                            navController.navigate(
                                                DetailedSecondsFeedUserImagesSlider(it)
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

                            slideComposable<DetailedSecondsFeedUserImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<DetailedSecondsFeedUserImagesSlider>().selectedImagePosition

                                val viewModel: SecondsOwnerProfileViewModel = hiltViewModel(remember {
                                    navController.getBackStackEntry<SecondsOwnerProfile>()
                                })

                                val selectedItem by viewModel.selectedItem.collectAsState()
                                selectedItem?.let {
                                    FeedUserSecondsImagesSliderScreen(
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

