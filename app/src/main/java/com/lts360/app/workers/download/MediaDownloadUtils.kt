package com.lts360.app.workers.download

import android.content.Context
import com.lts360.api.app.AppClient
import com.lts360.api.auth.services.CommonService
import com.lts360.app.workers.getFolderTypeByExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


suspend fun downloadMediaAndCache(context:Context, downloadUrl:String, originalFileName:String, extension:String):File{
    val responseBody = AppClient.mediaDownloadInstance.create(CommonService::class.java).downloadMedia(downloadUrl)


    val dir = context.getExternalFilesDir(null)

    val fileCategoryAndTypeByExtension = getFolderTypeByExtension(extension)

    val directory = File(dir, fileCategoryAndTypeByExtension.first)

    if (!directory.exists()) {
        directory.mkdirs()  // Create the directory if it doesn't exist
    }

    // Create the file in the desired directory
    val destinationFile = File(directory, "$originalFileName.enc")


    var readLength: Int
    val buffer = ByteArray(4 * 1024)
    val bis = BufferedInputStream(responseBody.byteStream(), 4 * 1024)

    var totalBytesDownloaded: Long = 0


    // Write directly to disk in chunks
    val fileOutputStream = withContext(Dispatchers.IO) {
        FileOutputStream(destinationFile, false)
    }

    val fileChannel = fileOutputStream.channel
    val bufferByteBuffer = ByteBuffer.allocate(4 * 1024)



    withContext(Dispatchers.IO) {
        while (bis.read(buffer).also { readLength = it } != -1) {

            if (!isActive) {
                cancel() // Stop the download if the job is cancelled
                return@withContext
            }
            totalBytesDownloaded += readLength.toLong()
            bufferByteBuffer.clear()
            bufferByteBuffer.put(buffer, 0, readLength)
            bufferByteBuffer.flip()
            fileChannel.write(bufferByteBuffer)

        }

        bis.close()
        fileOutputStream.close()
    }

    return destinationFile

}