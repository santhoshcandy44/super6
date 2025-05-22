package com.lts360.compose.ui.chat.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lts360.compose.ui.utils.FormatterUtils.humanReadableBytesSize

@Composable
fun DownloadMediaButton(
    fileSize: Long,
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = CircleShape
            ) // Button background
            .clickable {
                onDownloadClick()
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            if (fileSize > 0) {
                Text(
                    humanReadableBytesSize(fileSize), color = Color.White,
                    style = LocalTextStyle.current.copy(
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.CloudDownload,
                contentDescription = "Download Image",
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }

    }
}