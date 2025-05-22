package com.lts360.compose.ui.chat.panel.reply

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.lts360.components.utils.isUriExist
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.utils.getThumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReplyMessageVideoMediaContent(
    selectedMessage: String,
    thumbnailBitmap: Bitmap?,
    filepath: String?,
    senderName: String,
    formattedTimeStamp: String,
    onCloseClicked: () -> Unit,
) {


    var thumbnail by remember { mutableStateOf(thumbnailBitmap) }

    val context = LocalContext.current


    LaunchedEffect(filepath) {
        filepath?.let {
            if (isUriExist(context, it.toUri())) {
                thumbnail = withContext(Dispatchers.IO) {
                    MediaMetadataRetriever().getThumbnail(context, it.toUri())
                }
            }
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        Column(modifier = Modifier.fillMaxWidth()) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Row(
                    modifier = Modifier.weight(1f),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),

                        text = buildAnnotatedString {
                            append("Reply to ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(senderName)
                            }
                        },
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTimeStamp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                    )
                }


                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    {
                        onCloseClicked()
                    },

                    modifier = Modifier
                        .then(Modifier.minimumInteractiveComponentSize())
                ) {

                    Icon(
                        imageVector = Icons.Default.Close, contentDescription = "Close"
                    )

                }
            }



            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween

            ) {

                Text(
                    text = selectedMessage,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                    modifier = Modifier.weight(1f)
                )
                thumbnail?.let {
                    Image(
                        it.asImageBitmap(),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Thumbnail media",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))

                    )
                }


            }


        }


    }

}
