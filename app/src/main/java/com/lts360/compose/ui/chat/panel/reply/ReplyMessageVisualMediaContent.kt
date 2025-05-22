package com.lts360.compose.ui.chat.panel.reply

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.lts360.compose.ui.theme.customColorScheme


@Composable
fun ReplyMessageVisualMediaContent(
    selectedMessage: String,
    thumbnailPath: String?,
    senderName: String,
    formattedTimeStamp: String,
    onCloseClicked: () -> Unit,
) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Padding inside the border

        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon for replying
        IconButton(
            onClick = { /* Handle reply action here */ },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        // Reply Message content, like showing the selected message
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // Reply header with the name and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Text content taking up space
                Row(
                    modifier = Modifier.weight(1f),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),

                        text = buildAnnotatedString {
                            // Apply bold style to "Replying to"

                            append("Reply to ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(senderName)
                            }
                            // Append the rest normally
                        },
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp
                    Text(
                        text = formattedTimeStamp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                    )
                }


                Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp

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
                horizontalArrangement = Arrangement.SpaceBetween // This ensures the image is placed at the end

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

                Image(
                    rememberAsyncImagePainter(thumbnailPath),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Reply media",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)) // Rounded corners with 8dp radius

                )
            }


        }


    }

}