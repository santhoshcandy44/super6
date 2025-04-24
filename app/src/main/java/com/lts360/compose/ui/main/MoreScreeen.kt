package com.lts360.compose.ui.main

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lts360.BuildConfig
import com.lts360.R
import com.lts360.app.database.models.app.Board
import com.lts360.components.utils.openUrlInCustomTab
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.shimmerLoadingAnimation
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.viewmodels.MoreViewModel
import androidx.core.net.toUri
import com.lts360.libs.ui.ShortToast


@Composable
fun MoreScreen(
    navController: NavHostController,
    boardItems: List<Board>,
    onProfileNavigateUp: () -> Unit,
    onAccountAndProfileSettingsNavigateUp: (String) -> Unit,
    onManageIndustriesAndInterestsNavigateUp: () -> Unit,
    onSetupBoardsSettingsNavigateUp: () -> Unit,
    onManageServiceNavigateUp: () -> Unit,
    onManageSecondsNavigateUp: () -> Unit,
    onManageLocalJobNavigateUp: () -> Unit,
    onNavigateUpBookmarks: () -> Unit,
    onNavigateUpThemeModeSettings: () -> Unit,
    onNavigateUpWelcomeScreenSheet: () -> Unit,
    onNavigateUpLogInSheet: () -> Unit,
    viewModel: MoreViewModel,
    onNavigateUpGuestManageIndustriesAndInterests: () -> Unit = {}
) {

    val isServicesEnabled = boardItems.any { it.boardLabel == "services" }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    BackHandler {

        val allowedScreens = listOf(BottomBar.Chats, BottomBar.Notifications, BottomBar.More)
        val hierarchy = navBackStackEntry?.destination?.hierarchy

        if (hierarchy?.any { nonNullDestination -> allowedScreens.any { nonNullDestination.hasRoute(it::class) } } == true) {

            navController.navigate(BottomBar.Home()) {
                launchSingleTop = true
                restoreState = true
                popUpTo(BottomBar.Home()) {
                    saveState = true
                }
            }
        } else {
            navController.popBackStack()
        }

    }


    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()

    val purpleGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6200EE),
            Color(0xFF9747ff),
            Color(0xFFBB86FC)
        )
    )

    val profilePicBitmap by viewModel.profileImageBitmap.collectAsState()


    val isGuest = viewModel.isGuest

    val lifecycleOwner = LocalLifecycleOwner.current


    Surface(modifier = Modifier.fillMaxSize()){
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(32.dp))

                if (isLoading) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.customColorScheme.shimmerContainer)
                                    .shimmerLoadingAnimation(),
                                contentAlignment = Alignment.Center
                            ) {}

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    "",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.customColorScheme.shimmerContainer)
                                        .shimmerLoadingAnimation(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .background(MaterialTheme.customColorScheme.shimmerContainer)
                                        .shimmerLoadingAnimation(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                } else {
                    // Profile Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)

                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (!isGuest) {
                                        dropUnlessResumedV2(lifecycleOwner) {
                                            onProfileNavigateUp()
                                        }
                                    }
                                }) {


                            profilePicBitmap?.let {


                                Image(
                                    it.asImageBitmap(),
                                    contentDescription = null,

                                    modifier = Modifier
                                        .size(60.dp)
                                        .border(
                                            width = 2.dp,
                                            purpleGradientBrush,
                                            shape = CircleShape
                                        )
                                        .padding(2.dp)
                                        .clip(CircleShape), // Fill the Box with the image
                                    contentScale = ContentScale.Crop // Crop the image to fit within the Box
                                )


                            }
                                ?: run {
                                    Image(
                                        painter = painterResource(R.drawable.user_placeholder),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .border(
                                                width = 2.dp,
                                                purpleGradientBrush,
                                                shape = CircleShape
                                            )
                                            .padding(2.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    "${userProfile?.firstName.orEmpty()} ${userProfile?.lastName.orEmpty()}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                if (isGuest) {
                                    Text(
                                        userProfile?.userId.toString(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Text(
                                        userProfile?.email.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isGuest) {
                        Column(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.customColorScheme.moreActionsContainerColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                        ) {

                            MoreSectionItem(
                                color = Color(
                                    0xFF007cf9
                                ),
                                iconRes = R.drawable.ic_light_add,
                                text = "Join Now"
                            ) {
                                onNavigateUpWelcomeScreenSheet()
                            }

                            MoreSectionItem(
                                color = Color(
                                    0xFF49d85b
                                ),
                                iconRes = R.drawable.ic_light_sign_now,
                                text = "Log In"
                            ) {
                                onNavigateUpLogInSheet()
                            }

                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Prefs", style = MaterialTheme.typography.titleMedium)

                        Column(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.customColorScheme.moreActionsContainerColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            MoreSectionItem(
                                color = MaterialTheme.colorScheme.primary,
                                iconRes = R.drawable.ic_setup_boards,
                                text = "Boards Settings"
                            ) {
                                onSetupBoardsSettingsNavigateUp()
                            }

                            MoreSectionItem(
                                color = Color.Red,
                                iconRes = R.drawable.ic_light_interest,
                                text = "Manage Service Industries"
                            ) {
                                onNavigateUpGuestManageIndustriesAndInterests()
                            }
                        }

                    } else {

                        Text(
                            "Account Management",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.customColorScheme.moreActionsContainerColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                        ) {

                            MoreSectionItem(
                                color = Color(
                                    0xFF007cf9
                                ),
                                iconRes = R.drawable.ic_light_login_user,
                                text = "Account and Profile Settings"
                            ) {

                                userProfile?.let {

                                    onAccountAndProfileSettingsNavigateUp(it.accountType)

                                }
                            }

                            MoreSectionItem(
                                color = Color(
                                    0xFF964B00
                                ),
                                iconRes = R.drawable.ic_light_bookmark,
                                text = "Bookmarks"
                            ) {

                                onNavigateUpBookmarks()

                            }

                        }

                        userProfile?.let {

                            if (it.accountType == AccountType.Business.name) {

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Business Tools & Settings", style = MaterialTheme.typography.titleMedium)

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(

                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.customColorScheme.moreActionsContainerColor,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    MoreSectionItem(
                                        color = Color(
                                            0xFF49d85b
                                        ),
                                        iconRes = R.drawable.ic_light_manage_services,
                                        text = "Manage Services"
                                    ) {
                                        onManageServiceNavigateUp()
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.customColorScheme.moreActionsContainerColor,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    MoreSectionItem(
                                        color = Color(
                                            0xFFfe9603
                                        ),
                                        iconRes = R.drawable.ic_light_manage_seconds,
                                        text = "Manage Seconds"
                                    ) {
                                        onManageSecondsNavigateUp()
                                    }
                                }


                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.customColorScheme.moreActionsContainerColor,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    MoreSectionItem(
                                        color = Color(0xFF7E57C2),
                                        iconRes = R.drawable.ic_light_manage_local_job,
                                        text = "Manage Local Jobs"
                                    ) {
                                        onManageLocalJobNavigateUp()
                                    }
                                }

                            }

                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Prefs", style = MaterialTheme.typography.titleMedium)

                        Column(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.customColorScheme.moreActionsContainerColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            MoreSectionItem(
                                color = MaterialTheme.colorScheme.primary,
                                iconRes = R.drawable.ic_setup_boards,
                                text = "Boards Settings"
                            ) {
                                onSetupBoardsSettingsNavigateUp()
                            }

                            if(isServicesEnabled){
                                MoreSectionItem(
                                    color = Color.Red,
                                    iconRes = R.drawable.ic_light_interest,
                                    text = "Manage Service Industries"
                                ) {

                                    onManageIndustriesAndInterestsNavigateUp()
                                }
                            }
                        }
                    }



                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Actions", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.customColorScheme.moreActionsContainerColor,
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        MoreSectionItem(
                            color = Color.Black,
                            iconRes = R.drawable.ic_light_settings, text = "Settings"
                        ) {
                            onNavigateUpThemeModeSettings()
                        }
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .background(
                                MaterialTheme.customColorScheme.moreActionsContainerColor,
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                    ) {


                        MoreSectionItem(
                            color = Color(0xFF3f7787),
                            iconRes = R.drawable.ic_light_invite_friends, text = "Invite Friends"
                        ) {

                            try {
                                val shareMessage = """
        Lts360 (Download It from PlayStore)

        https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
    """.trimIndent()

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Super6 App")
                                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                                }

                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Choose one"
                                    )
                                )
                            } catch (e: Exception) {
                                e.message?.let{
                                    ShortToast(context, it)
                                }
                            }
                        }

                        MoreSectionItem(
                            color = Color(0xFFff591f),
                            iconRes = R.drawable.ic_light_help, text = "Help and Support"
                        ) {
                            val emailIntent =
                                Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
                                putExtra(
                                        Intent.EXTRA_EMAIL,
                                        arrayOf(context.getString(R.string.help_and_support))
                                    )
                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "Help & Support"
                                    )
                                }

                            try {
                                context.startActivity(
                                    Intent.createChooser(
                                        emailIntent,
                                        "Send mail..."
                                    )
                                )
                            } catch (_: ActivityNotFoundException) {
                                ShortToast(context, "No email app installed")
                            }
                        }

                        MoreSectionItem(
                            color = Color(0xFFc14581),
                            iconRes = R.drawable.ic_light_privacy_policy,
                            text = "Privacy Policy"
                        ) {
                            openUrlInCustomTab(context, context.getString(R.string.privacy_policy))
                        }

                    }
                }
            }
        }
    }


}


@Composable
fun MoreSectionItem(
    iconRes: Int,
    color: Color = Color.Unspecified,
    text: String, onClick: () -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                dropUnlessResumedV2(lifecycleOwner) {
                    onClick()
                }
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(color, CircleShape)
                .size(32.dp),
            contentAlignment = Alignment.Center // Centers the Image inside the Box
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp) // Remove padding, size only
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}


