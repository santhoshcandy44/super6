package com.lts360.compose.ui.chat.panel.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lts360.R
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.compose.utils.ExpandableText

@Composable
fun ChatMeMessageItem(
    userName: String,
    message: String,
    timestamp: String,
    status: ChatMessageStatus,
) {


    Column(modifier = Modifier
        .fillMaxWidth(0.8f)) {

        Text(
            text = userName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            maxLines = 1
        )

        Box(modifier = Modifier.wrapContentSize()
            .background(
                Color(0xFFECECEA),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 0.dp
                )
            )
            .padding(vertical = 4.dp, horizontal = 16.dp)
        ){

            ExpandableText(
                message,
                style = MaterialTheme.typography.bodyMedium,
                showMoreStyle = SpanStyle(color = Color(0xFF4399FF)),
                showLessStyle = SpanStyle(color = Color(0xFF4399FF)),
                textModifier = Modifier.wrapContentSize()
            )
        }



        Spacer(modifier = Modifier.height(4.dp))

        when (status) {
            ChatMessageStatus.SENDING, ChatMessageStatus.QUEUED -> {
                Icon(
                    painter = painterResource(R.drawable.ic_message_pending),
                    contentDescription = "Sending",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start)
                )
            }

            ChatMessageStatus.SENT -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Message Sent",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start)
                )
            }

            ChatMessageStatus.DELIVERED -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Message Delivered",
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Message Delivered",
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 8.dp)
                    )
                }


            }

            ChatMessageStatus.READ -> {}
            ChatMessageStatus.FAILED -> {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Message Delivered",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Red
                )
            }

            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {}
            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {}
            else -> {}

        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = timestamp,
            color = Color(0xFFC0C0C0),
            fontSize = 10.sp,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}
