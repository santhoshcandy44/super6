package com.lts360.compose.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
private sealed class ChooseMediaLibraryIcon(
    @Contextual val icon: ImageVector,
    @Contextual val color: Color
) {
    @Serializable
    data object Camera : ChooseMediaLibraryIcon(Icons.Default.Camera, Color(0xFF2196F3))

    @Serializable
    data object Library : ChooseMediaLibraryIcon(Icons.Default.LibraryAdd, Color(0xFF4CAF50))

    @Serializable
    data object Gallery : ChooseMediaLibraryIcon(Icons.Default.Photo, Color(0xFF8B5DFF))

    @Serializable
    data object Image : ChooseMediaLibraryIcon(Icons.Default.Image, Color(0xFFFF4081))

    @Serializable
    data object Video : ChooseMediaLibraryIcon(Icons.Default.VideoLibrary, Color(0xFFFF5722))

    @Serializable
    data object Folder : ChooseMediaLibraryIcon(Icons.Default.Folder, Color(0xFF9C27B0))
}

@Composable
fun AnimatedChooseMediaLibrary(isVisible: Boolean,
                               onChooseLibrary: () -> Unit,
                               onChooseGallery: () -> Unit,
                               onChooseCamera: () -> Unit) {

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(animationSpec = tween(600)) + fadeIn(),
        exit = fadeOut() + slideOutVertically(animationSpec = tween(400))
    ) {

        Card(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {

                // Convert into a list
                val mediaLibraryOptions = listOf(
                    ChooseMediaLibraryIcon.Camera,
                    ChooseMediaLibraryIcon.Gallery,
                    ChooseMediaLibraryIcon.Library,
                    /*
                    ChooseMediaLibraryIcon.Image,
*/
                /*    ChooseMediaLibraryIcon.Video,
                    ChooseMediaLibraryIcon.Folder*/
                )

                items(mediaLibraryOptions) { mediaLibraryOption ->
                    CircularIcon(mediaLibraryOption) {

                        when (mediaLibraryOption) {

                            ChooseMediaLibraryIcon.Camera -> onChooseCamera()

                            ChooseMediaLibraryIcon.Folder -> {}
                            ChooseMediaLibraryIcon.Image -> {}
                            ChooseMediaLibraryIcon.Gallery -> onChooseGallery()
                            ChooseMediaLibraryIcon.Library -> onChooseLibrary()

                            ChooseMediaLibraryIcon.Video -> {}
                        }

                    }
                }

            }
        }

    }
}


@Composable
private fun CircularIcon(mediaLibraryOption: ChooseMediaLibraryIcon, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = mediaLibraryOption.icon,
            contentDescription = null,
            tint = mediaLibraryOption.color
        )
    }
}