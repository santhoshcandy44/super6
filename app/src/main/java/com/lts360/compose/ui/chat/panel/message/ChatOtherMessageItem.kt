package com.lts360.compose.ui.chat.panel.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.utils.ExpandableText

@Composable
fun ChatOtherMessageItem(
    viewModel: ChatViewModel,
    userName: String,
    profileIUrl: String?,
    message: String,
    timestamp: String,
) {

    val context = LocalContext.current

    Row(modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(start = 8.dp),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(8.dp))

                AsyncImage(
                    ImageRequest.Builder(context)
                        .data(profileIUrl)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .crossfade(true)
                        .build()
                    ,
                    imageLoader = viewModel.chatUsersProfileImageLoader,
                    contentDescription = "User Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

            }

            Spacer(modifier = Modifier.height(8.dp))


            Box(modifier = Modifier.wrapContentSize()
                .background(
                    Color(0xFF4399FF),
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
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    showMoreStyle = SpanStyle(color = Color.Black),
                    showLessStyle = SpanStyle(color = Color.Black),
                    textModifier = Modifier.wrapContentSize(),

                )
            }

            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

