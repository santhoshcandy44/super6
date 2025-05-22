package com.lts360.compose.ui.chat.panel

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.api.models.service.FeedUserProfileInfo

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ChatTopBarScreen(
    userProfileInfo: FeedUserProfileInfo,
    imageLoader: ImageLoader,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateBack:()-> Unit){

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()){

        Surface(shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically){

                IconButton(onClick = dropUnlessResumed {
                    onNavigateBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start) {
                    Text(text = "Chat Info")
                }
            }
        }


        Row(modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)){
            AsyncImage(
                ImageRequest.Builder(context)
                    .data(userProfileInfo.profilePicUrl96By96)
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "User Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .sharedBounds(rememberSharedContentState(key = "profile-pic-${userProfileInfo.userId}"),
                        animatedVisibilityScope = animatedVisibilityScope).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                userProfileInfo.about?.let {
                    Text(
                        text = userProfileInfo.about,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "Joined at ${userProfileInfo.createdAt}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 14.sp
                )
            }
        }

    }

}