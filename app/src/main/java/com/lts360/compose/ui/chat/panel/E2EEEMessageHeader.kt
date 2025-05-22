package com.lts360.compose.ui.chat.panel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun E2EEEMessageHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDEECFF))
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(8.dp)
        ) {


            val id = "inlineContent"
            val text = buildAnnotatedString {
                appendInlineContent(id, "[icon]")
                append( "This chat is protected by end-to-end encryption. All your messages are fully secure encrypted.")
            }

            val inlineContent = mapOf(
                Pair(id,
                    InlineTextContent(

                        Placeholder(
                            width = 16.sp,
                            height = 16.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {

                        Icon(imageVector = Icons.Filled.Lock,
                            contentDescription = "Encrypted",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            )

            Text(text = text,
                modifier = Modifier.fillMaxWidth(),
                inlineContent = inlineContent
            )


        }
    }
}