package com.lts360.libs.imagepicker.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.libs.imagepicker.models.ImageMediaData
import com.lts360.libs.imagepicker.ui.LoadImageGalleryWithPermissions
import com.lts360.libs.imagepicker.ui.MediaItemImage
import com.lts360.libs.imagepicker.ui.MediaItemVideo


fun redirectToAppSettings(context: Context) {
    // Open the app settings page where the user can enable permissions manually
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}



fun getImagesAndVideosFromGallery(context: Context): List<ImageMediaData> {
    val mediaList = mutableListOf<ImageMediaData>()

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

            mediaList.add(ImageMediaData(id, contentUri, name, "image", dateAdded, width, height, data))
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
                ImageMediaData(
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


@Composable
fun ShowPhotosAndVideos(groupedByDate: Map<String, List<ImageMediaData>>) {
    LoadImageGalleryWithPermissions {

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
fun ShowAlbums(groupedByFolder: Map<String, List<ImageMediaData>>) {
    LoadImageGalleryWithPermissions {


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


