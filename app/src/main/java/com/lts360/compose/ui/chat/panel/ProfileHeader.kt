package com.lts360.compose.ui.chat.panel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.api.models.service.FeedUserProfileInfo

@Composable
fun ProfileHeader(
    imageLoader: ImageLoader,
    userProfileInfo: FeedUserProfileInfo
) {

    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(userProfileInfo.profilePicUrl96By96)
        .placeholder(R.drawable.user_placeholder)
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()


    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = 8.dp)
            ) {

                AsyncImage(
                    imageRequest,
                    imageLoader = imageLoader,
                    contentDescription = "User Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

            }

            // User Name and Status Container
            Column(
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Joined at ${userProfileInfo.createdAt}",
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }


}
