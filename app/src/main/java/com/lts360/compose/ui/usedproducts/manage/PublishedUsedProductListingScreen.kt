package com.lts360.compose.ui.usedproducts.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import com.lts360.api.utils.ResultError
import com.lts360.api.models.service.EditableUsedProductListing
import com.lts360.compose.ui.services.manage.NoInternetText
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishedUsedProductListingScreen(
    isSecondsCreated:Boolean?,
    onRemoveServiceCreatedState:()->Unit,
    onNavigateUp: (EditableUsedProductListing) -> Unit,
    viewModel: PublishedUsedProductsListingViewModel) {

    val userId = viewModel.userId
    val publishedSeconds by viewModel.publishedUsedProductListings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val resultError by viewModel.resultError.collectAsState()

    LaunchedEffect(isSecondsCreated){
        isSecondsCreated?.let {
            viewModel.refreshPublishedSeconds(userId)
            onRemoveServiceCreatedState()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        if (!isLoading) {
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { viewModel.refreshPublishedSeconds(userId) },
                modifier = Modifier.fillMaxSize()
            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (publishedSeconds.isNotEmpty()) {

                        item{
                            Text(
                                text = "All Published Seconds",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(publishedSeconds) { publishedUsedProductListing ->
                            UsedProductListingItem(
                                title = publishedUsedProductListing.name,
                                shortDescription = publishedUsedProductListing.description,
                                status = publishedUsedProductListing.status,
                                onClick = { _, serviceId ->
                                    viewModel.setSelectedSeconds(serviceId)
                                    onNavigateUp(publishedUsedProductListing)
                                },
                                type = publishedUsedProductListing.status,
                                item = publishedUsedProductListing
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

        // Progress Bar
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                strokeWidth = 4.dp
            )
        }
    }
}


@Composable
fun UsedProductListingItem(
    title: String,
    shortDescription: String,
    status: String,
    onClick: (String, Long) -> Unit, // onClick callback
    type: String,
    item: EditableUsedProductListing,
) {
    // Outer container with vertical margin and rounded background
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = dropUnlessResumed {
            onClick(type, item.productId)
        }
    ) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)){
            AsyncImage(item.images.firstOrNull()?.imageUrl, contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(120.dp).background(Color.LightGray))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                // Title text
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Short description text
                Text(
                    text = shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                // Status text with dynamic color
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (status == "Active")  Color(0xFF00C000)  else if(status=="In-Review") Color(0xFFFFA500)  else Color.Red // Conditional color
                )
            }
        }

    }

}
