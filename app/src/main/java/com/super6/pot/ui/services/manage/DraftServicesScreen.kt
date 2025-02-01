package com.super6.pot.ui.services.manage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.super6.pot.ui.manage.services.viewmodels.ServicesWorkflowViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftServicesScreen(
    onNavigateCreateService: (String, Long) -> Unit,
    viewModel: ServicesWorkflowViewModel

    ) {

    val draftServices by viewModel.draftServices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()


    val scrollState = rememberScrollState()


    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            viewModel.onRefresh()
        },
        modifier = Modifier.fillMaxSize()
    ) {




        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            if (draftServices.isEmpty() && !isLoading) {
                Text(
                    text = "Oops, Nothing to see",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    strokeWidth = 4.dp
                )
            }
        }


        if (!isLoading) {

            // Display the list of services if available
            if (draftServices.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),

                    ) {
                    items(draftServices) { service ->

                        ServiceItem(
                            title = service.title,
                            shortDescription = service.shortDescription,
                            status = service.status,
                            onClick = { type, serviceId ->
                                onNavigateCreateService(type, serviceId)
                            },
                            type = service.status,
                            item = service
                        )

                    }
                }
            }
        }


    }
}


