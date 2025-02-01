package com.super6.test

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.BottomSheetValue.Expanded
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ProcessLifecycleOwner
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.super6.pot.R
import com.super6.pot.ui.SimCardInfo
import com.super6.pot.ui.SimCardSubscription
import com.super6.pot.ui.chat.ChatActivity
import com.super6.pot.ui.chat.ChatScreen
import com.super6.pot.ui.chat.ChatUsersScreen
import com.super6.pot.ui.chat.formatTimeSeconds
import com.super6.pot.ui.getMiddleVideoThumbnail
import com.super6.pot.ui.isUriExist
import com.super6.pot.ui.theme.AppTheme
import com.super6.pot.utils.LogUtils.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue


@Serializable
data class RCSChatWindow(val senderAddress: String)

@Serializable
data object RCSMessage

@AndroidEntryPoint
class TestActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val bottomSheetScaffoldState =
                androidx.compose.material.rememberBottomSheetScaffoldState(
                    bottomSheetState = rememberBottomSheetState(Expanded)
                )

            val coroutineScope = rememberCoroutineScope()


            BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == BottomSheetValue.Expanded) {
                coroutineScope.launch {
                    bottomSheetScaffoldState.bottomSheetState.collapse()
                }
            }



            var isShowingDialog by rememberSaveable {  mutableStateOf(true)  }

            AppTheme {

                Surface(Modifier.safeDrawingPadding()) {
                    Scaffold { cp ->
                        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
                            .fillMaxSize()

                            .padding(cp).background(Color.Red)){
                            Text("WOW")

                        }

                    }
                }



              /*  Surface {
                    PinVerificationAppOpenScreen()
                }*/
/*

                if(isShowingDialog){

                    BasicAlertDialog(onDismissRequest = {
                        isShowingDialog = false
                    },
                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)),
                    ) {
                        Surface{
                            Column(modifier = Modifier.fillMaxWidth()
                                .padding(8.dp)){
                                Text("Complete your profile before proceed", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(painterResource(R.drawable.all_caught_up) , contentDescription = null,  modifier = Modifier.align(Alignment.CenterHorizontally))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Unlock creating new services to show case and listing out.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))

                                Button({

                                }, shape = RectangleShape, modifier = Modifier.fillMaxWidth()) {
                                    Text("Complete")
                                }
                            }
                        }
                    }

                }
*/


/*
                val smsViewModel : SmsViewModel =  hiltViewModel()
*/
                Surface {


                    /*    val controller = rememberNavController()

                        NavHost(controller,RCSMessage) {

                            composable<RCSMessage> {
                                PermissionWrapper(smsViewModel){
                                    controller.navigate(RCSChatWindow(it))
                                }
                            }
                            composable<RCSChatWindow>{
                                val args= it.toRoute<RCSChatWindow>()
                                SMSChatWindowScreen(smsViewModel, args.senderAddress)
                            }
                        }*/


                    /*
                                        NavHost(rememberNavController(), BottomBarScreen.Home()) {

                                            noTransitionComposable<BottomBarScreen.Home>(){

                                                val context = LocalContext.current
                                                SharedTransitionLayout (modifier = Modifier.fillMaxSize()){
                                                    var isExpanded by remember { mutableStateOf(false) }
                                                    val boundsTransform = { _: Rect, _: Rect -> tween<Rect>(550) }

                                                    AnimatedContent(targetState = isExpanded, label = "") { target ->
                                                        if (!target) {
                                                            LazyColumn(modifier = Modifier.fillMaxSize()) {

                                                                item(key = "sa"){
                                                                    val imageRequest = remember("https://static.vecteezy.com/system/resources/thumbnails/022/654/478/small_2x/generative-ai-illustration-of-valentine-day-background-love-romantic-concept-heart-shape-neural-network-generated-art-digitally-generated-image-not-based-on-any-actual-scene-or-pattern-photo.jpg") {
                                                                        ImageRequest.Builder(context)
                                                                            .data("https://static.vecteezy.com/system/resources/thumbnails/022/654/478/small_2x/generative-ai-illustration-of-valentine-day-background-love-romantic-concept-heart-shape-neural-network-generated-art-digitally-generated-image-not-based-on-any-actual-scene-or-pattern-photo.jpg")
                                                                            .placeholder(R.drawable.user_placeholder) // Your placeholder image
                                                                            .error(R.drawable.user_placeholder)
                                                                            .crossfade(true)
                                                                            .build()
                                                                    }

                                                                    Card(
                                                                        onClick = dropUnlessResumed {

                                                                        },
                                                                        modifier = Modifier
                                                                            .fillMaxWidth(),
                                                                        elevation = CardDefaults.cardElevation(0.dp),
                                                                        shape = RectangleShape, // Remove rounded corners
                                                                    ) {
                                                                        Row(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .padding(horizontal = 8.dp, vertical = 16.dp),
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            // Profile image with online status dot
                                                                            Box(modifier = Modifier.size(50.dp)) {


                                                                                AsyncImage(
                                                                                    imageRequest,
                                                                                    contentDescription = "User Profile Image",
                                                                                    modifier = Modifier
                                                                                        .size(50.dp)
                                                                                        .sharedElement(
                                                                                            state = rememberSharedContentState(key = "image"),
                                                                                            animatedVisibilityScope = this@AnimatedContent,
                                                                                            boundsTransform = boundsTransform,
                                                                                        )
                                                                                        .clip(CircleShape).clickable {
                                                                                            isExpanded = !isExpanded
                                                                                        },
                                                                                    contentScale = ContentScale.Crop
                                                                                )

                                                                            }

                                                                            Spacer(modifier = Modifier.width(8.dp))

                                                                            // Name and message layout
                                                                            Column(
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .padding(horizontal = 8.dp)
                                                                            ) {
                                                                                // Row for user name and time
                                                                                Row(
                                                                                    modifier = Modifier
                                                                                        .fillMaxWidth()
                                                                                        .padding(horizontal = 8.dp),
                                                                                    verticalAlignment = Alignment.CenterVertically
                                                                                ) {

                                                                                }

                                                                                Spacer(modifier = Modifier.height(8.dp))

                                                                                // Row for last message and unread message count
                                                                                Row(
                                                                                    modifier = Modifier
                                                                                        .fillMaxWidth()
                                                                                        .padding(horizontal = 8.dp),
                                                                                    verticalAlignment = Alignment.CenterVertically
                                                                                ) {

                                                                                    Row(
                                                                                        modifier = Modifier

                                                                                            .weight(1f),
                                                                                        verticalAlignment = Alignment.CenterVertically
                                                                                    ) {



                                                                                    }


                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                            }

                                                        }else{
                                                            Box(modifier = Modifier.fillMaxSize()){
                                                                val imageRequest = remember("https://static.vecteezy.com/system/resources/thumbnails/022/654/478/small_2x/generative-ai-illustration-of-valentine-day-background-love-romantic-concept-heart-shape-neural-network-generated-art-digitally-generated-image-not-based-on-any-actual-scene-or-pattern-photo.jpg") {
                                                                    ImageRequest.Builder(context)
                                                                        .data("https://static.vecteezy.com/system/resources/thumbnails/022/654/478/small_2x/generative-ai-illustration-of-valentine-day-background-love-romantic-concept-heart-shape-neural-network-generated-art-digitally-generated-image-not-based-on-any-actual-scene-or-pattern-photo.jpg")
                                                                        .placeholder(R.drawable.user_placeholder) // Your placeholder image
                                                                        .error(R.drawable.user_placeholder)
                                                                        .crossfade(true)
                                                                        .build()
                                                                }


                                                                AsyncImage(
                                                                    imageRequest,
                                                                    contentDescription = "User Profile Image",
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .sharedElement(
                                                                            state = rememberSharedContentState(key = "image"),
                                                                            animatedVisibilityScope = this@AnimatedContent,
                                                                            boundsTransform = boundsTransform,
                                                                        )
                                                                        .clickable {
                                                                            isExpanded = !isExpanded

                                                                        }.align(Alignment.Center),
                                                                    contentScale = ContentScale.Fit
                                                                )


                                                            }
                                                        }
                                                    }

                                                }

                                            }

                                        }
                    */


                    /*
                                        // A surface container using the 'background' color from the theme
                                        DefaultPreview()*/

                    /*       // Initialize pager state to track selected tab index
                           val scope = rememberCoroutineScope()

                           // List of tab titles
                           val tabs = listOf("Tab 1", "Tab 2", "Tab 3", "Tab 4", "Tab 5")
                           val pagerState = rememberPagerState(pageCount = { tabs.size })

                           // WheelTabRow (or CenteredWheelTabRow) to handle tab selection
                           WheelTabRow(pagerState = pagerState, tabs = tabs, scope = scope)
       */
                    /* CameraXApp()*/
                    /*                    androidx.compose.material.BottomSheetScaffold(
                                            scaffoldState = bottomSheetScaffoldState,
                                            sheetContent = {

                                                Surface {
                                                    GalleyPager()
                                                }

                                            },
                                            sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
                                            sheetGesturesEnabled = false, // Allow gestures to hide/show bottom sheet
                    //            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                                        ) { innerPadding ->

                                            Scaffold { nestedInnerPadding ->

                                                Box(modifier = Modifier.padding(nestedInnerPadding)){

                                                }

                                            }


                                        }*/

                }

            }
        }
    }
}


// Tamil month names
val tamilMonths = listOf(
    "சித்திரை", "வைகாசி", "ஆணி", "ஆடீ", "ஆவணி", "புரட்டாசி",
    "ஓகோ", "கும்பம்", "புரிபூ", "மாசி", "பங்குனி"
)


@Composable
fun CalendarScreen() {

    // Pager state
    val pagerState = rememberPagerState(pageCount = { tamilMonths.size })

    Column(modifier = Modifier.fillMaxSize()) {
        // Background image of the devotional god

        AsyncImage(
            "https://i.pinimg.com/236x/bd/1b/c4/bd1bc47e6d4ae2db1d56da511a5bebd7.jpg", // Make sure your image is in drawable folder
            contentDescription = "Devotional God",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)

        )


        // Calendar Pages using Pager
        HorizontalPager(
            pageSpacing = 8.dp,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) { page ->
            CalendarPage(page, month = tamilMonths[page], pagerState)
        }
    }
}


@Composable
fun CalendarPage(page: Int, month: String, pagerState: PagerState) {
    // Month details (could also add events or festivals)

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {


        AsyncImage("https://srirangaminfo.com/cal/2024/2401.jpg",
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true,
                ) {
                    scope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
                .graphicsLayer {
                    val pageOffSet = ((pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                            ).absoluteValue

                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                    )
                    /* scaleY =   lerp(
                         start = 0.75f,
                         stop = 1f,
                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                     )*/
                }
        )

    }
}


@Composable
fun MyComposable() {
    val tabsList = listOf("Songs", "Albums", "Artists", "Genres", "Dates", "Folders")

    var selectedTabIndex by remember { mutableStateOf(0) }

    // Track tab widths dynamically
    val tabWidths = remember { mutableStateListOf<Float>() }

    val screenWidthPx =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    var scrollPosition by remember { mutableStateOf(0f) } // We'll use a float for the offset to handle fractional positions

    // Function to calculate and center the selected tab
    val centerTab = { index: Int ->
        // Calculate the offset for the selected tab to center it
        val tabOffset = tabWidths.take(index).sum() // Sum of widths of all previous tabs
        val selectedTabWidth = tabWidths.getOrElse(index) { 0f }

        // Calculate the offset required to center the selected tab
        val offsetToCenter = tabOffset + (selectedTabWidth / 2) - (screenWidthPx / 2)

        // Coerce the offset to be within the bounds of the tabWidths sum
        scrollPosition = offsetToCenter.coerceIn(0f, tabWidths.sum().toFloat())
    }

    // When the selected tab index changes, center the tab
    LaunchedEffect(selectedTabIndex) {
        centerTab(selectedTabIndex)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        // LazyRow for horizontal scrolling of tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()) // Make it horizontally scrollable
                .offset(x = scrollPosition.dp) // Apply the calculated scroll position (offset)
        ) {
            tabsList.forEachIndexed { tabIndex, tabName ->
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .onGloballyPositioned { coordinates ->
                            // Track the width of each tab dynamically as they are laid out
                            val tabWidth = coordinates.size.width.toFloat()
                            if (tabWidths.size <= tabIndex) {
                                tabWidths.add(tabWidth)
                            } else {
                                tabWidths[tabIndex] = tabWidth
                            }
                        }
                ) {
                    FilterChip(
                        selected = tabIndex == selectedTabIndex,
                        onClick = { selectedTabIndex = tabIndex },
                        label = {
                            Text(text = tabName, textAlign = TextAlign.Center)
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun GalleyPager() {
    // List of titles for the tabs
    val tabTitles = listOf("Photos and Videos", "Album")

    // Pager state to track the current page
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })


    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // TabRow for the tabs
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            indicator = {},
            divider = {},

            ) {
            // Create a Tab for each title
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 8.dp
                    ) // Add padding between tabs
                ) {
                    Text(text = title)
                }
            }
        }

        // HorizontalPager for the swipeable content
        HorizontalPager(
            state = pagerState,

            ) { page ->
            // Each page's content, corresponding to the selected tab
            TabContent(page = page)
        }
    }
}

@Composable
fun TabContent(page: Int) {

    val context = LocalContext.current

    val mediaItems = getImagesAndVideosFromGallery(context).sortedByDescending { it.dateAdded }


    val groupedByDate = groupMedia(mediaItems)


    // Group media items by the folder name (extracted from file path)
    val groupedByFolder = mediaItems.groupBy {
        val parentDir = File(it.path).parentFile?.name ?: "Unknown Folder"
        if (parentDir == "0" || parentDir.isEmpty()) "Root" else parentDir
    }


    // Different content for each tab
    when (page) {
        0 -> ShowPhotosAndVideos(groupedByDate)
        1 -> {
            ShowAlbums(groupedByFolder)
        }
    }
}

@Composable
fun ShowPhotosAndVideos(groupedByDate: Map<String, List<MediaData>>) {
    LoadGalleryWithPermissions {

        // LazyVerticalGrid to display images and videos in a grid layout
        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),  // Change 3 to the number of columns you need
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),  // Add padding around the grid
            horizontalArrangement = Arrangement.spacedBy(8.dp),  // Space between columns
            verticalArrangement = Arrangement.spacedBy(8.dp)   // Space between rows
        ) {


            // Loop over the grouped items
            groupedByDate.forEach { (date, mediaGroup) ->

                // Add a header for each date group, with full span
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = date,  // You can format this better if needed
                    )
                }

                // Display all media items for the current date group
                items(mediaGroup) { media ->
                    if (media.type == "image") {
                        MediaItemImage(media)
                    } else if (media.type == "video") {
                        MediaItemVideo(media)
                    }
                }
            }


        }

    }
}

@Composable
fun ShowAlbums(groupedByFolder: Map<String, List<MediaData>>) {
    LoadGalleryWithPermissions {


        // LazyVerticalGrid to display images and videos in a grid layout
        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),  // Change 3 to the number of columns you need
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),  // Add padding around the grid
            horizontalArrangement = Arrangement.spacedBy(8.dp),  // Space between columns
            verticalArrangement = Arrangement.spacedBy(8.dp)   // Space between rows

        ) {


            // Loop over the grouped items
            groupedByFolder.forEach { (album, mediaGroup) ->

                // Add a header for each date group, with full span
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = album,  // You can format this better if needed
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Display all media items for the current date group
                items(mediaGroup) { media ->
                    if (media.type == "image") {
                        MediaItemImage(media)
                    } else if (media.type == "video") {
                        MediaItemVideo(media)
                    }
                }
            }


        }

    }
}


@Composable
fun LoadGalleryWithPermissions(content: @Composable () -> Unit) {
    val context = LocalContext.current

    // Check if permissions are already granted
    val permissionImagesGranted = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.READ_MEDIA_IMAGES
    ) == PackageManager.PERMISSION_GRANTED

    val permissionVideosGranted = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.READ_MEDIA_VIDEO
    ) == PackageManager.PERMISSION_GRANTED

    // State to handle permission request
    val (permissionsGranted, setPermissionsGranted) = remember {
        mutableStateOf(permissionImagesGranted && permissionVideosGranted)
    }


    // Register the permission request callback for multiple permissions
    val requestPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val imagesGranted = permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == true
        val videosGranted = permissions[android.Manifest.permission.READ_MEDIA_VIDEO] == true

        // Update state based on the permissions result
        if (imagesGranted && videosGranted) {
            setPermissionsGranted(true)
        } else {
            setPermissionsGranted(false)
            // Optionally, show rationale or notify the user why these permissions are required
        }
    }


    // Column to show the UI for requesting permissions or displaying the gallery
    Column(modifier = Modifier.fillMaxSize()) {
        if (!permissionsGranted) {
            // If permissions are not granted, show a button to request them
            Button(onClick = {
                // Request both permissions at once
                requestPermissions.launch(
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                )
            }) {
                Text("Request Media Permissions")
            }
        }


        // If permissions are granted, show gallery or perform tasks
        if (permissionsGranted) {
            // Load the gallery once permissions are granted
            content()
        } else {
            Text("Please grant permissions to access images and videos.")
        }
    }

}


@Composable
fun MediaItemImage(media: MediaData) {
    val painter = rememberAsyncImagePainter(media.uri)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),  // Keep the aspect ratio of the grid item
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = media.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun MediaItemVideo(media: MediaData) {
    val context = LocalContext.current


    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }  // Flag to track if the thumbnail is loading


    LaunchedEffect(media.uri) {
        // Check if URI exists and retrieve the thumbnail in a background thread
        media.uri.let {
            if (isUriExist(context, it)) {
                // Use MediaMetadataRetriever to fetch the middle thumbnail in a background thread
                if (media.duration != -1L) {
                    try {
                        isLoading = true  // Start loading
                        thumbnail = withContext(Dispatchers.Default) {
                            // Retrieve the middle frame thumbnail or an equivalent
                            MediaMetadataRetriever().getMiddleVideoThumbnail(
                                context,
                                media.duration,
                                it
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching thumbnail: ${e.message}")
                        // Handle error if thumbnail creation fails
                    } finally {
                        isLoading = false  // Thumbnail loaded or failed
                    }
                }
            }
        }
    }

    // Card with adaptive size, aspect ratio ensures items are proportional
    Card(
        modifier = Modifier
            .fillMaxWidth()  // Fill available width
            .aspectRatio(1f),  // Ensure a square aspect ratio
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Show gray placeholder while loading
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Gray placeholder background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f))  // Semi-transparent gray
                    )
                }
            } else {
                // Show the video thumbnail once loaded
                thumbnail?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = media.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    // Gray placeholder background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f))  // Semi-transparent gray
                    )
                }
            }

            if (media.duration != -1L) {
                Text(
                    formatTimeSeconds(media.duration / 1000f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp)
                        .align(Alignment.BottomEnd)

                )

            }
        }
    }
}


fun getImagesAndVideosFromGallery(context: Context): List<MediaData> {
    val mediaList = mutableListOf<MediaData>()

    // Query Images
    val imageProjection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.DATA

    )
    val imageCursor: Cursor? = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        imageProjection,
        null,
        null,
        MediaStore.Images.Media.DATE_ADDED + " DESC"
    )

    imageCursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val widthColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val name = it.getString(nameColumn)
            val dateAdded = it.getLong(dateAddedColumn)
            val width = it.getInt(widthColumn)
            val height = it.getInt(heightColumn)
            val data = it.getString(dataColumn)

            val contentUri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )

            mediaList.add(MediaData(id, contentUri, name, "image", dateAdded, width, height, data))
        }
    }

    // Query Videos
    val videoProjection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.DATA


    )
    val videoCursor: Cursor? = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        videoProjection,
        null,
        null,
        MediaStore.Video.Media.DATE_ADDED + " DESC"
    )

    videoCursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
        val widthColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val heightColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val name = it.getString(nameColumn)
            val dateAdded = it.getLong(dateAddedColumn)
            val width = it.getInt(widthColumn)
            val height = it.getInt(heightColumn)
            val duration = it.getLong(durationColumn)
            val data = it.getString(dataColumn)

            val contentUri = Uri.withAppendedPath(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )

            mediaList.add(
                MediaData(
                    id,
                    contentUri,
                    name,
                    "video",
                    dateAdded,
                    width,
                    height,
                    data,
                    duration,
                )
            )
        }
    }

    return mediaList
}

data class MediaData(
    val id: Long, val uri: Uri, val displayName: String,
    val type: String,
    val dateAdded: Long, val width: Int, val height: Int, val path: String, val duration: Long = -1
)

/*

fun formatMediaDateAdded(dateAdded: Long): String {
    // Check if the timestamp is in seconds (not milliseconds)
    val adjustedDateAdded = if (dateAdded < 1000000000000L) {
        // If it's in seconds, convert to milliseconds by multiplying by 1000
        dateAdded * 1000
    } else {
        // If it's already in milliseconds, use it as is
        dateAdded
    }

    // Create a Date object from the adjusted timestamp (milliseconds)
    val date = Date(adjustedDateAdded)

    // Create a ZonedDateTime object from the Date object
    val dateTime = date.toInstant().atZone(ZoneId.systemDefault())

    // Determine the date of the timestamp
    val dateOfTimestamp = dateTime.toLocalDate()

    // Get the current date
    val now = LocalDate.now()

    // Check if the date is today
    if (dateOfTimestamp.isEqual(now)) {
        // Format: HH:mm a (e.g., 02:00 AM)
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        return dateTime.format(timeFormatter)
    }

    // Check if the date is yesterday
    if (dateOfTimestamp.isEqual(now.minus(1, ChronoUnit.DAYS))) {
        // Format: Yesterday HH:mm a (e.g., Yesterday 12:00 AM)
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val timePart = dateTime.format(timeFormatter)
        return "Yesterday $timePart"
    }

    // Check if the date is within the last 7 days
    if (!dateOfTimestamp.isBefore(now.minus(7, ChronoUnit.DAYS))) {
        val dayOfWeek = dateOfTimestamp.dayOfWeek
        val weekdayName = dayOfWeek.name.lowercase()
            .replaceFirstChar { it.uppercase() } // Capitalize the first letter

        // Format time part for the last 7 days
        val timeOfTimestamp = dateTime.toLocalTime() // Get time from ZonedDateTime
        val timeFormatted = timeOfTimestamp.format(DateTimeFormatter.ofPattern("h:mm a"))

        return "$weekdayName $timeFormatted"
    }

    // For any other date, format as: MMM d, yyyy h:mm a (e.g., Sep 12, 2024 5:37 AM)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
    return dateTime.format(dateTimeFormatter)
}

*/


private fun groupMedia(medias: List<MediaData>): Map<String, List<MediaData>> {
    val groupedMedias = mutableMapOf<String, MutableList<MediaData>>()

    val now = LocalDate.now()

    for (media in medias) {


        val dateAdded = media.dateAdded

        val adjustedDateAdded = if (dateAdded < 1000000000000L) {
            // If it's in seconds, convert to milliseconds by multiplying by 1000
            dateAdded * 1000
        } else {
            // If it's already in milliseconds, use it as is
            dateAdded
        }

        // Convert timestamp to LocalDate
        val messageLocalDate = Instant.ofEpochMilli(adjustedDateAdded)
            .atZone(ZoneId.systemDefault())  // Adjust the ZoneId if needed (e.g., UTC)
            .toLocalDate()

        // Calculate days between the current date and message date
        val daysBetween = now.toEpochDay() - messageLocalDate.toEpochDay()

        // Determine the grouping label
        val label = when {
            messageLocalDate.isEqual(now) -> "Today"  // If the message date is today
            messageLocalDate.isEqual(now.minusDays(1)) -> "Yesterday"  // If the message date is yesterday
            daysBetween in 1..6 -> messageLocalDate.format(DateTimeFormatter.ofPattern("EEEE"))  // For the last 7 days (including today and yesterday)
            else -> messageLocalDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))  // For dates outside the last 7 days
        }


        // Add message to the corresponding group
        groupedMedias.computeIfAbsent(label) { mutableListOf() }.add(media)
    }

    return groupedMedias
}


@Composable
fun CameraXApp() {
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var previewUseCase by remember { mutableStateOf<androidx.camera.core.Preview?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    val context = LocalContext.current

    // Setup CameraX provider
    LaunchedEffect(true) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()

        // Create Preview use case
        previewUseCase = androidx.camera.core.Preview.Builder().build()

        // Create ImageCapture use case
        imageCaptureUseCase = ImageCapture.Builder().build()

        // Bind the use cases to the camera lifecycle
        cameraProvider?.bindToLifecycle(
            ProcessLifecycleOwner.get(),
            cameraSelector,
            previewUseCase,
            imageCaptureUseCase
        )
    }

    // Camera preview and UI controls
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(cameraProvider = cameraProvider, previewUseCase = previewUseCase)
        TorchButton()
        ActionControls(imageCaptureUseCase = imageCaptureUseCase)
    }
}

@Composable
fun CameraPreview(
    cameraProvider: ProcessCameraProvider?,
    previewUseCase: androidx.camera.core.Preview?
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        previewUseCase?.surfaceProvider = previewView.surfaceProvider
    }
}

@Composable
fun BoxScope.TorchButton() {
    var flashEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    IconButton(
        onClick = {
            flashEnabled = !flashEnabled
            // Toggle torch/flash state (CameraX flash support needs to be implemented)
            Toast.makeText(
                context,
                "Flash: ${if (flashEnabled) "On" else "Off"}",
                Toast.LENGTH_SHORT
            ).show()
        },
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.TopStart)
    ) {
        Icon(
            imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
            contentDescription = "Toggle Flash",
            tint = Color.White
        )
    }
}

@Composable
fun BoxScope.ActionControls(
    imageCaptureUseCase: ImageCapture?
) {
    val context = LocalContext.current
    var isTakingPhoto by remember { mutableStateOf(false) }


    // List of tabs
    val tabs = listOf("Photo", "Video", "Portrait", "Night Mode")

    // State for Pager and ScrollableTabRow
    val pagerState = rememberPagerState(pageCount = { tabs.size })


    // Get a coroutine scope to launch scrolling actions
    val scope = rememberCoroutineScope()



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(16.dp)
    ) {

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier
                .fillMaxWidth()
                .offset(), // Here, we bind scrollState to the tab row
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = Color.Yellow
                )
            }

        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    selectedContentColor = Color.Yellow,
                    unselectedContentColor = Color.White,

                    onClick = {
                        // Launch a coroutine to animate the scroll to the position
                        scope.launch {
                            pagerState.animateScrollToPage(index)

                        }
                    },
                    text = {
                        Text(text = title)
                    }
                )
            }
        }


        // HorizontalPager for the swipeable content
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) { page ->

            when (page) {
                0 -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(60.dp) // Outer circle size (border area)
                                    .border(
                                        width = 1.dp, // Border thickness
                                        color = Color.White, // Border color
                                        shape = CircleShape
                                    )
                            ) {

                            }

                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp) // Outer circle size (border area)
                                    .border(
                                        width = 2.dp, // Border thickness
                                        color = Color.White, // Border color
                                        shape = CircleShape
                                    )
                            ) {
                                // Circular Button to capture photo with a white-filled circle and border gap
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(8.dp)
                                        .size(70.dp) // Size of the circle
                                        .background(
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            if (!isTakingPhoto) {
                                                isTakingPhoto = true
                                                imageCaptureUseCase?.takePicture(
                                                    ContextCompat.getMainExecutor(context),
                                                    object :
                                                        ImageCapture.OnImageCapturedCallback() {
                                                        override fun onCaptureSuccess(image: ImageProxy) {
                                                            super.onCaptureSuccess(image)
                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Photo captured",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                            isTakingPhoto = false
                                                        }

                                                        override fun onError(exception: ImageCaptureException) {
                                                            super.onError(exception)
                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Error capturing photo",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                            isTakingPhoto = false
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isTakingPhoto) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(30.dp), // Circle inside the button
                                            color = Color.Red,
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }
                            }
                        }


                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(40.dp) // Outer circle size (border area)
                                    .border(
                                        width = 1.dp, // Border thickness
                                        color = Color.White, // Border color
                                        shape = CircleShape
                                    )
                                    .padding(8.dp)
                            ) {

                                IconButton({}) {
                                    Icon(Icons.Filled.Cameraswitch, contentDescription = null)
                                }
                            }
                        }

                    }
                }

                else -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp) // Outer circle size (border area)
                                .border(
                                    width = 2.dp, // Border thickness
                                    color = Color.White, // Border color
                                    shape = CircleShape
                                )
                        ) {
                            // Circular Button to capture photo with a white-filled circle and border gap
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                                    .size(70.dp) // Size of the circle
                                    .background(
                                        color = Color.Red,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (!isTakingPhoto) {
                                            isTakingPhoto = true
                                            imageCaptureUseCase?.takePicture(
                                                ContextCompat.getMainExecutor(context),
                                                object : ImageCapture.OnImageCapturedCallback() {
                                                    override fun onCaptureSuccess(image: ImageProxy) {
                                                        super.onCaptureSuccess(image)
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "Photo captured",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                        isTakingPhoto = false
                                                    }

                                                    override fun onError(exception: ImageCaptureException) {
                                                        super.onError(exception)
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "Error capturing photo",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                        isTakingPhoto = false
                                                    }
                                                }
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isTakingPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(30.dp), // Circle inside the button
                                        color = Color.Red,
                                        strokeWidth = 3.dp
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


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {


    AppTheme {
        Surface {
            SimCardSubscription(SimCardInfo(1, "Jio", "9944870144", 121454), true) {}

        }
    }
}

