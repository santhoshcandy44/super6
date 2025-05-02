package com.lts360.compose.ui.services.manage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.api.utils.ResultError
import com.lts360.api.models.service.EditableService
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishedServicesScreen(
    isServiceCreated:Boolean?,
    onRemoveServiceCreatedState:()->Unit,
    onNavigateUp: (EditableService) -> Unit,
    viewModel: PublishedServicesViewModel
) {

    val userId = viewModel.userId
    val publishedServices by viewModel.services.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val resultError by viewModel.resultError.collectAsState()

    LaunchedEffect(isServiceCreated){
        isServiceCreated?.let {
            viewModel.refreshPublishedServices(userId)
            onRemoveServiceCreatedState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (!isLoading) {

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { viewModel.refreshPublishedServices(userId) },
                modifier = Modifier.fillMaxSize()
            ) {

                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (publishedServices.isNotEmpty()) {
                        items(publishedServices) { service ->
                            ServiceItem(
                                title = service.title,
                                shortDescription = service.shortDescription,
                                status = service.status,
                                onClick = { _, serviceId ->
                                    viewModel.setSelectedService(serviceId)
                                    onNavigateUp(service)

                                },
                                type = service.status,
                                item = service
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
                            if (publishedServices.isEmpty()) {
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
fun NoInternetText(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            text = "No internet connection",
            color = Color.Red,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun ServiceItem(
    title: String,
    shortDescription: String,
    status: String,
    onClick: (String, Long) -> Unit,
    type: String,
    item: EditableService,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = dropUnlessResumed {
            onClick(type, item.serviceId)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (status == "draft") Color.Red else Color(0xFF00C000)// Conditional color
            )
        }
    }

}

