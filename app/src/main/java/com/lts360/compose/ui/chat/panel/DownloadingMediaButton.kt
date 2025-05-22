package com.lts360.compose.ui.chat.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lts360.compose.ui.utils.FormatterUtils.humanReadableBytesSize

@Composable
fun BoxScope.DownloadingMediaButton(
    fileSize: Long,
    downloadedSize: Long,
    onDownloadCancel: () -> Unit,
) {

    val progressPercentage = if (fileSize > 0) {
        ((downloadedSize.toFloat() / fileSize.toFloat()) * 100).toInt()
    } else {
        0
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.BottomEnd)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ) // Button background
            .clickable {
                onDownloadCancel()
            }
            .padding(8.dp)
    ) {


        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                "${humanReadableBytesSize(downloadedSize)}/${humanReadableBytesSize(fileSize)}",
                color = Color.White,
                style = LocalTextStyle.current.copy(
                    fontSize = 12.sp
                )
            )

            Spacer(Modifier.width(4.dp))


            Box {
                CircularProgressIndicator(
                    progress = { progressPercentage / 100f },
                    gapSize = 0.dp,
                    strokeWidth = 2.dp,
                    strokeCap = StrokeCap.Square,
                    modifier = Modifier.size(32.dp)
                )

                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Download Image",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(18.dp),
                    tint = Color.White
                )
            }


        }

    }
}
