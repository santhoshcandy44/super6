package com.lts360.compose.ui.bookmarks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.compose.ui.chat.IsolatedChatActivity
import com.lts360.compose.ui.localjobs.BookmarkedDetailedLocalJobInfoScreen
import com.lts360.compose.ui.localjobs.manage.BookmarkedLocalJobsImagesSliderScreen
import com.lts360.compose.ui.main.navhosts.routes.BookMarkRoutes
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
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

object UserProfileSerializer {

    fun serializeFeedUserProfileInfo(feedUserProfile: FeedUserProfileInfo): String {
        return Json.encodeToString(feedUserProfile)
    }

    fun deserializeFeedUserProfile(serialized: String): FeedUserProfileInfo {
        return Json.decodeFromString(serialized)
    }
}

class BookmarksActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {
                        val bookmarksViewModel: BookmarksViewModel = koinViewModel()
                        val secondsOwnerProfileViewModel: SecondsOwnerProfileViewModel =
                            koinViewModel()
                        val servicesOwnerProfileViewModel: ServiceOwnerProfileViewModel =
                            koinViewModel()
                        val context = LocalContext.current

                        val backStack = rememberNavBackStack(BookMarkRoutes.BookmarkedServices)

                        NavDisplay(
                            backStack = backStack,
                            entryDecorators = listOf(
                                rememberSceneSetupNavEntryDecorator(),
                                rememberSavedStateNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            entryProvider = entryProvider {
                                entry<BookMarkRoutes.BookmarkedServices> {
                                    BookmarksScreen(
                                        {
                                            backStack.add(BookMarkRoutes.BookmarkedDetailedService)
                                        },
                                        {
                                            backStack.add(BookMarkRoutes.ServiceOwnerProfile(it))
                                        },
                                        {
                                            backStack.add(BookMarkRoutes.BookmarkedDetailedUsedProductListing)
                                        },
                                        {
                                            backStack.add(BookMarkRoutes.BookmarkedDetailedLocalJob)
                                        },
                                        {
                                            (context as Activity).finish()
                                        },
                                        bookmarksViewModel
                                    )
                                }

                                entry<BookMarkRoutes.BookmarkedDetailedService> {
                                    BookmarkedDetailedServiceInfoScreen(
                                        {
                                            backStack.add(
                                                BookMarkRoutes.BookmarkedDetailedServiceImagesSlider(
                                                    it
                                                )
                                            )
                                        },
                                        { chatId, recipientId, feedUserProfile ->
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    IsolatedChatActivity::class.java
                                                ).apply {
                                                    putExtra(
                                                        "feed_user_profile",
                                                        UserProfileSerializer.serializeFeedUserProfileInfo(
                                                            feedUserProfile
                                                        )
                                                    )
                                                    putExtra("chat_id", chatId)
                                                    putExtra(
                                                        "recipient_id",
                                                        recipientId
                                                    )
                                                })
                                        },
                                        {backStack.removeLastOrNull()},
                                        bookmarksViewModel
                                    )
                                }

                                entry<BookMarkRoutes.BookmarkedDetailedServiceImagesSlider> {
                                    val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
                                    selectedItem?.let { nonNullSelectedItem ->
                                        BookmarkedImagesSliderScreen(
                                            it.selectedImagePosition,
                                            bookmarksViewModel
                                        ) { backStack.removeLastOrNull() }
                                    }
                                }

                                entry<BookMarkRoutes.ServiceOwnerProfile> {
                                    val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        BookmarkedServiceOwnerProfileScreen(
                                            { chatId, recipientId, feedUserProfile ->
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        IsolatedChatActivity::class.java
                                                    ).apply {
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
                                                backStack.add(BookMarkRoutes.DetailedServiceFeedUser())
                                            },
                                            { backStack.removeLastOrNull() },
                                            bookmarksViewModel,
                                            servicesOwnerProfileViewModel
                                        )
                                    }
                                }

                                entry<BookMarkRoutes.DetailedServiceFeedUser> {
                                    val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        BookmarkedFeedUserDetailedServiceInfoScreen(
                                            {
                                                backStack.add(
                                                    BookMarkRoutes.DetailedServiceFeedUserImagesSlider(
                                                        it
                                                    )
                                                )
                                            },
                                            { chatId, recipientId, feedUserProfile ->
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        IsolatedChatActivity::class.java
                                                    ).apply {
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
                                                backStack.removeLastOrNull()
                                            },
                                            servicesOwnerProfileViewModel
                                        )
                                    }
                                }

                                entry<BookMarkRoutes.DetailedServiceFeedUserImagesSlider> { navEntry ->
                                    val selectedItem by servicesOwnerProfileViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        FeedUserImagesSliderScreen(
                                            navEntry.selectedImagePosition,
                                            servicesOwnerProfileViewModel
                                        ) { backStack.removeLastOrNull() }
                                    }
                                }

                                entry<BookMarkRoutes.BookmarkedDetailedUsedProductListing> {
                                    BookmarkedDetailedUsedProductListingInfoScreen(
                                        {
                                            backStack.add(
                                                BookMarkRoutes.BookmarkedDetailedUsedProductListingImagesSlider(
                                                    it
                                                )
                                            )
                                        },
                                        { chatId, recipientId, feedUserProfile ->
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    IsolatedChatActivity::class.java
                                                ).apply {
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
                                            backStack.add(BookMarkRoutes.SecondsOwnerProfile(it))
                                        },
                                        {
                                            backStack.removeLastOrNull()
                                        },
                                        bookmarksViewModel
                                    )
                                }

                                entry<BookMarkRoutes.BookmarkedDetailedUsedProductListingImagesSlider> { navEntry ->

                                    val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        BookmarkedSecondsSliderScreen(
                                            navEntry.selectedImagePosition,
                                            bookmarksViewModel
                                        ) { backStack.removeLastOrNull() }
                                    }
                                }

                                entry<BookMarkRoutes.SecondsOwnerProfile> {
                                    val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        BookmarkedSecondsOwnerProfileScreen(
                                            { chatId, recipientId, feedUserProfile ->
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        IsolatedChatActivity::class.java
                                                    ).apply {
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
                                                backStack.add(BookMarkRoutes.DetailedSecondsFeedUser())
                                            },
                                            { backStack.removeLastOrNull() },
                                            bookmarksViewModel,
                                            secondsOwnerProfileViewModel
                                        )
                                    }
                                }

                                entry<BookMarkRoutes.DetailedSecondsFeedUser> {
                                    val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        BookmarkedFeedUserDetailedUsedProductListingInfoScreen(
                                            {
                                                backStack.add(
                                                    BookMarkRoutes.DetailedSecondsFeedUserImagesSlider(
                                                        it
                                                    )
                                                )
                                            },
                                            { chatId, recipientId, feedUserProfile ->
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        IsolatedChatActivity::class.java
                                                    ).apply {
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
                                                backStack.removeLastOrNull()
                                            },
                                            secondsOwnerProfileViewModel
                                        )
                                    }
                                }

                                entry<BookMarkRoutes.DetailedSecondsFeedUserImagesSlider> { navEntry ->
                                    val selectedItem by secondsOwnerProfileViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        FeedUserSecondsImagesSliderScreen(
                                            navEntry.selectedImagePosition,
                                            secondsOwnerProfileViewModel
                                        ) { backStack.removeLastOrNull() }
                                    }
                                }

                                entry<BookMarkRoutes.BookmarkedDetailedLocalJob> {
                                    BookmarkedDetailedLocalJobInfoScreen(
                                        {
                                            backStack.add(
                                                BookMarkRoutes.BookmarkedDetailedLocalJobImagesSlider(
                                                    it
                                                )
                                            )
                                        },
                                        { chatId, recipientId, feedUserProfile ->
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    IsolatedChatActivity::class.java
                                                ).apply {
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
                                        { backStack.removeLastOrNull() },
                                        bookmarksViewModel
                                    )
                                }

                                entry<BookMarkRoutes.BookmarkedDetailedLocalJobImagesSlider> { navEntry ->
                                    val selectedItem by bookmarksViewModel.selectedItem.collectAsState()
                                    selectedItem?.let {
                                        BookmarkedLocalJobsImagesSliderScreen(
                                            navEntry.selectedImagePosition,
                                            bookmarksViewModel
                                        ) { backStack.removeLastOrNull() }
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

