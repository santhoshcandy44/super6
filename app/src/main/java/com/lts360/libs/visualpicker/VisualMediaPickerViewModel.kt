package com.lts360.libs.visualpicker

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.libs.imagepicker.models.ImageMediaData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class VisualMediaPickerViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {


    private val _mediaItems = MutableStateFlow<List<ImageMediaData>>(emptyList())
    val mediaItems = _mediaItems.asStateFlow()

    init {
        loadMediaItems(context)
    }


    // Function to load and group media items
    private fun loadMediaItems(context: Context) {
        // Launch a coroutine to fetch and process images
        viewModelScope.launch(Dispatchers.IO) {
            // Fetch media items (sorted by descending dateAdded)
            val mediaItems = getImagesAndVideosFromGallery(context).sortedByDescending { it.dateAdded }
            _mediaItems.value = mediaItems
        }
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
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MINI_THUMB_MAGIC
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


    fun updateImageMediaIsSelected(mediaId: Long) {


        // Get the current value of _groupedByDateMediaItems
        val currentMediaItems = _mediaItems.value

        // Create a new map to store the updated data
        val updatedGroupedData = currentMediaItems.map { mediaItem ->
            if (mediaItem.id == mediaId) {
                // Update the selection status of the media item
                mediaItem.copy(isSelected = !mediaItem.isSelected)  // Toggle selection status
            } else {
                mediaItem  // Leave the other items unchanged
            }
        }

        // Emit the updated grouped data to the MutableStateFlow
        _mediaItems.value = updatedGroupedData
    }


    fun groupMediaFolders(medias: List<ImageMediaData>): Map<String, List<ImageMediaData>> {
        return medias.groupBy {
            val parentDir = File(it.path).parentFile?.name ?: "Unknown Folder"
            if (parentDir == "0" || parentDir.isEmpty()) "Root" else parentDir
        }
    }

    fun groupMediaDate(medias: List<ImageMediaData>): Map<String, List<ImageMediaData>> {
        val groupedMedias = mutableMapOf<String, MutableList<ImageMediaData>>()

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