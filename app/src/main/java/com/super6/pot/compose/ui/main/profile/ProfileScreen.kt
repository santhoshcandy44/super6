package com.super6.pot.compose.ui.main.profile


import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import coil3.size.Size
import com.super6.pot.R
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.app.database.models.profile.UserProfile
import com.super6.pot.compose.ui.profile.viewmodels.ProfileViewModel
import com.super6.pot.compose.ui.theme.customColorScheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onPopStack:()-> Unit,
    viewModel: ProfileViewModel = hiltViewModel()) {
    val userId = viewModel.userId

    val profilePicBitmap by viewModel.profileImageBitmap.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed { onPopStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(text = "Profile", style = MaterialTheme.typography.titleMedium)
                }
            )
        }
    ) { contentPadding ->

        Box(modifier = Modifier.padding(contentPadding)) {
            ProfileScreenContent(
                profilePicBitmap,
                userProfile
            )
        }


    }

}


@Composable
private fun ProfileScreenContent(
    profilePicBitmap: Bitmap?,
    userProfile: UserProfile?,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        if (userProfile == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) // Show loading state

        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {

                item {
                    // Profile Header
                    ProfileHeader(
                        profilePicBitmap,
                        userProfile
                    )
                }

                item {
                    // About Section
                    userProfile.about?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileAboutSection(userProfile.about)
                    }
                }
            }

        }


    }


}


@Composable
fun ProfilePicUrlHeader(profile: FeedUserProfileInfo) {


    // Define a gradient brush
    val purpleGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6200EE),
            Color(0xFF9747ff),
            Color(0xFFBB86FC)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {


        // Profile Image
        profile.profilePicUrl?.let { profilePicUrl ->

            val imageRequest = ImageRequest.Builder(LocalContext.current)
                .data(profilePicUrl) // Set the image URL
                .apply {
                    placeholder(R.drawable.user_placeholder) // Placeholder image
                    error(R.drawable.user_placeholder) // Error image in case of failure
                }
                .build()

            AsyncImage(
                imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .border(
                        width = 4.dp,
                        purpleGradientBrush,
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .clip(CircleShape),

                contentScale = ContentScale.Crop // Crop the image to fit the circle
            )

        }
            ?: run {
                Image(
                    painter = painterResource(R.drawable.user_placeholder),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .border(
                            width = 4.dp,
                            purpleGradientBrush,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop // Crop the image to fit the circle
                )
            }


        Spacer(modifier = Modifier.width(16.dp))

        // Name, Email, and Joined Date
        Column {
            Text(
                text = "${profile.firstName} ${profile.lastName ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = "Joined at ${profile.createdAt}"
            )
        }
    }


}


@Composable
fun ProfileHeader(
    profilePicBitmap: Bitmap?,
    profile: UserProfile,
) {

    // Define a gradient brush
    val purpleGradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF6200EE), Color(0xFF9747ff), Color(0xFFBB86FC))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {


        // Profile Image
        profilePicBitmap?.let {
            Image(it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .border(
                        width = 4.dp,
                        purpleGradientBrush,
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .clip(CircleShape),

                contentScale = ContentScale.Crop // Crop the image to fit the circle
            )

        }
            ?: run {
                Image(
                    painter = painterResource(R.drawable.user_placeholder),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .border(
                            width = 4.dp,
                            purpleGradientBrush,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop // Crop the image to fit the circle
                )
            }


        Spacer(modifier = Modifier.width(16.dp))

        // Name, Email, and Joined Date
        Column {
            Text(
                text = "${profile.firstName} ${profile.lastName ?: ""}",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = "Joined at ${profile.createdAt}"
            )
        }
    }
}


@Composable
fun ProfileAboutSection(about: String) {
    Text(
        text = "About",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = about,
        style = MaterialTheme.typography.bodyMedium,
    )
}


@Composable
fun ProfileServicesSection(
    thumbnail: String?,
    title: String,
    shortDescription: String,
    onItemClick: () -> Unit,
    onOptionItemClick: () -> Unit,
) {
    val context = LocalContext.current

    OutlinedCard(
        onClick = dropUnlessResumed {
            onItemClick()
        },
    ) {
        Column {
            // Service Image

            val imageRequest = ImageRequest.Builder(context)
                .size(Size.ORIGINAL)
                .data(thumbnail) // Use placeholder drawable if imageUrl is null
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = "Service Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .background(MaterialTheme.customColorScheme.serviceSurfaceContainer)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Service Name
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {

                // Short Description
                Text(
                    text = shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                // More Options (e.g., a button)
                IconButton(
                    onClick = onOptionItemClick,
                    modifier = Modifier.padding(start = 8.dp) // Add some space before the icon
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options" // Add content description for accessibility
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))


        }
    }

}


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun BoxScope.ProfileInfoChatUser(
    profilePicUrl: String?,
    isOnline: Boolean,
    onChatClick: () -> Unit,
) {

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(profilePicUrl)
            .placeholder(R.drawable.user_placeholder)  // Placeholder image resource
            .error(R.drawable.user_placeholder)  // Error image resource
            .build()
    )


    Card(
        onClick = dropUnlessResumed { onChatClick() },
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .align(Alignment.BottomEnd), // Align to bottom-end
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorPrimary)), // Use Material theme color
        shape = CircleShape// You can customize the shape
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Image container with online status indicator
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Profile image
                Image(
                    painter = painter, // Load image using Coil or similar
                    contentDescription = "User profile image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape), // Circular shape,
                    contentScale = ContentScale.Crop
                )

                // Online status indicator
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = Color.Green, // Solid color for online status
                                shape = CircleShape // Clip to circle shape
                            )
                    )
                }
            }

            // Chat text
            Text(
                text = "Message",
                color = Color.White,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .align(Alignment.CenterVertically) // Center vertically in the row
            )
        }
    }
}



