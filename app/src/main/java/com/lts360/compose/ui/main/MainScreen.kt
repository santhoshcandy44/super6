package com.lts360.compose.ui.main


import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lts360.R
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.components.utils.PermissionsUtils.Companion.isNotificationPermissionGranted
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.auth.ForceWelcomeScreen
import com.lts360.compose.ui.chat.viewmodels.ChatListViewModel
import com.lts360.compose.ui.main.navhosts.BottomNavHost
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.onboarding.ChooseIndustrySheet
import com.lts360.compose.ui.onboarding.GuestChooseIndustrySheet
import com.lts360.compose.ui.services.manage.ManageServicesActivity
import com.lts360.compose.ui.viewmodels.HomeActivityViewModel
import com.lts360.compose.ui.viewmodels.MoreViewModel
import com.lts360.compose.ui.viewmodels.NotificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    chatListViewModel: ChatListViewModel,
    notificationViewModel: NotificationViewModel,
    moreViewModel: MoreViewModel,
    viewModel: HomeActivityViewModel = hiltViewModel(),
    onProfileNavigateUp: () -> Unit,
    onAccountAndProfileSettingsNavigateUp: (String) -> Unit,
    onManageIndustriesAndInterestsNavigateUp: (Long, String?) -> Unit,
    onNavigateUpBookmarkedServices: () -> Unit,
    onNavigateUpGuestManageIndustriesAndInterests: () -> Unit,
    onNavigateUpChatWindow: (ChatUser, Int, Long, String) -> Unit,
    onNavigateUpUsedProductListing:()-> Unit
) {


    val userId = UserSharedPreferencesManager.userId

    val messageCount by viewModel.messageCount.collectAsState(0)
    val notificationCount by viewModel.notificationCount.collectAsState(0)


    // Remember the previous connection state with state preservation
    var wasConnected by rememberSaveable { mutableStateOf(false) }
    var isDismissed by rememberSaveable { mutableStateOf(false) } // State to track if the user dismissed the floating view

    // Observe the connectivity status using observeAsState
    val isConnected by viewModel.isConnectedEvent.collectAsState(null)


    val context = LocalContext.current


    val signInMethod = viewModel.signInMethod
//    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    val coroutineScope = rememberCoroutineScope()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        ))

    val selectedIndustriesCount by viewModel.selectedIndustriesCount.collectAsState()


    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }


    var sheetContent by rememberSaveable { mutableStateOf("") } // Local state for 'welcome'

    var isSheetIsClosedAlready by rememberSaveable { mutableStateOf(false) }

    // Monitor the bottom sheet state to reset 'welcome' when the sheet is collapsed
    LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.currentValue) {
        if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
            if (sheetContent.isNotEmpty()) {
                isSheetIsClosedAlready = true
            }
            sheetContent = ""
        }
    }


    var showDialog by remember {
        mutableStateOf(
            !viewModel.isNotificationPermissionDismissed()
                    && !isNotificationPermissionGranted(context)
                    && viewModel.isFeatureEnabled
        )
    }


    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setNotificationPermissionDismissed()
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    // Custom Alert Dialog
    if (showDialog) {
        NotificationPermissionAlertDialog(
            onDismiss = {
                viewModel.setNotificationPermissionDismissed()
                showDialog = false
            },
            onConfirm = {
                // Handle confirm action here
                showDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onCancel = {
                viewModel.setNotificationPermissionDismissed()
                // Handle cancel action here
                showDialog = false
            }
        )
    }


    if (signInMethod == "guest") {
        LaunchedEffect(selectedIndustriesCount) {
            if (selectedIndustriesCount == 0 && !viewModel.isIndustriesSheetDismissed() && !isSheetIsClosedAlready) {
                // Set the sheet content to "industries"
                sheetContent = "guest_industries"
            }
        }
    }

    LaunchedEffect(sheetContent) {
        // Check if the content is either "industries" or "welcome"
        if (sheetContent == "guest_industries" || sheetContent == "valid_user_industries" || sheetContent == "welcome") {
            // Expand the sheet
            bottomSheetScaffoldState.bottomSheetState.expand()
        }
    }

    var lastEntry by rememberSaveable { mutableStateOf<String?>(null) }

    val navController = rememberCustomBottomNavController(lastEntry)


    val allowedScreens = listOf(
        BottomBar.Home::class,
        BottomBar.NestedServices::class,
        BottomBar.Chats::class,
        BottomBar.Notifications::class,
        BottomBar.More::class
    )

    // Step 2: Get the list of allowed screens' qualified names
    val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }


    val currentBackStackEntry by navController.currentBackStackEntryAsState()


    LaunchedEffect(currentBackStackEntry) {
        lastEntry = navController.currentBackStackEntry?.destination?.route
    }


    // Step 5: Collect the bottom navigation visibility state from the ViewModel
    val bottomNavVisibility by viewModel.bottomNavVisibility.collectAsState()



    var isHomeScreen by rememberSaveable{
        mutableStateOf(false)
    }



    // Step 6: Use LaunchedEffect to update the visibility of the bottom bar based on the route
    LaunchedEffect(currentBackStackEntry) {
        // Step 3: Get the current route and clean it (remove path and query parameters)
        val currentRoute = currentBackStackEntry?.destination?.route

        val cleanedRoute = currentRoute
            ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
            ?.replace(Regex("\\?.*"), "")?.trim() // Remove query parameters and trim whitespace

        // Step 4: Check if the current route is in the allowed routes list
        val isAllowedBottomBar = cleanedRoute != null && allowedRoutes.contains(cleanedRoute)

        // If the route is allowed, update the bottom navigation visibility
        if (cleanedRoute != null) {
            viewModel.updateBottomNavVisibility(isAllowedBottomBar)
        }

        isHomeScreen = cleanedRoute != null && allowedScreens[0].qualifiedName.orEmpty() == cleanedRoute
    }

    var dockedFloatingActionButtonVisibility by rememberSaveable { mutableStateOf(false) } // Initially hidden


    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {

        BottomSheetScaffold(
            sheetContent = {

                if (sheetContent == "guest_industries" && selectedIndustriesCount == 0) {

                    Surface {
                        GuestChooseIndustrySheet(onDismissed = {
                            viewModel.setIndustriesSheetDismissed()
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }) {
                            viewModel.setIndustriesSheetDismissed()
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }
                    }

                }

                if (sheetContent == "valid_user_industries") {
                    Surface {
                        ChooseIndustrySheet(onDismissed = {
                            viewModel.setIndustriesSheetDismissed()
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }) {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                            onManageIndustriesAndInterestsNavigateUp(userId, "update_industries")
                        }
                    }

                }

                if (sheetContent == "welcome") {
                    ForceWelcomeScreen(
                        onLogInNavigate = {
                            context.startActivity(Intent(context, AuthActivity::class.java)
                                .apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    putExtra("force_type", "force_login")
                                })
                        }, onSelectAccountNavigate = {
                            context.startActivity(Intent(context, AuthActivity::class.java)
                                .apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    putExtra("force_type", "force_register")
                                }
                            )

                        }) {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }
                    }
                }

            },
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
            sheetDragHandle = null,
            sheetSwipeEnabled = true, // Allow gestures to hide/show bottom sheet
        ) { innerPadding ->



            Scaffold(
                floatingActionButton = {

                    if (isHomeScreen && dockedFloatingActionButtonVisibility) {
                        FloatingActionButton(onNavigateUpUsedProductListing,
                            shape = CircleShape,
                            modifier = Modifier.offset(y = 50.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                },

                floatingActionButtonPosition = FabPosition.Center,

                bottomBar = {


                    if (bottomNavVisibility) {
                        BottomBar(
                            navController,
                            signInMethod,
//                    scrollBehavior,
                            messageCount,
                            notificationCount,
                            {
                                sheetContent = "welcome"
                            }
                        )
                    }


                }) { contentPadding ->

                BottomNavHost(
                    homeViewModel,
                    chatListViewModel,
                    notificationViewModel,
                    moreViewModel,
                    navController,
                    modifier = Modifier.padding(contentPadding),
                    onProfileNavigateUp = {
                        onProfileNavigateUp()
                    },
                    onAccountAndProfileSettingsNavigateUp = { accountType ->
                        onAccountAndProfileSettingsNavigateUp(accountType)
                    },
                    onManageIndustriesAndInterestsNavigateUp = {
                        onManageIndustriesAndInterestsNavigateUp(userId, "update_industries")
                    },
                    onManageServiceNavigateUp = {
                        context.startActivity(Intent(context, ManageServicesActivity::class.java)
                            .apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            }
                        )
                    },

                    onNavigateUpBookmarkedServices = {
                        onNavigateUpBookmarkedServices()
                    },
                    {
                        sheetContent = "welcome"
                    },

                    {
                        context.startActivity(Intent(context, AuthActivity::class.java)
                            .apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                putExtra("force_type", "force_login")
                            })

                    },
                    (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded),
                    {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }
                    },
                    {
                        onNavigateUpGuestManageIndustriesAndInterests()
                    },
                    onNavigateUpChatWindow,
                    {

                        if (!isSheetIsClosedAlready) {
                            sheetContent = "valid_user_industries"
                        }
                    },{
                        dockedFloatingActionButtonVisibility = it
                    })
            }

            // Check if isConnected has a value
            isConnected?.let { connectionState ->


                when {
                    connectionState && !wasConnected -> {
                        ShowFloatingViewConnectionAvailable {
                            wasConnected = true
                            isDismissed = false
                        }

                        // If currently connected and previously was not connected

                    }

                    !connectionState && !isDismissed -> {
                        // If currently disconnected
                        ShowFloatingViewInternetDisconnected(
                            onDismiss = { /* Handle dismiss logic here */
                                isDismissed = true
                            }
                        )
                        wasConnected = false

                    }
                    // If already connected, do nothing
                }
            }

        }

    }

}


@Composable
fun ShowFloatingViewInternetDisconnected(onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        // Connection lost message composable
        ConnectionLostMessage(
            onDismiss = {
                isVisible = false
                onDismiss() // Call dismiss callback
            })
    }
}


@Composable
fun ShowFloatingViewConnectionAvailable(
    onDismiss: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(true) }

    // Animate visibility
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically(
            initialOffsetY = { -it }),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)) + slideOutVertically(
            targetOffsetY = { -it })
    ) {
        // Connection available message composable
        ConnectionAvailableMessage(
            /*  onDismiss = {
                  isVisible = false
                  onDismiss() // Call dismiss callback
              }*/
        )

        // Auto-dismiss after 2 seconds
        LaunchedEffect(Unit) {
            delay(2000L)
            isVisible = false
            onDismiss()
        }
    }
}


@Composable
fun ConnectionLostMessage(
    onDismiss: () -> Unit,
) {

    Box(
        modifier = Modifier

            .fillMaxWidth()
            .background(color = Color.Red)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {

                    },
                    onPress = {
                        // Consume the press to prevent the event from reaching the child
                    },
                    onTap = {
                        // No action on tap to prevent background click event
                    }
                )
            }

    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Row covers the entire box area

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Text message
            Text(
                text = "Your device is disconnected to the internet.",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            // Close button

            Icon(
                painter = painterResource(R.drawable.close), // Your close icon
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.clickable {
                    onDismiss() // Only the IconButton should trigger this

                }
            )


        }
    }

}


@Composable
fun ConnectionAvailableMessage(
    /*   onDismiss: () -> Unit*/
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {

                    },
                    onPress = {
                        // Consume the press to prevent the event from reaching the child
                    },
                    onTap = {
                        // No action on tap to prevent background click event
                    }
                )
            }


    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Text message
            Text(
                text = "Your device is connected to the internet.",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

        }
    }


}


@Composable
fun NotificationPermissionAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    // Custom AlertDialog content
    AlertDialog(

        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Allow Notifications",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "To receive important updates, please enable notifications for this app.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    painter = painterResource(id = R.drawable.allow_notifications),
                    contentDescription = "Notification Icon",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(text = "Cancel")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,  // Allow back press dismissal
            dismissOnClickOutside = false  // Prevent dismissal when clicking outside
        )
    )
}






