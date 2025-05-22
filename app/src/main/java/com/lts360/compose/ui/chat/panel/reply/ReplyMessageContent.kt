package com.lts360.compose.ui.chat.panel.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lts360.compose.ui.theme.customColorScheme

@Composable
fun ReplyMessageContent(
    selectedMessage: String,
    senderName: String,
    formattedTimeStamp: String,
    onCloseClicked: () -> Unit,
) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {  },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

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
            )

        }


    }

}
