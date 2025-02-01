package com.super6.pot.app.workers

import android.content.Context
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.api.auth.managers.socket.SocketConnectionException
import io.socket.client.Socket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


fun getFileMimeType(extension: String): String {

    return when (extension.lowercase()) {
        // Image file types
        ".jpg", ".jpeg" -> "image/jpeg"
        ".png" -> "image/png"
        ".gif" -> "image/gif"
        ".bmp" -> "image/bmp"
        ".webp" -> "image/webp"

        // Audio file types
        ".mp3" -> "audio/mpeg"
        ".wav" -> "audio/wav"
        ".ogg" -> "audio/ogg"
        ".flac" -> "audio/flac"
        ".aac" -> "audio/aac"

        // Video file types
        ".mp4" -> "video/mp4"
        ".mkv" -> "video/x-matroska"
        ".webm" -> "video/webm"
        ".avi" -> "video/x-msvideo"
        ".mov" -> "video/quicktime"
        ".flv" -> "video/x-flv"
        ".ts"-> "video/mp2t"

        // Document file types
        ".pdf" -> "application/pdf"
        ".txt" -> "text/plain"
        ".doc", ".docx" -> "application/msword"
        ".xls", ".xlsx" -> "application/vnd.ms-excel"
        ".ppt", ".pptx" -> "application/vnd.ms-powerpoint"
        ".csv" -> "text/csv"

        // Compressed files
        ".zip" -> "application/zip"
        ".rar" -> "application/x-rar-compressed"
        ".tar" -> "application/x-tar"
        ".gz" -> "application/gzip"

        // Miscellaneous
        ".html", ".htm" -> "text/html"
        ".json" -> "application/json"
        ".xml" -> "application/xml"


        // For any other types, use a wildcard
        else -> "*/*"
    }
}




fun getFolderTypeByExtension(fileExtension: String): Pair<String, String> {
    return when (fileExtension.lowercase()) {
        // Image file types
        ".jpg", ".jpeg", ".png", ".bmp", ".webp" -> Pair("Super6 Images", "Image")

        ".gif" -> Pair("Super6 Gifs", "Gif")
        // Audio file types
        ".mp3", ".wav", ".ogg", ".flac", ".aac" -> Pair("Super6 Audio", "Audio")

        // Video file types
        ".mp4", ".mkv", ".webm", ".avi", ".mov", ".flv", ".ts" -> Pair("Super6 Videos", "Video")

        // Document file types
        ".pdf", ".txt", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".csv" -> Pair(
            "Super6 Documents",
            "Document"
        )

        // Compressed files
        ".zip", ".rar", ".tar", ".gz" -> Pair("Super6 Compressed", "Compressed")

        // Miscellaneous
        ".html", ".htm", ".json", ".xml" -> Pair("Super6 Miscellaneous", "Miscellaneous")

        else -> Pair("Super6 Others", "Others")
    }
}


fun getFileExtension(fileName: String): String {
    return fileName.substring(fileName.lastIndexOf("."))
}


fun generateUniqueFileName(extension: String): String {
    // Format the current date into a human-readable format (e.g., "20241205_153245")
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val formattedDate =
        dateFormat.format(Date())  // Get the formatted date (e.g., "20241205_153245")

    // Generate a random unique identifier
    val uniqueId = UUID.randomUUID().toString().substring(0, 8)  // Get the first 8 characters of the UUID


    val prefix = when (getFileCategoryByExtension(extension)) {
        "image" -> "IMG"
        "gif" -> "GIF"
        "video" -> "VID"
        "audio" -> "AUD"
        "file" -> "FILE"
        "other" -> "OTHER"
        else -> {
            "MEDIA"
        }
    }

    return "${prefix}_${formattedDate}_SUPER6_${uniqueId}$extension"
}


fun getFileCategoryByExtension(extension: String): String {
    return when (extension.lowercase()) {
        // Image file types
        ".jpg", ".jpeg", ".png", ".bmp", ".webp" -> "image"

        ".gif" -> "gif"

        // Audio file types
        ".mp3", ".wav", ".ogg", ".flac", ".aac" -> "audio"

        // Video file types
        ".mp4", ".mkv", ".webm", ".avi", ".mov", ".flv" , ".ts" -> "video"

        // Document file types
        ".pdf", ".txt", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".csv" -> "file"

        // For any other file types
        else -> "other"
    }
}


// Helper suspend function to handle socket connection
suspend fun awaitConnectToSocket(socketManager: SocketManager, isBackground:Boolean=true, isForceNew:Boolean = false, queryParam:String =""): Socket = withContext(Dispatchers.IO) {

   val socketDeferred = CompletableDeferred<Socket>()

    socketManager.getSocket(
        onSuccess = {
            if (!socketDeferred.isCompleted) { // Check if the Deferred is not already completed
                socketDeferred.complete(it) // Complete with the socket if not yet completed
            }
        },
        onError = {
            // If the socket connection fails, we retry the work
            socketDeferred.completeExceptionally(SocketConnectionException("Socket connection error"))
        },
        isBackground = isBackground,
        isForceNew = isForceNew,
        queryParam = queryParam

    )

    return@withContext socketDeferred.await()
}



fun cacheThumbnailToAppSpecificFolder(
    context: Context,
    fileName: String,
    thumbnailExtension:String,
    extension: String,
): File {
    // Get the app-specific media directory (this will return a list of directories)
    val mediaDirs = context.getExternalFilesDir(null)

    val fileCategoryAndTypeByExtension = getFolderTypeByExtension(extension)

    // Select the primary external media directory for your app
    val directory = File(
        mediaDirs,
        fileCategoryAndTypeByExtension.first
    ) // Create the "Super6 Images" folder within the first directory


    // Ensure the directory exists
    if (!directory.exists()) {
        directory.mkdirs()  // Create the directory if it doesn't exist
    }

    if (!File(directory, ".nomedia").exists()) {
        File(directory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
    }

    // Generate a unique file name and ensure it does not exist

    var uniqueFileName = fileName

    // Only generate a unique name for Image, Video
    if (fileCategoryAndTypeByExtension.second in listOf("Image", "Gif", "Video")) {
        uniqueFileName = generateUniqueFileName(thumbnailExtension)

        // Check if file exists, if so, regenerate the unique name
        while (File(directory, uniqueFileName).exists()) {
            uniqueFileName = generateUniqueFileName(thumbnailExtension)
        }
    }


    // Create a new file in the directory
    return File(directory, uniqueFileName)

}




fun lastMessageTimestamp(timestamp: Long): String {

    // Get the current date and the date of the timestamp
    val now = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    // Check if the message is from today
    if (now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
    ) {
        // Format time of day for today (e.g., "2:30 PM")
        val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdfTime.format(Date(timestamp))
    }

    // Check if the message is from yesterday
    if (now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) + 1
    ) {
        // Format: Yesterday HH:mm a (e.g., "Yesterday 2:30 PM")
        val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timePart = sdfTime.format(Date(timestamp))
        return "Yesterday $timePart"
    }

    // For any other date, format as: MMM d, yyyy (e.g., "Sep 12, 2024")
    val sdfDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdfDate.format(Date(timestamp))
}
