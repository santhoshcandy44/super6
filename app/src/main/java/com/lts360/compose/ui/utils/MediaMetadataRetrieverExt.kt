package com.lts360.compose.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.lts360.components.utils.getPathFromUri
import java.io.IOException


fun MediaMetadataRetriever.getMiddleVideoThumbnail(
    context: Context,
    duration: Long,
    uri: Uri
): Bitmap? {

    setDataSource(context, uri)
    val thumbnailTimeUs: Long = duration * 1000 / 2
    val thumbnail = getFrameAtTime(thumbnailTimeUs)
    release()
    return thumbnail
}


fun MediaMetadataRetriever.getThumbnail(context: Context, uri: Uri, frameAt: Long = 0): Bitmap? {

    val openFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: throw IOException("Failed to open file descriptor")

    setDataSource(openFileDescriptor.fileDescriptor)
    val thumbnail = getFrameAtTime(frameAt)
    openFileDescriptor.close()
    release()
    return thumbnail
}


fun MediaMetadataRetriever.getThumbnailFromPath(path: String): Bitmap? {
    setDataSource(path)
    val thumbnail = getFrameAtTime(0)
    release()
    return thumbnail
}
