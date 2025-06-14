package com.lts360.compose.ui.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.compose.ui.chat.panel.StateSyncingModifier
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.models.ServiceReview
import com.lts360.compose.ui.main.models.ServiceReviewReply
import com.lts360.compose.ui.serviceReviewsFormatTimestamp
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.utils.ScrollBarConfig
import com.lts360.compose.utils.verticalScrollWithScrollbar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsList(
    isReviewsLoading: Boolean,
    isReviewsPosting: Boolean,
    serviceReviews: List<ServiceReview>,
    onSendComment: (String) -> Unit,
    onReplyCommentClicked: (ServiceReview) -> Unit
) {


    var value by remember { mutableStateOf("") }
    val textFieldState = remember {

        TextFieldState(
            initialText = value,
            // Initialize the cursor to be at the end of the field.
            initialSelection = TextRange(value.length)
        )
    }


    LaunchedEffect(isReviewsPosting) {
        if (!isReviewsPosting) {
            textFieldState.clearText()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isReviewsLoading) {

            CircularProgressIndicatorLegacy(
                modifier = Modifier.align(Alignment.Center)
            )

        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item {
                        Text(
                            text = "Comments",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(serviceReviews) { comment ->

                        CommentItem(comment = comment) { commentId ->

                            onReplyCommentClicked(comment)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier)  // This will add space after the last item
                    }
                }


                CommentTextField(
                    isReviewsPosting,
                    value,
                    textFieldState,
                    onValueChange = {
                        value = it
                    },
                    onSendComment = {
                        onSendComment(value.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }


}

@Composable
fun CommentItem(comment: ServiceReview, onReplyClick: (Int) -> Unit) {
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
                comment.user.profilePicUrl,
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
                        text = comment.user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${serviceReviewsFormatTimestamp(comment.timestamp)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Text(
                    text = comment.text,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically

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
                                onReplyClick(comment.id)
                            },
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                    )

                }

            }

        }
    }
}

@Composable
fun CommentTextField(
    isReviewsPosting: Boolean,
    value: String,
    state: TextFieldState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSendComment: () -> Unit,
    onCloseNestedReply: () -> Unit = {},
    selectedReply: ServiceReviewReply? = null,
    // and other arguments you want to delegate
) {


    val scrollState = rememberScrollState()

    // This is effectively a rememberUpdatedState, but it combines the updated state (text) with
    // some state that is preserved across updates (selection).
    var valueWithSelection by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        )
    }
    valueWithSelection = valueWithSelection.copy(text = value)


    // EditText for message input
    BasicTextField(
        state = state,
//                    onValueChange = { viewModel.onMessageValueChange(it) },
        textStyle = TextStyle.Default.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        ),

        modifier = modifier.then(
            StateSyncingModifier(
                state = state,
                value = valueWithSelection,
                onValueChanged = {
                    // Don't fire the callback if only the selection/cursor changed.
                    if (it.text != valueWithSelection.text) {
                        onValueChange(it.text)
                    }
                    valueWithSelection = it
                },
                writeSelectionFromTextFieldValue = false
            )

                .background(
                    MaterialTheme.customColorScheme.searchBarColor,
                    RoundedCornerShape(20.dp)
                )
                .heightIn(min = 48.dp)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ),
        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
        scrollState = scrollState,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        decorator = {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                selectedReply?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            it.user.fullName,
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = onCloseNestedReply, modifier =
                            Modifier
                                .size(16.dp)
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "close",
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                    }

                }




                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScrollWithScrollbar(
                            scrollState,
                            scrollbarConfig = ScrollBarConfig()
                        ),
                    contentAlignment = Alignment.CenterStart

                ) {
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                "Type comment",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        it()
                    }

                }


                if (state.text.isNotEmpty() && !isReviewsPosting) {
                    IconButton(
                        onClick = onSendComment, modifier =
                        Modifier
                            .size(32.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                        )
                    }

                    Spacer(Modifier.width(4.dp))
                }

                if (isReviewsPosting) {
                    CircularProgressIndicatorLegacy(
                        Modifier
                            .size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }

        })


}

