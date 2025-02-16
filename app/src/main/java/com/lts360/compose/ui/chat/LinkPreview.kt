package com.lts360.compose.ui.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup



data class LinkPreviewData(
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val url: String
)




suspend fun fetchLinkPreview(url: String): LinkPreviewData? = withContext(Dispatchers.IO) {
    val maxRetries = 100
    val retryDelay = 1000L // 1 second delay between retries
    var currentAttempt = 0

    while (currentAttempt < maxRetries) {
        try {

            val startTime = System.currentTimeMillis()
            val userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.5615.137 Safari/537.36"
            val document = Jsoup
                .connect(url)
                .userAgent(userAgent) // Set the User-Agent
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .get()


            // Try to get og:title, fallback to <title> tag if not found
            val title = document.select("meta[property=og:title]").attr("content")
                .ifEmpty { document.title() }

            // Try to get og:description, fallback to description meta tag if not found
            val description = document.select("meta[property=og:description]").attr("content")
                .ifEmpty { document.select("meta[name=description]").attr("content") }

            // Try to get og:image, fallback to a default image if not found
            val imageUrl = document.select("meta[property=og:image]").attr("content")
                .ifEmpty { document.select("meta[name=image]").attr("content").ifEmpty { null } }



            return@withContext LinkPreviewData(title, description, imageUrl, url)
        } catch (e: Exception) {
            e.printStackTrace()
            currentAttempt++

            if (currentAttempt < maxRetries) {
                delay(retryDelay) // Delay before retrying
            }
        }
    }
    null
}



