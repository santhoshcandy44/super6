package com.lts360.compose.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lts360.R


@Composable
fun LocationWrapper(location: String?, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {

        Box(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .align(Alignment.CenterEnd), // Adjust according to your layout
            contentAlignment = Alignment.CenterEnd // Aligns children to the end

        ) {
            Row(
                modifier = Modifier
                    .clickable { onClick() }, // Set focusable to false
                verticalAlignment = Alignment.CenterVertically // Center contents vertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                if (location != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

            }
        }
    }


}