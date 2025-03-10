package com.lts360.libs.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.components.utils.isUriExist
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.chat.formatTimeSeconds
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.utils.getMiddleVideoThumbnail
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class GalleyPagerActivityContracts {

    class ImagePicker : ActivityResultContract<Unit, Uri?>() {
        companion object {
            const val EXTRA_DATA = "data"
        }

        override fun createIntent(context: Context, input: Unit): Intent {

            return Intent(context, GalleyImagesPagerActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {

            return if (resultCode == Activity.RESULT_OK) {
                Uri.parse(intent?.getStringExtra(EXTRA_DATA))
            } else {
                null
            }

        }

    }

}

@Serializable
sealed class GalleyImagesPagerRoutes {
    @Serializable
    data object GalleyImagesPager : GalleyImagesPagerRoutes()

    @Serializable
    data class AlbumImagePicker(val album: String) : GalleyImagesPagerRoutes()
}

@AndroidEntryPoint
class GalleyImagesPagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Black
                    ) { contentPadding ->
                        LoadImageGalleryWithPermissions(modifier = Modifier.padding(contentPadding)) {

                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = GalleyImagesPagerRoutes.GalleyImagesPager
                            ) {

                                slideComposable<GalleyImagesPagerRoutes.GalleyImagesPager> {
                                    GalleyImagesPagerScreen(
                                        {
                                            setResult(RESULT_OK, Intent()
                                                .apply {
                                                    putExtra("data", it.toString())
                                                })
                                            finish()
                                        },
                                        {
                                            navController.navigate(
                                                GalleyImagesPagerRoutes.AlbumImagePicker(
                                                    it
                                                )
                                            )
                                        }
                                    )
                                }

                                slideComposable<GalleyImagesPagerRoutes.AlbumImagePicker> { backStackEntry ->

                                    val args =
                                        backStackEntry.toRoute<GalleyImagesPagerRoutes.AlbumImagePicker>()
                                    val viewModel: ImagePickerViewModel = hiltViewModel(
                                        navController.getBackStackEntry<GalleyImagesPagerRoutes.GalleyImagesPager>()
                                    )

                                    val groupedByFolderMediaItems by viewModel.groupedByFolderMediaItems.collectAsState()

                                    val album = groupedByFolderMediaItems[args.album]
                                    album?.let {
                                        ShowAlbumPhotosScreen(args.album, viewModel.groupMediaDate(
                                            it
                                        ), {
                                            navController.popBackStack()
                                        }) {
                                            setResult(RESULT_OK, Intent()
                                                .apply {
                                                    putExtra("data", it.toString())
                                                })
                                            finish()
                                        }

                                    }

                                }
                            }
                        }
                    }


                }
            }
        }
    }
}

@Composable
fun GalleyImagesPagerScreen(onImagePicked: (Uri) -> Unit, onNavigateUpAlbum: (String) -> Unit) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // List of titles for the tabs
        val tabTitles = listOf("Photos", "Album")
        // Pager state to track the current page
        val pagerState = rememberPagerState(pageCount = { tabTitles.size })

        val scope = rememberCoroutineScope()

        Column(modifier = Modifier.fillMaxSize()) {
            // TabRow for the tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally
                ),
                contentPadding = PaddingValues(16.dp)
            ) {

                itemsIndexed(tabTitles) { index, title ->
                    Text(
                        text = title,
                        color = if (index == pagerState.currentPage) Color.Yellow else Color.White,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch {
                                pagerState.scrollToPage(index)
                            }
                        }
                    )
                }
            }

            // HorizontalPager for the swipeable content
            HorizontalPager(state = pagerState) { page ->
                // Each page's content, corresponding to the selected tab
                TabContent(page = page, onImagePicked, onNavigateUpAlbum)
            }
        }
    }

}


data class MediaData(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val type: String,
    val dateAdded: Long,
    val width: Int,
    val height: Int,
    val path: String,
    val duration: Long = -1
)


@HiltViewModel
class ImagePickerViewModel @Inject constructor(@ApplicationContext val context: Context) :
    ViewModel() {

    // A StateFlow to hold the grouped media items, initially empty
    private val _groupedByFolderMediaItems =
        MutableStateFlow<Map<String, List<MediaData>>>(emptyMap())
    val groupedByFolderMediaItems = _groupedByFolderMediaItems.asStateFlow()


    private val _groupedByDateMediaItems =
        MutableStateFlow<Map<String, List<MediaData>>>(emptyMap())
    val groupedByDateMediaItems = _groupedByDateMediaItems.asStateFlow()


    init {
        loadMediaItems(context)
    }


    // Function to load and group media items
    private fun loadMediaItems(context: Context) {
        // Launch a coroutine to fetch and process images
        viewModelScope.launch(Dispatchers.IO) {
            // Fetch media items (sorted by descending dateAdded)

            val mediaItems = getImagesFromGallery(context).sortedByDescending { it.dateAdded }
            _groupedByDateMediaItems.value = groupMediaDate(mediaItems)
            _groupedByFolderMediaItems.value = groupMediaFolders(mediaItems)

        }
    }


    private fun getImagesFromGallery(context: Context): List<MediaData> {
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

                mediaList.add(
                    MediaData(
                        id,
                        contentUri,
                        name,
                        "image",
                        dateAdded,
                        width,
                        height,
                        data
                    )
                )
            }
        }

        return mediaList
    }

    private fun groupMediaFolders(medias: List<MediaData>): Map<String, List<MediaData>> {
        return medias.groupBy {
            val parentDir = File(it.path).parentFile?.name ?: "Unknown Folder"
            if (parentDir == "0" || parentDir.isEmpty()) "Root" else parentDir
        }
    }

    fun groupMediaDate(medias: List<MediaData>): Map<String, List<MediaData>> {
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


}


@Composable
fun TabContent(
    page: Int,
    onImagePicked: (Uri) -> Unit,
    onNavigateUpAlbum: (String) -> Unit,
    viewModel: ImagePickerViewModel = hiltViewModel()
) {


    val groupedByDateMediaItems by viewModel.groupedByDateMediaItems.collectAsState()
    val groupedByFolderMediaItems by viewModel.groupedByFolderMediaItems.collectAsState()


    // Different content for each tab
    when (page) {
        0 -> ShowPhotos(groupedByDateMediaItems, onImagePicked)
        1 -> {
            ShowPhotosAlbums(groupedByFolderMediaItems, onNavigateUpAlbum)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAlbumPhotosScreen(
    album: String,
    items: Map<String, List<MediaData>>,
    onPopStack: () -> Unit,
    onImagePicked: (Uri) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed { onPopStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(
                        text = album,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black,
    ) { contentPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            ShowPhotos(
                items
            ) {
                onImagePicked(it)
            }
        }


    }
}


@Composable
fun ShowPhotos(groupedByDate: Map<String, List<MediaData>>, onImagePicked: (Uri) -> Unit) {
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
                    color = Color.White,
                    text = date,  // You can format this better if needed
                )
            }

            // Display all media items for the current date group
            items(mediaGroup, key = { it.id }) { media ->
                if (media.type == "image") {
                    MediaItemImage(media, {
                        onImagePicked(media.uri)
                    })
                }
            }
        }


    }

}


@Composable
fun ShowPhotosAlbums(
    groupedByFolder: Map<String, List<MediaData>>,
    onNavigateUpAlbum: (String) -> Unit
) {
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


            if (mediaGroup.isNotEmpty()) {

                val mediaGroupItem = mediaGroup[0]

                item(key = mediaGroupItem.id) {

                    Column(modifier = Modifier.wrapContentSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)){

                        if (mediaGroupItem.type == "image") {
                            MediaItemImage(mediaGroupItem, onClicked = {
                                onNavigateUpAlbum(album)
                            })
                        }
                        Text(
                            text = album,  // You can format this better if needed
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Clip
                        )

                        Text(
                            text = mediaGroup.size.toString(),  // You can format this better if needed
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }

            }


        }


    }
}


@Composable
fun LoadImageGalleryWithPermissions(
    modifier: Modifier,
    content: @Composable () -> Unit
) {

    val context = LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        var showRationale by remember { mutableStateOf(false) }


        // State to handle permission request
        var isInitial by remember {
            mutableStateOf(
                true
            )
        }

        // State to handle permission request
        val (permissionsGranted, setPermissionsGranted) = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            )
        }


        // Register the permission request callback for multiple permissions
        val requestPermissions = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val imagesGranted = permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == true

            // Update state based on the permissions result
            if (imagesGranted) {
                setPermissionsGranted(true)
            } else {
                setPermissionsGranted(false)
                isInitial = false
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                )
                // Optionally, show rationale or notify the user why these permissions are required
            }
        }



        LaunchedEffect(Unit) {
            // Request both permissions at once
            requestPermissions.launch(
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
            )
        }


        // Column to show the UI for requesting permissions or displaying the gallery
        Box(modifier = modifier.fillMaxSize()) {

            // If permissions are granted, show gallery or perform tasks
            if (permissionsGranted) {
                // Load the gallery once permissions are granted
                content()
            } else {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (showRationale) {
                        // If permissions are not granted, show a button to request them
                        Text(
                            "Permission is required.",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        Button(

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFFFE8B02,
                                )
                            ),
                            onClick = {
                                redirectToAppSettings(context)
                            }) {
                            Text("Allow Permissions", color = Color.White)
                        }
                    } else {


                        if (!isInitial) {
                            // If permissions are not granted, show a button to request them
                            Text(
                                "To get all images permission is needed.",
                                color = Color.White, textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFE8B02,
                                    )
                                ),
                                onClick = {
                                    // Request both permissions at once
                                    requestPermissions.launch(
                                        arrayOf(
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                    )
                                }) {
                                Text("Allow Permissions", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

    } else {


        var showRationale by remember { mutableStateOf(false) }


        // State to handle permission request
        var isInitial by remember {
            mutableStateOf(
                true
            )
        }

        // State to handle permission request
        var readPermissionGranted by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
        }


        // Register the permission request callback for multiple permissions
        val requestPermissions = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val permissionGranted =
                permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true

            // Update state based on the permissions result
            if (permissionGranted) {
                readPermissionGranted = true
            } else {
                readPermissionGranted = false
                isInitial = false

                // Check if the user has permanently denied the permission
                showRationale = !ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity, android.Manifest.permission.READ_EXTERNAL_STORAGE
                )

                // Optionally, show rationale or notify the user why these permissions are required
            }
        }

        LaunchedEffect(Unit) {
            // Request both permissions at once
            requestPermissions.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }

        // Column to show the UI for requesting permissions or displaying the gallery
        Box(modifier = modifier.fillMaxSize()) {

            // If permissions are granted, show gallery or perform tasks
            if (readPermissionGranted) {
                // Load the gallery once permissions are granted
                content()
            } else {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (showRationale) {
                        // If permissions are not granted, show a button to request them
                        Text(
                            "Permission is required.",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        Button(

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFFFE8B02,
                                )
                            ),
                            onClick = {
                                redirectToAppSettings(context)
                            }) {
                            Text("Allow Permissions", color = Color.White)
                        }
                    } else {


                        if (!isInitial) {
                            // If permissions are not granted, show a button to request them
                            Text(
                                "To get all images permission is needed.",
                                color = Color.White, textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFE8B02,
                                    )
                                ),
                                onClick = {
                                    // Request both permissions at once
                                    requestPermissions.launch(
                                        arrayOf(
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                    )
                                }) {
                                Text("Allow Permissions", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

    }
}


fun redirectToAppSettings(context: Context) {
    // Open the app settings page where the user can enable permissions manually
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}


@Composable
fun MediaItemImage(media: MediaData, onClicked: () -> Unit = {}) {

    Card(
        onClick = onClicked,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),  // Keep the aspect ratio of the grid item
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                media.uri,
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
                    } catch (_: Exception) {
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

    Log.e(TAG, "${mediaList.size}")
    return mediaList
}

@Composable
fun ShowPhotosAndVideos(groupedByDate: Map<String, List<MediaData>>) {
    LoadGalleryWithPermissions {

        // LazyVerticalGrid to display images and videos in a grid layout
        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),  // Change 3 to the number of columns you need
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red),
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

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
    } else {

        val context = LocalContext.current


        // State to handle permission request
        var readPermissionGranted by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
        }


        // Register the permission request callback for multiple permissions
        val requestPermissions = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val permissionGranted =
                permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true

            // Update state based on the permissions result
            if (permissionGranted) {
                readPermissionGranted = true
            } else {
                readPermissionGranted = false
                // Optionally, show rationale or notify the user why these permissions are required
            }
        }


        // Column to show the UI for requesting permissions or displaying the gallery
        Column(modifier = Modifier.fillMaxSize()) {
            if (!readPermissionGranted) {
                // If permissions are not granted, show a button to request them
                Button(onClick = {
                    // Request both permissions at once
                    requestPermissions.launch(
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                }) {
                    Text("Request Media Permissions")
                }
            }


            // If permissions are granted, show gallery or perform tasks
            if (readPermissionGranted) {
                // Load the gallery once permissions are granted
                content()
            } else {
                Text("Please grant permissions to access images and videos.")
            }

        }

    }
}


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
