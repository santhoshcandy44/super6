package com.super6.pot.app

import android.app.Application
import android.content.Context
import android.util.Log
import com.super6.pot.components.utils.LogUtils.TAG
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.system.exitProcess


class AppExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Log the error to a file
        // Convert the throwable stack trace to a string
        val errorContent = throwable.stackTraceToString()

        // Log the error to a file in the public directory
        saveLogToPublicDirectory(context, errorContent)
        // Pass the exception to the default handler (if any)
        defaultHandler?.uncaughtException(thread, throwable)
        // Ensure the app exits after logging the error
        exitProcess(0)
    }



   private fun saveLogToPublicDirectory(context: Context, content: String) {
        // Prepare the ContentValues to insert or find the log file
        val mediaDirs = (context as Application).externalMediaDirs


        val directory = File(
            mediaDirs[0],
            "Logs"
        )

       if(!directory.exists()){
           directory.mkdirs()
       }
        // Log file
        val errorLog = File(directory, "error_log.txt")  // Specify file name with extension

        try {
            // Open the file in append mode and write the content
            FileWriter(errorLog, true).use { writer ->
                writer.append(content)  // Append content to the log file
                writer.append("\n")  // Add a newline after the log entry (optional)
            }
            Log.d(TAG, "Error Log saved successfully!")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save log: ${e.localizedMessage}")
        }

    }

}

