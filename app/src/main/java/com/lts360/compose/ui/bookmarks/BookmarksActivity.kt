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
import com.lts360.compose.ui.main.navhosts.routes.BookMarkRoutes
import com.lts360.compose.ui.main.navhosts.routes.UserProfileSerializer
import com.lts360.compose.ui.main.profile.BookmarkedServiceOwnerProfileScreen
import com.lts360.compose.ui.services.BookmarkedDetailedServiceInfoScreen
import com.lts360.compose.ui.services.BookmarkedFeedUserDetailedServiceInfoScreen
import com.lts360.compose.ui.services.BookmarkedImagesSliderScreen
import com.lts360.compose.ui.services.FeedUserImagesSliderScreen
import com.lts360.compose.ui.services.ServiceOwnerProfileViewModel
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.usedproducts.BookmarkedDetailedUsedProductListingInfoScreen
import com.lts360.compose.ui.usedproducts.BookmarkedFeedUserDetailedUsedProductListingInfoScreen
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

                        val bookmarksViewModel: BookmarksViewModel = hiltViewModel()
                        val secondsOwnerProfileViewModel: SecondsOwnerProfileViewModel = hiltViewModel()
                        val servicesOwnerProfileViewModel: ServiceOwnerProfileViewModel = hiltViewModel()

                        val context = LocalContext.current

                        var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }

                        val navController = rememberBookMarksCustomBottomNavController(
                            lastEntry,
                            bookmarksViewModel.isSelectedBookmarkNull(),
                            secondsOwnerProfileViewModel.isSelectedUsedProductListingNull(),
                            servicesOwnerProfileViewModel.isSelectedServiceNull()
                        )

                        val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

                        LaunchedEffect(currentBackStackEntryAsState) {

                            lastEntry = navController.currentBackStackEntry?.destination?.route
                        }

                        NavHost(navController, BookMarkRoutes.BookmarkedServices) {

                            slideComposable<BookMarkRoutes.BookmarkedServices> {
                                BookmarksScreen(
                                    {
                                        navController.navigate(BookMarkRoutes.BookmarkedDetailedService)
                                    },
                                    {
                                        navController.navigate(
                                            BookMarkRoutes.ServiceOwnerProfile(it),
                                            NavOptions.Builder().setLaunchSingleTop(true).build()
                                        )
                                    },
                                    {
                                        navController.navigate(BookMarkRoutes.BookmarkedDetailedUsedProductListing)

                                    },
                                    {
                                        this@BookmarksActivity.finish()

                                    },
                                    bookmarksViewModel
                                )
                            }

                            slideComposable<BookMarkRoutes.BookmarkedDetailedService> {

                                BookmarkedDetailedServiceInfoScreen(
                                    navController,
                                    onNavigateUpSlider = {
                                        navController.navigate(
                                            BookMarkRoutes.BookmarkedDetailedServiceImagesSlider(
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

                                    }, bookmarksViewModel
                                )
                            }

                            slideComposable<BookMarkRoutes.BookmarkedDetailedServiceImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<BookMarkRoutes.BookmarkedDetailedServiceImagesSlider>().selectedImagePosition

                                val selectedItem by bookmarksViewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedImagesSliderScreen(
                                        navController,
                                        selectedImagePosition,
                                        bookmarksViewModel,
                                        { navController.popBackStack() }
                                    )
                                }
                            }

                            slideComposable<BookMarkRoutes.ServiceOwnerProfile> {

                                val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
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
                                            navController.navigate(BookMarkRoutes.DetailedServiceFeedUser())
                                        }, bookmarksViewModel,
                                        servicesOwnerProfileViewModel
                                    )
                                }

                            }

                            slideComposable<BookMarkRoutes.DetailedServiceFeedUser> {

                                val selectedItem by bookmarksViewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedFeedUserDetailedServiceInfoScreen(
                                        navController,
                                        {
                                            navController.navigate(
                                                BookMarkRoutes.DetailedServiceFeedUserImagesSlider(it)
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

                                        },
                                        bookmarksViewModel,
                                        servicesOwnerProfileViewModel
                                    )
                                }

                            }

                            slideComposable<BookMarkRoutes.DetailedServiceFeedUserImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<BookMarkRoutes.DetailedServiceFeedUserImagesSlider>().selectedImagePosition

                                val selectedItem by servicesOwnerProfileViewModel.selectedItem.collectAsState()
                                selectedItem?.let {
                                    FeedUserImagesSliderScreen(
                                        navController,
                                        selectedImagePosition,
                                        servicesOwnerProfileViewModel,
                                        { navController.popBackStack() }
                                    )
                                }

                            }


                            slideComposable<BookMarkRoutes.BookmarkedDetailedUsedProductListing> {

                                BookmarkedDetailedUsedProductListingInfoScreen(
                                    navController,
                                    onNavigateUpSlider = {
                                        navController.navigate(
                                            BookMarkRoutes.BookmarkedDetailedUsedProductListingImagesSlider(
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
                                            BookMarkRoutes.SecondsOwnerProfile(
                                                it
                                            )
                                        )
                                    },

                                    bookmarksViewModel
                                )
                            }

                            slideComposable<BookMarkRoutes.BookmarkedDetailedUsedProductListingImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<BookMarkRoutes.BookmarkedDetailedUsedProductListingImagesSlider>().selectedImagePosition

                                val selectedItem by bookmarksViewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedSecondsSliderScreen(
                                        navController,
                                        selectedImagePosition,
                                        bookmarksViewModel,
                                        { navController.popBackStack() }
                                    )
                                }
                            }

                            slideComposable<BookMarkRoutes.SecondsOwnerProfile> {
                                val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
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
                                            navController.navigate(BookMarkRoutes.DetailedSecondsFeedUser())
                                        }, bookmarksViewModel,
                                        secondsOwnerProfileViewModel
                                    )
                                }
                            }


                            slideComposable<BookMarkRoutes.DetailedSecondsFeedUser> {

                                val selectedItem by bookmarksViewModel.selectedItem.collectAsState()

                                selectedItem?.let {
                                    BookmarkedFeedUserDetailedUsedProductListingInfoScreen(
                                        navController,
                                        {
                                            navController.navigate(
                                                BookMarkRoutes.DetailedSecondsFeedUserImagesSlider(
                                                    it
                                                )
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
                                        }, bookmarksViewModel,
                                        secondsOwnerProfileViewModel
                                    )
                                }

                            }


                            slideComposable<BookMarkRoutes.DetailedSecondsFeedUserImagesSlider> {
                                val selectedImagePosition =
                                    it.toRoute<BookMarkRoutes.DetailedSecondsFeedUserImagesSlider>().selectedImagePosition

                                val selectedItem by secondsOwnerProfileViewModel.selectedItem.collectAsState()
                                selectedItem?.let {
                                    FeedUserSecondsImagesSliderScreen(
                                        navController,
                                        selectedImagePosition,
                                        secondsOwnerProfileViewModel,
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

