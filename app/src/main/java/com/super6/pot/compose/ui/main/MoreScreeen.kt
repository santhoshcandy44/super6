package com.super6.pot.compose.ui.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.super6.pot.BuildConfig
import com.super6.pot.R
import com.super6.pot.components.utils.openUrlInCustomTab
import com.super6.pot.compose.dropUnlessResumedV2
import com.super6.pot.compose.ui.auth.AccountType
import com.super6.pot.compose.ui.main.navhosts.routes.BottomBarScreen
import com.super6.pot.compose.ui.shimmerLoadingAnimation
import com.super6.pot.compose.ui.theme.customColorScheme
import com.super6.pot.compose.ui.viewmodels.MoreViewModel


@Composable
fun MoreScreen(
    navController: NavHostController,
    onProfileNavigateUp: () -> Unit,
    onAccountAndProfileSettingsNavigateUp: (String) -> Unit,
    onManageIndustriesAndInterestsNavigateUp: () -> Unit,
    onManageServiceNavigateUp: () -> Unit,
    onNavigateUpBookmarkedServices: () -> Unit,
    viewModel: MoreViewModel,
    onNavigateUpWelcomeScreenSheet: () -> Unit,
    onNavigateUpLogInSheet: () -> Unit,
    onNavigateUpGuestManageIndustriesAndInterests: () -> Unit = {},
    isSheetExpanded: Boolean,
    collapseSheet: () -> Unit,
) {


    val navBackStackEntry by navController.currentBackStackEntryAsState()


    val currentRoute = navBackStackEntry?.destination?.route

// Clean the current route by removing path and query parameters
    val cleanedRoute = currentRoute
        ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
        ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace



    BackHandler {
        if (isSheetExpanded) {
            collapseSheet()
        } else {
            if (cleanedRoute in listOf(
                    BottomBarScreen.Chats::class.qualifiedName.orEmpty(),
                    BottomBarScreen.Notifications::class.qualifiedName.orEmpty(),
                    BottomBarScreen.More::class.qualifiedName.orEmpty()
                )
            ) {

                // Navigate back to A and preserve its state
                navController.navigate(BottomBarScreen.Home()) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(BottomBarScreen.Home()) {
                        saveState = true
                    }
                }
            } else {
                // Let the default back behavior occur
                navController.popBackStack()
            }
        }

    }


    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()

    // Initialize the ModalBottomSheetState with default value


    // Define a gradient brush
    val purpleGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6200EE),
            Color(0xFF9747ff),
            Color(0xFFBB86FC)
        )
    )

    val profilePicBitmap by viewModel.profileImageBitmap.collectAsState()


    val signInMethod = viewModel.signInMethod

    val lifecycleOwner = LocalLifecycleOwner.current


    Scaffold(

        /*   topBar = {
               TopAppBar( title = { Text(text = "More",
                           style = MaterialTheme.typography.titleMedium) })
           },
   */

        content = { paddingValues ->


            Box(
                modifier = Modifier
                    .fillMaxSize() // This makes the Box take up the entire available space
                    .padding(paddingValues) // Use the padding values provided by Scaffold
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    if (isLoading) {
                        // Profile Section
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
                                // Profile Image
                                Box {

                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.customColorScheme.shimmerContainer)
                                            .shimmerLoadingAnimation(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                    }
                                }

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

                                        if (signInMethod == "guest") {


                                        } else {
                                            dropUnlessResumedV2(lifecycleOwner) {
                                                onProfileNavigateUp()
                                            }
                                        }
                                    }) {
                                // Profile Image
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

                                    if (signInMethod == "guest") {
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

                        if (signInMethod == "guest") {
                            Column(
                                modifier = Modifier
                                    .background(MaterialTheme.customColorScheme.moreActionsContainerColor)
                            ) {
                                // Settings Item
                                AccountManagementItem(
                                    iconRes = R.drawable.ic_add,
                                    text = "Join Now"
                                ) {
                                    onNavigateUpWelcomeScreenSheet()
                                }

                                AccountManagementItem(
                                    iconRes = R.drawable.ic_sign_now,
                                    text = "Log In"
                                ) {
                                    onNavigateUpLogInSheet()
                                }

                                AccountManagementItem(
                                    iconRes = R.drawable.ic_interest,
                                    text = "Manage Industries and Interests"
                                ) {

                                    onNavigateUpGuestManageIndustriesAndInterests()
                                }
                            }
                        } else {
                            // Account Management Section
                            Text(
                                "Account Management",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .background(MaterialTheme.customColorScheme.moreActionsContainerColor)
                            ) {
                                // Settings Item
                                AccountManagementItem(
                                    iconRes = R.drawable.ic_settings,
                                    text = "Account and Profile Settings"
                                ) {

                                    userProfile?.let {

                                        onAccountAndProfileSettingsNavigateUp(it.accountType)

                                    }
                                }

                                AccountManagementItem(
                                    iconRes = R.drawable.ic_bookmark,
                                    text = "Bookmarked Services"
                                ) {

                                    onNavigateUpBookmarkedServices()

                                }
                                AccountManagementItem(
                                    iconRes = R.drawable.ic_interest,
                                    text = "Manage Industries and Interests"
                                ) {

                                    onManageIndustriesAndInterestsNavigateUp()
                                }
                            }

                            userProfile?.let {

                                if (it.accountType == AccountType.Business.name) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Actions Section
                                    Text(
                                        "Business Tools & Settings",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(
                                        modifier = Modifier
                                            .background(MaterialTheme.customColorScheme.moreActionsContainerColor)
                                    ) {
                                        AccountManagementItem(
                                            iconRes = R.drawable.ic_manage_services,
                                            text = "Manage Services"
                                        ) {
                                            onManageServiceNavigateUp()
                                        }
                                    }
                                }

                            }
                        }


                        Spacer(modifier = Modifier.height(8.dp))

                        // Actions Section
                        Text("Actions", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.customColorScheme.moreActionsContainerColor)
                        ) {

                            ActionItem(iconRes = R.drawable.ic_invite, text = "Invite Friends") {

                                try {
                                    val shareMessage = """
        Super6 (Download It from PlayStore)

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
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }
                            }

                            ActionItem(iconRes = R.drawable.ic_help, text = "Help and Support") {
                                val emailIntent =
                                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                                        putExtra(
                                            Intent.EXTRA_EMAIL,
                                            arrayOf("mobiblogger95@gmail.com")
                                        )
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Video Downloader for Twitter Application Feedback"
                                        )
                                    }

                                try {
                                    context.startActivity(
                                        Intent.createChooser(
                                            emailIntent,
                                            "Send mail..."
                                        )
                                    )
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        context,
                                        "No email app installed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            ActionItem(
                                iconRes = R.drawable.ic_privacy_policy,
                                text = "Privacy Policy"
                            ) {

                                openUrlInCustomTab(context, "https://www.super6.com")
                            }


                        }

                    }


                }


            }


        })

}


@Composable
fun AccountManagementItem(iconRes: Int, text: String, onClick: () -> Unit) {


    val lifecycleOwner = LocalLifecycleOwner.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                dropUnlessResumedV2(lifecycleOwner) {
                    onClick()
                }

            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }

}

@Composable
fun ActionItem(iconRes: Int, text: String, onClick: () -> Unit) {

    val lifecycleOwner = LocalLifecycleOwner.current


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                dropUnlessResumedV2(lifecycleOwner) {
                    onClick()
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}