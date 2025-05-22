package com.lts360.compose.ui.chat.panel.reply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.app.database.models.chat.ChatMessageType
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.app.database.models.chat.ThumbnailLoader.getThumbnailBitmap
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds

@Composable
fun OverAllOtherRepliedMessageItem(
    message: MessageWithReply,
    userProfileInfo: FeedUserProfileInfo,
    viewModel: ChatViewModel,
    onRepliedMessageClicked: (Message) -> Unit
) {

    val fileMetadata = message.repliedMessageFileMeta

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        message.repliedToMessage?.let {

            when (it.type) {
                ChatMessageType.TEXT -> {
                    RepliedMessageContent(
                        it.content,
                        "You",
                        viewModel.formatMessageReceived(it.timestamp)
                    ) { onRepliedMessageClicked(it) }
                }

                ChatMessageType.IMAGE -> {
                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVisualMediaContent(
                            it.content,
                            fileMetadata.fileAbsolutePath ?: fileMetadata.fileThumbPath,
                            "You",
                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }

                }

                ChatMessageType.GIF -> {
                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVisualMediaContent(
                            it.content,
                            fileMetadata.fileAbsolutePath ?: fileMetadata.fileThumbPath,
                            "You",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }

                }

                ChatMessageType.AUDIO -> {
                    fileMetadata?.let { fileMetadata ->

                        RepliedMessageContent(
                            "${it.content} ${formatTimeSeconds(fileMetadata.totalDuration / 1000f)}",
                            "You",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }

                }

                ChatMessageType.VIDEO -> {

                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVideoMediaContent(
                            it.content,
                            getThumbnailBitmap(fileMetadata.thumbData),
                            fileMetadata.fileAbsolutePath,
                            "You",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }


                }

                ChatMessageType.FILE -> {

                    RepliedMessageContent(
                        it.content,
                        "You",

                        viewModel.formatMessageReceived(it.timestamp)
                    ) { onRepliedMessageClicked(it) }
                }
            }

        } ?: run {
            if (message.receivedMessage.replyId != -1L) {
                RepliedMessageContent(
                    "...",
                    "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                    ""
                ) {}
            }
        }
    }
}
