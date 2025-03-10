package com.lts360.compose.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lts360.test.NewsArticle


@Composable
fun DisCoverScreen() {

    val items : List<NewsArticle> = emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(
            8.dp
        ),
        contentPadding = PaddingValues(8.dp)
    ) {

        items(items) { item ->

            DiscoverItem(
                item.thumbnail,
                item.title,
                "15 Secs ago",
                "Cinemapettai",
                "https://www.cinemapettai.com/wp-content/uploads/2024/06/cp-fav-icon.png",
                {},
                {}
            ) {

            }

        }
    }

}


@Composable
fun DiscoverItem(
    imageUrl: String, // Replace with Image URL loading logic
    title: String,
    publishedDate: String,
    siteName: String,
    siteFavicon: String, // Replace with Favicon URL loading logic
    onShare: () -> Unit,
    onLike: () -> Unit,
    onMore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                imageUrl,
                contentDescription = "Featured Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)) {
                        AsyncImage(
                            siteFavicon,
                            contentDescription = "Site Favicon",
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = siteName, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = publishedDate, fontSize = 12.sp, color = Color.Gray)
                    }

                    Text("Technology")

                }
                Spacer(modifier = Modifier.height(8.dp))

                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        IconButton(
                            onClick = onLike,
                            modifier = Modifier
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info, contentDescription = "Like",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onLike,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.Comment,
                                contentDescription = "Like",
                                modifier = Modifier
                                    .size(16.dp)
                                    .size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(24.dp)

                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onMore,
                            modifier = Modifier
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert, contentDescription = "More",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

            }
        }
    }
}
