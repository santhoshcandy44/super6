package com.lts360.compose.ui.services.manage


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.api.models.service.EditableService
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.common.ProfileNotCompletedPromptSheet
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.lts360.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServicesScreen(
    onCreatedNewService: () -> Unit,
    onNavigateUpCreateService: (String, Long) -> Unit,
    onManagePublishedService: (EditableService) -> Unit,
    onProfileSettings:()-> Unit,
    onBack:()->Unit,
    draftServicesViewModel: ServicesWorkflowViewModel,
    publishedServicesViewModel: PublishedServicesViewModel,
    tabTitles: List<String> = listOf("Draft", "Published")) {

    val isServiceCreated = false


    val pagerState = rememberPagerState(pageCount = { tabTitles.size }, initialPage = 0)
    val scrollToPageCoroutinePage = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var isShowingProfileNotCompletedBottomSheet by remember { mutableStateOf(false) }

    val isProfileCompleted by draftServicesViewModel.isProfileCompletedFlow.collectAsState(initial = false)
    val unCompletedProfileFieldsFlow by draftServicesViewModel.unCompletedProfileFieldsFlow.collectAsState(initial = listOf("EMAIL","PHONE"))


    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Manage Services",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }) { paddingValues ->


            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Create Service",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick =  if (isProfileCompleted) {
                            dropUnlessResumed {
                                onCreatedNewService()
                            }
                        } else {
                            {
                                isShowingProfileNotCompletedBottomSheet = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RectangleShape,

                        ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Choose Location",
                            modifier = Modifier
                                .padding(end = 8.dp)
                        )
                        Text(text = "Add New")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "All Services",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 0.dp,
                    indicator = {},
                    divider = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selectedContentColor = Color.Unspecified,
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scrollToPageCoroutinePage.launch {
                                    pagerState.scrollToPage(index)
                                }
                            },
                            modifier = Modifier
                                .padding(end = 8.dp) // Padding between tabs
                                .height(32.dp)
                                .clip(CircleShape) // Make the tab rounded
                                .background(
                                    if (pagerState.currentPage == index) {
                                        MaterialTheme.colorScheme.primary // Selected color (purple_500)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest // Default color (white)
                                    }
                                )
                                .border(
                                    1.dp, if (pagerState.currentPage == index) {
                                        MaterialTheme.colorScheme.primary // Selected color (purple_500)
                                    } else {
                                        Color.LightGray // Default color (gray)
                                    }, CircleShape
                                ),

                            text = {
                                Text(
                                    color = if (pagerState.currentPage == index) Color.White else MaterialTheme.colorScheme.onSurface,
                                    text = title,
                                )
                            },
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->

                    when (page) {
                        0 -> {
                            DraftServicesScreen ({ type, draftId ->
                                dropUnlessResumedV2(lifecycleOwner){
                                    onNavigateUpCreateService(type, draftId)
                                }
                            }, draftServicesViewModel)
                        }

                        1 -> {
                            PublishedServicesScreen(
                                isServiceCreated,
                                {
                                    isServiceCreated?.let {

                                    }
                                },
                                onManagePublishedService,
                                publishedServicesViewModel
                            )
                        }
                    }
                }
            }


            if (isShowingProfileNotCompletedBottomSheet) {
                ProfileNotCompletedPromptSheet(
                    unCompletedProfileFields = unCompletedProfileFieldsFlow,
                    onProfileCompleteClick = onProfileSettings,
                    onDismiss = {
                        isShowingProfileNotCompletedBottomSheet = false
                    })
            }
        }
    }

}

