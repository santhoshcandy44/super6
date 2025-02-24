package com.lts360.compose.ui.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.models.ServiceReview
import com.lts360.compose.ui.main.models.ServiceReviewReply
import com.lts360.compose.ui.serviceReviewsFormatTimestamp


@Composable
fun ReplyScreen(
    comment: ServiceReview,
    isReplyPosting: Boolean,
    isReviewsReplyLoading: Boolean,
    replies: List<ServiceReviewReply>,
    navigateBack: () -> Unit,
    onSendComment: (String, ServiceReviewReply?) -> Unit
) {


    var selectedReply by remember { mutableStateOf<ServiceReviewReply?>(null) }

    var value by remember { mutableStateOf("") }

    val textFieldState = remember {
        TextFieldState(
            initialText = value,
            // Initialize the cursor to be at the end of the field.
            initialSelection = TextRange(value.length)
        )
    }

    LaunchedEffect(isReplyPosting) {
        if (!isReplyPosting) {
            textFieldState.clearText()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)

            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {

                        AsyncImage(
                            comment.user.profilePicUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )

                        Spacer(Modifier.width(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {

                            // Display the comment info and its replies
                            Text(
                                text = comment.user.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = comment.text,
                            )
                        }
                    }
                }



                if (isReviewsReplyLoading) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize()) {
                            CircularProgressIndicatorLegacy(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                } else {

                    if (replies.isNotEmpty()) {
                        item {
                            Text(
                                text = "Replies",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }

                        items(replies) { reply ->
                            ReplyItem(reply = reply) {
                                selectedReply = reply
                            }
                        }
                    }

                }


            }

            // Button to close the Bottom Sheet
            IconButton(onClick = navigateBack, modifier = Modifier.align(Alignment.BottomEnd)) {

                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            CommentTextField(
                isReplyPosting,
                value,
                textFieldState,
                onValueChange = {
                    value = it
                },
                onSendComment = {
                    onSendComment(value.trim(), selectedReply)
                },
                onCloseNestedReply = {
                    selectedReply = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                selectedReply = selectedReply
            )
        }


    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReplyItem(reply: ServiceReviewReply, onNestedReplyClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            AsyncImage(
                reply.user.profilePicUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                // Display Comment Info with improved typography


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reply.user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${serviceReviewsFormatTimestamp(reply.timestamp)}",
                        style = MaterialTheme.typography.titleSmall,

                        )
                }


                Column(modifier = Modifier.fillMaxWidth()) {
                    if (reply.replyToFullName != null) {
                        Text(
                            text = "@${reply.replyToFullName}",
                            color = Color(
                                0xFF4B91F0
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = reply.text)
                }


                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.wrapContentWidth()

                ) {

                    Image(
                        painterResource(R.drawable.like_outlined),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                            },
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                    )



                    Spacer(modifier = Modifier.width(8.dp))

                    Image(
                        painterResource(R.drawable.ic_comment),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                onNestedReplyClicked()
                            },
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                    )


                }

            }

        }
    }
}