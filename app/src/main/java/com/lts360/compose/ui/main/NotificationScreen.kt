package com.lts360.compose.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lts360.R
import com.lts360.app.database.models.notification.Notification
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.theme.icons
import com.lts360.compose.ui.viewmodels.NotificationViewModel
import com.lts360.compose.ui.viewmodels.NotificationViewModel.Companion.getTimeAgo



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavHostController,
                       viewModel: NotificationViewModel
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()



    BackHandler {

        val allowedScreens = listOf(BottomBar.Chats, BottomBar.Notifications, BottomBar.More)
        val hierarchy = navBackStackEntry?.destination?.hierarchy

        if (hierarchy?.any { nonNullDestination -> allowedScreens.any { nonNullDestination.hasRoute(it::class) } } == true) {
            // Navigate back to A and preserve its state
            navController.navigate(BottomBar.Home()) {
                launchSingleTop = true
                restoreState = true
                popUpTo(BottomBar.Home()) {
                    saveState = true
                }
            }
        } else {
            // Let the default back behavior occur
            navController.popBackStack()
        }
    }




    // Collect state from ViewModel

    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()


    // Initialize the ModalBottomSheetState with default value

    val sheetState = rememberModalBottomSheetState()

    val listState = rememberLazyListState() // Remember the scroll state

    var bottomSheetValue by remember{ mutableStateOf(false) }


    LaunchedEffect(bottomSheetValue) {
        if (bottomSheetValue) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }



    Scaffold(

        topBar = {
            TopAppBar(

                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleMedium
                    )
                },)
        },


        content = { paddingValues ->


            Box(
                modifier = Modifier
                    .fillMaxSize() // This makes the Box take up the entire available space
                    .padding(paddingValues) // Use the padding values provided by Scaffold

            ) {


                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center) // Centers the CircularProgressIndicator inside the Box
                    )
                } else {


                    if (notifications.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center // Center content within the Box

                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally

                            ) {

                                Image(
                                    painter = painterResource(R.drawable.no_notifications),
                                    contentDescription = "Image from drawable",
                                    modifier = Modifier
                                        .sizeIn(
                                            maxWidth = 200.dp,
                                            maxHeight = 200.dp
                                        )
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(text = "No, notifications")

                            }

                        }


                    } else {

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize(),

                        ) {

                            if (notifications.any { it.status == "un_read" }) {
                                item {
                                    // "Mark All as Read" Button
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.End // Aligns the button to the end
                                    ) {
                                        Text(
                                            text = "Mark All as Read",
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.clickable {
                                                viewModel.markAllAsRead()
                                            }
                                        )

                                    }
                                }
                            }



                            itemsIndexed(
                                notifications,
                            ) { index, notification ->

                                NotificationCard(notification, index) {
                                    bottomSheetValue=true
                                    viewModel.setSelectedItem(notification)
                                }
                            }
                        }
                    }
                }

            }


            if (bottomSheetValue) {

                ModalBottomSheet(
                    modifier = Modifier
                        .safeDrawingPadding()
                        .padding(16.dp),
                    onDismissRequest = {
                        bottomSheetValue=false
                    },
                    sheetState = sheetState,
                    dragHandle = null,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    selectedItem?.let { nonNullSelectedItem ->

                        Column(modifier = Modifier.fillMaxWidth()) {



                            if(nonNullSelectedItem.status=="un_read"){
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.markAsRead(nonNullSelectedItem)
                                            bottomSheetValue=false

                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                                ) {


                                    Icon(
                                        painter = painterResource(
                                            MaterialTheme.icons.notificationMarkAsRead
                                        ),
                                        contentDescription = "Mark as Read",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )

                                    Text(text = "Mark as Read",
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        viewModel.deleteNotification(nonNullSelectedItem)
                                        bottomSheetValue=false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {


                                // Bookmark Icon
                                Icon(
                                    painter = painterResource(
                                        MaterialTheme.icons.deleteNotification
                                    ),
                                    contentDescription = "Bookmark",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )

                                // Text
                                Text(text = "Delete",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                    }


                }

            }


        })


}


@Composable
fun NotificationCard(
    notification: Notification,
    position: Int,
    onMoreOptionClick: (Int) -> Unit,
) {

    // Determine background color based on notification status
    val backgroundColor = if (notification.status == "un_read") {
        Color(0xFFDEECFF).copy(alpha = 0.2f) // Example for unread color
    } else {
       MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                // Notification icon
                Box(
                    modifier = Modifier
                        .size(32.dp) // Adjust size based on padding and drawable dimensions
                        .background(Color(0xFFC7F6C7), shape = CircleShape) // Oval shape with color
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_general_notification_message),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center) // Center the icon
                    )
                }


                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    // Title
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Message
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Time aligned to the end
                    Text(
                        text = getTimeAgo(notification.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            // More option icon with click handling
            IconButton(
                onClick = { onMoreOptionClick(position) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vertical_dots),
                    contentDescription = "More options"
                )
            }
        }
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.8.dp)
            .background(Color.LightGray)
    )

}
