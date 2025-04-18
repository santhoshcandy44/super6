package com.lts360.test


import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lts360.R
import com.lts360.compose.ui.NoRippleInteractionSource
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.libs.ui.ShortToast
import java.text.NumberFormat
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostings(
    viewModel: JobPostingsViewModel,
    isTopBarShowing: Boolean = true,
    onNavigateUp: () -> Unit
) {

    val userId = viewModel.userId

    val context = LocalContext.current

    val initialLoadState by viewModel.pageSource.initialLoadState.collectAsState()
    val isLoadingItems by viewModel.pageSource.isLoadingItems.collectAsState()
    val isRefreshingItems by viewModel.pageSource.isRefreshingItems.collectAsState()

    val items by viewModel.pageSource.items.collectAsState()

    val hasNetworkError by viewModel.pageSource.hasNetworkError.collectAsState()
    val hasAppendError by viewModel.pageSource.hasAppendError.collectAsState()
    val hasMoreItems by viewModel.pageSource.hasMoreItems.collectAsState()

    val lazyListState = rememberLazyListState()

    val lastLoadedItemPosition by viewModel.lastLoadedItemPosition.collectAsState()

    val connectivityManager = viewModel.connectivityManager

    val onlySearchBar = false
    val searchQuery = ""

    val currentFilters by viewModel.filters.collectAsState()
    val showFilters by viewModel.showFilters.collectAsState()


    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->
                // Check if the last item is visible
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull {
                    (it.key as? String)?.startsWith("job_") == true
                }?.index


                if (!isLoadingItems
                    && hasMoreItems
                    && !hasAppendError
                    && lastVisibleItemIndex != null
                    && lastVisibleItemIndex >= items.size - 1
                    && lastVisibleItemIndex >= lastLoadedItemPosition
                ) {

                    viewModel.updateLastLoadedItemPosition(if (lastLoadedItemPosition == -1) 0 else lastVisibleItemIndex)
                    viewModel.nextPage(
                        userId,
                        searchQuery
                    )
                }
            }
    }

    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {

        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.updateLastLoadedItemPosition(-1)
                viewModel.refresh(userId, searchQuery, currentFilters)
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {
                viewModel.pageSource.setNetWorkError(true)
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                viewModel.pageSource.setNetWorkError(true)
                viewModel.pageSource.setRefreshingItems(false)
                ShortToast(context, "No internet connection")
            }
        }
    }

    val onRefresh: () -> Unit = {
        viewModel.pageSource.setRefreshingItems(true)
        connectivityManager.checkForSeconds(Handler(Looper.getMainLooper()), statusCallback, 4000)
    }


    val onRetry = {
        viewModel.pageSource.setRefreshingItems(true)
        connectivityManager.checkForSeconds(
            Handler(Looper.getMainLooper()), statusCallback,
            4000
        )
    }

    val pullToRefreshState = rememberPullToRefreshState()



    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text("Job Openings", style = MaterialTheme.typography.titleLarge) },
            actions = {
                // Add filter/sort icons if needed
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize() // This makes the Box take up the entire available space
                    .pullToRefresh(
                        isRefreshingItems, pullToRefreshState,
                        enabled = !(initialLoadState && items.isEmpty()) && isTopBarShowing
                    ) {
                        onRefresh()
                    }
            ) {


                if (initialLoadState && items.isEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp), // Adjust the space between items
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (onlySearchBar) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize()) {
                                    CircularProgressIndicatorLegacy(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.primary

                                    )
                                }
                            }
                        } else {

                            item {
                                Box(modifier = Modifier.fillParentMaxSize()) {
                                    CircularProgressIndicatorLegacy(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.primary

                                    )
                                }
                            }
                        }
                    }

                } else {
                    // Handle no internet case
                    if (hasNetworkError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()) // Enables nested scrolling
                        ) {
                            NoInternetScreen(modifier = Modifier.align(Alignment.Center)) {
                                onRetry()
                            }
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                        ) {

                            item {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateShowFilters(true)
                                        },
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Show Filters"
                                        )
                                    }
                                }
                            }

                            if (currentFilters.hasValidFilters()) {
                                item {
                                    SelectedFiltersChips(
                                        filters = currentFilters,
                                        contentPadding = PaddingValues(0.dp),
                                        onFilterWorkModeRemoved = { value ->
                                            viewModel.updateJobFiltersAndRefresh(
                                                userId, searchQuery,
                                                currentFilters.copy(
                                                    workModes = currentFilters.workModes - value
                                                )
                                            )
                                        },
                                        onFilterSalaryRemoved = {
                                            viewModel.updateJobFiltersAndRefresh(
                                                userId, searchQuery,
                                                currentFilters.copy(
                                                    salaryRange = null
                                                )
                                            )
                                        }
                                    )
                                }

                            }

                            if (!isLoadingItems && !hasAppendError && items.isEmpty()) {

                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.align(Alignment.Center)
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.all_caught_up),
                                                contentDescription = "Image from drawable",
                                                modifier = Modifier.sizeIn(
                                                    maxWidth = 200.dp,
                                                    maxHeight = 200.dp
                                                )
                                            )
                                            Spacer(Modifier.height(16.dp))
                                            Text(text = "Oops, nothing to catch")
                                        }
                                    }
                                }


                            } else {
                                items(items, key = { "job_${it.id}" }) { job ->
                                    JobPostingCard(job, onItemClick = {
                                        viewModel.updateSelectedJobPosting(job)
                                        onNavigateUp()
                                    })
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (isLoadingItems) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            CircularProgressIndicatorLegacy(
                                                modifier = Modifier.padding(
                                                    16.dp
                                                ),
                                                color = MaterialTheme.colorScheme.primary

                                            )
                                        }
                                    }
                                }

                                // Handle errors for appending items
                                if (hasAppendError) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Text("Unable to load more.")

                                            Text(
                                                "Retry",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .clickable {
                                                        viewModel.retry(
                                                            userId,
                                                            searchQuery,
                                                            filters = currentFilters
                                                        )
                                                    }

                                            )
                                        }
                                    }
                                }

                                if (!hasMoreItems) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "You have reached the end",
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }


                        }

                    }

                }

                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshingItems,
                    state = pullToRefreshState
                )
            }

            if (showFilters) {
                JobFilterBottomSheet(
                    userCountryCode = viewModel.countryCode,
                    onDismiss = {
                        viewModel.updateShowFilters(false)
                    },
                    onFiltersApplied = { filters ->
                        viewModel.updateJobFiltersAndRefresh(userId, searchQuery, filters)
                        viewModel.updateShowFilters(false)
                    }
                )
            }


        }

    }
}

@Composable
private fun JobPostingCard(job: JobPosting, onItemClick: () -> Unit) {

    var isBookmarked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onItemClick,
        interactionSource = NoRippleInteractionSource()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = { isBookmarked = !isBookmarked },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = job.organization.name, // Replace with actual company name
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = job.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkModeChip(job.workMode)

                EmploymentTypeChip(job.employmentType)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Experience",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (job.experienceType) {
                            ExperienceType.FIXED -> "${job.experienceFixed} yrs"
                            ExperienceType.MIN_MAX -> "${job.experienceRangeMin}-${job.experienceRangeMax} yrs"
                            ExperienceType.FRESHER -> "Fresher"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = "Salary",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (job.salaryNotDisclosed) "Not Disclosed"
                        else "${job.salaryMinFormatted} - ${job.salaryMaxFormatted}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Skills: ${job.mustHaveSkills.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.formattedPostedBy(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkModeChip(workMode: WorkMode) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = "Work mode",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (workMode) {
                    WorkMode.REMOTE -> "Remote"
                    WorkMode.HYBRID -> "Hybrid"
                    WorkMode.OFFICE -> "Office"
                    WorkMode.FLEXIBLE -> "Flexible"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmploymentTypeChip(employmentType: EmploymentType) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Employment type",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (employmentType) {
                    EmploymentType.FULL_TIME -> "Full Time"
                    EmploymentType.PART_TIME -> "Part Time"
                    EmploymentType.INTERNSHIP -> "Internship"
                    EmploymentType.CONTRACT -> "Contract"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}


@Composable
private fun SelectedFiltersChips(
    filters: JobFilters,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    onFilterSalaryRemoved: () -> Unit,
    onFilterWorkModeRemoved: (WorkMode) -> Unit
) {
    if (filters.workModes.isNotEmpty() || filters.salaryRange != null) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = contentPadding
        ) {
            // Work Mode Chips
            items(filters.workModes.toList()) { workMode ->
                Chip(
                    text = workMode.displayName,
                    onRemove = {
                        onFilterWorkModeRemoved(workMode)
                    },
                    enableRemoveButton = true
                )
            }

            // Salary Range Chip
            filters.salaryRange?.let { range ->
                item {
                    Chip(
                        text = range.displayName,
                        onRemove = {
                            onFilterSalaryRemoved()
                        },
                        enableRemoveButton = true
                    )
                }
            }
        }
    }
}


@Composable
private fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit = {},
    enableRemoveButton: Boolean = false
) {
    Surface(
        modifier = modifier.wrapContentSize(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { onRemove() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )

            if (enableRemoveButton) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove skill",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

enum class FilterCategory {
    WORK_MODE, SALARY_RANGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFilterBottomSheet(
    userCountryCode: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onFiltersApplied: (JobFilters) -> Unit
) {
    var filters by remember { mutableStateOf(JobFilters()) }
    var selectedCategory by remember { mutableStateOf(FilterCategory.WORK_MODE) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RectangleShape,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier.safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Jobs",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (filters.hasValidFilters()) {

                    SelectedFiltersChips(
                        filters = filters,
                        onFilterWorkModeRemoved = {
                            filters = filters.copy(workModes = filters.workModes - it)
                        },
                        onFilterSalaryRemoved = {
                            filters = filters.copy(salaryRange = null)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }


                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    Column(
                        modifier = Modifier
                            .width(120.dp)
                            .fillMaxHeight()

                    ) {
                        FilterCategory.entries.forEach { category ->
                            CategoryTab(
                                category = category,
                                isSelected = category == selectedCategory,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        when (selectedCategory) {
                            FilterCategory.WORK_MODE -> WorkModeSection(
                                selectedModes = filters.workModes,
                                onModeSelected = { mode ->
                                    filters = filters.copy(
                                        workModes = if (filters.workModes.contains(mode)) {
                                            filters.workModes - mode
                                        } else {
                                            filters.workModes + mode
                                        }
                                    )
                                }
                            )

                            FilterCategory.SALARY_RANGE -> SalaryRangeSection(
                                userCountryCode = userCountryCode,
                                selectedRange = filters.salaryRange,
                                onRangeSelected = { range ->
                                    filters = filters.copy(
                                        salaryRange = if (filters.salaryRange == range) null else range
                                    )
                                }
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(8.dp)

                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onFiltersApplied(filters) },
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(8.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

@Composable
private fun CategoryTab(
    category: FilterCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Text(
            text = when (category) {
                FilterCategory.WORK_MODE -> "Work Mode"
                FilterCategory.SALARY_RANGE -> "Salary"
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun WorkModeSection(
    selectedModes: Set<WorkMode>,
    onModeSelected: (WorkMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val workModes = remember {
        listOf(WorkMode.REMOTE, WorkMode.OFFICE, WorkMode.HYBRID, WorkMode.FLEXIBLE)
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Select Work Mode",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            workModes.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onModeSelected(mode) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedModes.contains(mode),
                        onClick = { onModeSelected(mode) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
private fun SalaryRangeSection(
    userCountryCode:String,
    selectedRange: SalaryRange?,
    onRangeSelected: (SalaryRange) -> Unit,
    modifier: Modifier = Modifier
) {

    val (currency, ranges) = remember(userCountryCode) {
        when (userCountryCode) {
            "IN" -> Currency.getInstance("INR") to listOf(2_00_000, 3_00_000, 6_00_000, 12_00_000)
            "US" -> Currency.getInstance("USD") to listOf(50_000, 100_000, 150_000)
            "GB" -> Currency.getInstance("GBP") to listOf(40_000, 80_000, 120_000)
            "FR" -> Currency.getInstance("EUR") to listOf(35_000, 70_000, 100_000)
            else -> Currency.getInstance("INR") to listOf(2_00_000, 3_00_000, 6_00_000, 12_00_000)
        }
    }

    val currencyFormatter = remember(currency) {
        NumberFormat.getCurrencyInstance().apply {
            this.currency = currency
            maximumFractionDigits = 0
        }
    }

    val salaryRanges = remember(ranges, currencyFormatter) {
        listOf(
            SalaryRange("Under ${currencyFormatter.format(ranges[0])}", 0, ranges[0]),
            SalaryRange(
                "${currencyFormatter.format(ranges[0])} - ${currencyFormatter.format(ranges[1])}",
                ranges[0],
                ranges[1]
            ),
            SalaryRange(
                "${currencyFormatter.format(ranges[1])} - ${currencyFormatter.format(ranges[2])}",
                ranges[1],
                ranges[2]
            ),
            SalaryRange("Over ${currencyFormatter.format(ranges[2])}", ranges[2], Int.MAX_VALUE)
        )
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = "Select Salary Range",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            salaryRanges.forEach { range ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRangeSelected(range) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRange == range,
                        onClick = { onRangeSelected(range) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = range.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


data class JobFilters(
    val workModes: Set<WorkMode> = emptySet(),
    val salaryRange: SalaryRange? = null
)

fun JobFilters.hasValidFilters(): Boolean {
    return workModes.isNotEmpty() || salaryRange != null
}

data class SalaryRange(
    val displayName: String,
    val min: Int,
    val max: Int
)

