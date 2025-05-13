package com.lts360.compose.ui.localjobs.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lts360.api.utils.ResultError
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.localjobs.manage.viewmodels.PublishedLocalJobViewModel
import com.lts360.compose.ui.localjobs.models.EditableLocalJob
import com.lts360.compose.ui.services.manage.NoInternetText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishedLocalJobScreen(
    isLocalJobCreated: Boolean?,
    onRemoveLocalJobCreatedSavedState: () -> Unit,
    onNavigateUpManagePublishedLocalJob: () -> Unit,
    onNavigateUpPublishedLocalJobViewApplicants: () -> Unit,

    viewModel: PublishedLocalJobViewModel
) {

    val userId = viewModel.userId
    val publishedSeconds by viewModel.publishedLocalJobs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val resultError by viewModel.resultError.collectAsState()

    LaunchedEffect(isLocalJobCreated) {
        isLocalJobCreated?.let {
            viewModel.refreshPublishedItems(userId)
            onRemoveLocalJobCreatedSavedState()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        if (!isLoading) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshPublishedItems(userId) },
                modifier = Modifier.fillMaxSize()
            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (publishedSeconds.isNotEmpty()) {

                        item {
                            Text(
                                text = "All Published Local Jobs",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(publishedSeconds) { item ->
                            LocalJobItem(
                                item = item,
                                onEditClick = {
                                    viewModel.setSelectedItem(it)
                                    onNavigateUpManagePublishedLocalJob()
                                },
                                onViewApplicantsClick = {
                                    viewModel.setSelectedItemAndLoadApplicants(it)
                                    onNavigateUpPublishedLocalJobViewApplicants()
                                }
                            )
                        }
                    }

                    if (resultError is ResultError.NoInternet) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                NoInternetText(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                    } else {

                        if (resultError != null) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize()) {
                                    Text(
                                        text = "Something went wrong",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(8.dp)
                                    )
                                }
                            }
                        } else {
                            if (publishedSeconds.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxSize()) {
                                        Text(
                                            text = "Oops, Nothing to see",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                    }


                }
            }
        }

        if (isLoading) {
            CircularProgressIndicatorLegacy(
                modifier = Modifier.align(Alignment.Center),
                strokeWidth = 4.dp
            )
        }
    }
}


@Composable
fun LocalJobItem(
    item: EditableLocalJob,
    onEditClick: (Long) -> Unit,
    onViewApplicantsClick: (Long) -> Unit
) {

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                item.images.firstOrNull()?.imageUrl, contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.status == "Active") Color(0xFF00C000) else if (item.status == "In-Review") Color(
                        0xFFFFA500
                    ) else Color.Red
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF8AB4F8))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable { onEditClick(item.localJobId) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Edit",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onViewApplicantsClick(item.localJobId)
                }) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View Applicants",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "View Applicants",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,

                    )
            }
        }

    }

}
