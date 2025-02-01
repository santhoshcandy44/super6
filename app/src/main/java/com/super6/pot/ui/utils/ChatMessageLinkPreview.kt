package com.super6.pot.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.super6.pot.ui.chat.LinkPreviewData
import com.super6.pot.ui.chat.fetchLinkPreview

@Composable
fun ChatMessageLinkPreview(text: String) {


    val context = LocalContext.current

    // State for the link preview
    var linkPreview by remember { mutableStateOf<LinkPreviewData?>(null) }


    // Extract the first URL from the text.
    val firstLink = Regex(
        "(?<!\\S)(https?|ftp)://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?|(?:www\\.)[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
        setOf(RegexOption.IGNORE_CASE) // Enable case insensitive matching
    ).find(text)?.value

    // Fetch link preview for the first link, if available.
    LaunchedEffect(firstLink) {
        if (firstLink != null) {
            linkPreview = fetchLinkPreview(firstLink)
        }
    }





    Column(modifier = Modifier.fillMaxWidth(0.7f)) {


        // Show link preview if available.
        if (linkPreview != null) {
            // Link preview UI (e.g., title, description, image).
            Text(
                text = linkPreview?.title.orEmpty(),
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold

            )
            Text(
                text = linkPreview?.description.orEmpty(),
                maxLines = 5, color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis

            )

            // Load and display image if available
            linkPreview?.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Link Preview Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.aspectRatio(5f / 4f)
                )
            }

        }


    }

}


@Composable
fun ChatMessageLinkPreviewHeader(linkPreviewData: LinkPreviewData?) {


    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Load and display image if available
        linkPreviewData?.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Link Preview Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)) // Rounded corners with 8.dp radius
            )
        }


        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            // Link preview UI (e.g., title, description, image).
            Text(
                text = linkPreviewData?.title.orEmpty(),
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold

            )
            Text(
                text = linkPreviewData?.description.orEmpty(),
                maxLines = 5, color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis

            )
        }


    }

}


@Composable
fun ChatMessageLinkPreviewHeaderLoading() {


    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {

        Box(
            modifier = Modifier.size(80.dp)
        )

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            // Link preview UI (e.g., title, description, image).
            Text(
                text = "",
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold

            )
            Text(
                text = "",
                maxLines = 5, color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis

            )
        }

    }

}


