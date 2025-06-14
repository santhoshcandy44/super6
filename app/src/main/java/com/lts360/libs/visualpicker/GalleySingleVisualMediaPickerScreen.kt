package com.lts360.libs.visualpicker

import android.net.Uri
import android.nfc.Tag
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.libs.imagepicker.models.ImageMediaData
import com.lts360.libs.imagepicker.ui.MediaItemImage
import com.lts360.libs.visualpicker.ui.MediaItemVideo
import kotlinx.coroutines.launch


@Composable
fun GallerySingleVisualPickerScreen(
    onImagePicked: (Uri) -> Unit,
    onNavigateUpAlbum: (String) -> Unit,
    viewModel: VisualMediaPickerViewModel
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // List of titles for the tabs
        val tabTitles = listOf("All Visuals", "Album")
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
                TabContent(page = page, onImagePicked, onNavigateUpAlbum, viewModel)
            }
        }
    }

}


@Composable
fun TabContent(
    page: Int,
    onImagePicked: (Uri) -> Unit,
    onNavigateUpAlbum: (String) -> Unit,
    viewModel: VisualMediaPickerViewModel
) {


    val mediaItems by viewModel.mediaItems.collectAsState()

    val groupedByDateMediaItems = viewModel.groupMediaDate(mediaItems)
    val groupedByFolderMediaItems = viewModel.groupMediaFolders(mediaItems)


    // Different content for each tab
    when (page) {
        0 -> ShowVisuals(groupedByDateMediaItems, onImagePicked)
        1 -> {
            ShowVisualsAlbums(groupedByFolderMediaItems, onNavigateUpAlbum)
        }
    }
}


@Composable
fun ShowVisuals(groupedByDate: Map<String, List<ImageMediaData>>, onImagePicked: (Uri) -> Unit) {
    // LazyVerticalGrid to display images and videos in a grid layout
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp),  // Change 3 to the number of columns you need
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        contentPadding = PaddingValues(8.dp),  // Add padding around the grid
        horizontalArrangement = Arrangement.spacedBy(8.dp),  // Space between columns
        verticalArrangement = Arrangement.spacedBy(8.dp)   // Space between rows
    ) {


        // Loop over the grouped items
        groupedByDate.entries.forEachIndexed { index, entry ->

            val (date, mediaGroup) = entry

            // Add a header for each date group, with full span
            item(span = { GridItemSpan(maxLineSpan) },
                key = "key_${index}_${date.hashCode()}") {
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

                if(media.type=="video"){
                    MediaItemVideo(media,{
                        onImagePicked(media.uri)
                    })
                }
            }
        }


    }

}


@Composable
fun ShowVisualsAlbums(
    groupedByFolder: Map<String, List<ImageMediaData>>,
    onNavigateUpAlbum: (String) -> Unit
) {
    // LazyVerticalGrid to display images and videos in a grid layout
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp),  // Change 3 to the number of columns you need
        modifier = Modifier.fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        contentPadding = PaddingValues(8.dp),  // Add padding around the grid
        horizontalArrangement = Arrangement.spacedBy(8.dp),  // Space between columns
        verticalArrangement = Arrangement.spacedBy(8.dp)   // Space between rows

    ) {


        // Loop over the grouped items
        groupedByFolder.forEach { (album, mediaGroup) ->


            if (mediaGroup.isNotEmpty()) {

                val mediaGroupItem = mediaGroup[0]

                item(key = mediaGroupItem.id) {

                    Column(
                        modifier = Modifier.wrapContentSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        if (mediaGroupItem.type == "image") {
                            MediaItemImage(mediaGroupItem, onClicked = {
                                onNavigateUpAlbum(album)
                            })
                        }
                        if(mediaGroupItem.type=="video"){
                            MediaItemVideo(mediaGroupItem,{
                                onNavigateUpAlbum(album)
                            })
                        }
                        Text(
                            text = album,  // You can format this better if needed
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Clip,
                            maxLines = 1
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowAlbumVisualsPickerScreen(
    album: String,
    items: Map<String, List<ImageMediaData>>,
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
            ShowVisuals(
                items
            ) {
                onImagePicked(it)
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
