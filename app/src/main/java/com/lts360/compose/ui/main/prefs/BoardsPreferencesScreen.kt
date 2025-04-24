package com.lts360.compose.ui.main.prefs

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.api.utils.ResultError
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.prefs.viewmodels.BoardPref
import com.lts360.compose.ui.main.prefs.viewmodels.BoardPreferencesViewModel
import com.lts360.compose.ui.main.prefs.viewmodels.GuestBoardPreferencesViewModel
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.libs.ui.ShortToast
import kotlin.math.ceil
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestBoardsPreferencesScreen(
    viewModel: GuestBoardPreferencesViewModel = hiltViewModel(),
    onBackGo: () -> Unit
) {


    val context = LocalContext.current

    val allBoards by viewModel.allBoards.collectAsState()
    val selectedBoards = allBoards
        .filter { it.isSelected }
        .sortedBy { it.displayOrder }


    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val error by viewModel.error.collectAsState()

    val connectivityManager = viewModel.connectivityManager


    val onRetry = {

        if (connectivityManager.isConnectedInternet) {

            viewModel.onGetBoards(
                viewModel.userId
            ) {
                ShortToast(context, it)
            }

        } else {
            ShortToast(context, "No internet connection")
        }
    }

    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.onGetBoards(
                    viewModel.userId,
                    isLoading = false,
                    isRefreshing = true
                ) { errorMessage ->
                    ShortToast(context, errorMessage)
                }
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {}

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                viewModel.setRefreshing(false)
                ShortToast(context, "No internet connection")
            }
        }
    }

    val onRefresh = {
        viewModel.setRefreshing(true)
        connectivityManager.checkForSeconds(Handler(Looper.getMainLooper()), statusCallback, 4000)
    }

    val pullToRefreshState = rememberPullToRefreshState()


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            dropUnlessResumed {
                                onBackGo()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .pullToRefresh(isRefreshing, pullToRefreshState) {
                        onRefresh()
                    }
            ) {

                if (isLoading) {
                    CircularProgressIndicatorLegacy(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                } else {

                    if (error is ResultError.NoInternet) {

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            Text(
                                "Internet connection failed",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Retry",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    onRetry()
                                }
                            )
                        }

                    } else if (error != null && allBoards.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "Something went wrong",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    } else {

                        BoardsPreferencesSelectionScreen(
                            allBoards = allBoards,
                            selectedBoards = selectedBoards,
                            onBoardSelected = { boardId ->
                                viewModel.updateBoardSelectionStatus(
                                    boardId
                                )
                            },
                            onBoardDeselected = { boardId -> viewModel.deselectBoard(boardId) },
                            onBoardOrderChanged = { id, to ->
                                viewModel.boardOrderChange(id, to)
                            },
                            onUpdateBoards = {

                                if (viewModel.validateSelectedBoards()) {
                                    viewModel.onUpdateBoards(
                                        allBoards,
                                        onSuccess = {
                                            ShortToast(context, it)
                                        },
                                        onError = {
                                            ShortToast(context, it)
                                        }
                                    )
                                } else {
                                    ShortToast(context, "At least 1 board must be selected")
                                }

                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                }
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState
                )


            }
        }

        if (isUpdating) {
            LoadingDialog()
        }

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardsPreferencesScreen(
    viewModel: BoardPreferencesViewModel = hiltViewModel(),
    onBackGo: () -> Unit
) {


    val context = LocalContext.current

    val allBoards by viewModel.allBoards.collectAsState()
    val selectedBoards = allBoards
        .filter { it.isSelected }
        .sortedBy { it.displayOrder }


    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val error by viewModel.error.collectAsState()

    val connectivityManager = viewModel.connectivityManager


    val onRetry = {

        if (connectivityManager.isConnectedInternet) {

            viewModel.onGetBoards(
                viewModel.userId) {
                ShortToast(context, it)

            }

        } else {
            ShortToast(context, "No internet connection")
        }
    }

    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.onGetBoards(
                    viewModel.userId,
                    isLoading = false,
                    isRefreshing = true
                ) { errorMessage ->
                    ShortToast(context, errorMessage)
                }
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {}

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                viewModel.setRefreshing(false)
                ShortToast(context, "No internet connection")
            }
        }
    }

    val onRefresh = {
        viewModel.setRefreshing(true)
        connectivityManager.checkForSeconds(Handler(Looper.getMainLooper()), statusCallback, 4000)
    }

    val pullToRefreshState = rememberPullToRefreshState()


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            dropUnlessResumed {
                                onBackGo()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .pullToRefresh(isRefreshing, pullToRefreshState) {
                        onRefresh()
                    }
            ) {

                if (isLoading) {
                    CircularProgressIndicatorLegacy(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                } else {

                    if (error is ResultError.NoInternet) {

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            Text(
                                "Internet connection failed",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Retry",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    onRetry()
                                }
                            )
                        }

                    } else if (error != null && allBoards.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "Something went wrong",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    } else {

                        BoardsPreferencesSelectionScreen(
                            allBoards = allBoards,
                            selectedBoards = selectedBoards,
                            onBoardSelected = { boardId ->
                                viewModel.updateBoardSelectionStatus(
                                    boardId
                                )
                            },
                            onBoardDeselected = { boardId -> viewModel.deselectBoard(boardId) },
                            onBoardOrderChanged = { id, to ->
                                viewModel.boardOrderChange(id, to)
                            },
                            onUpdateBoards = {
                                if (viewModel.validateSelectedBoards()) {
                                    viewModel.onUpdateBoards(
                                        viewModel.userId,
                                        allBoards,
                                        onSuccess = {
                                            ShortToast(context, it)
                                        },
                                        onError = {
                                            ShortToast(context, it)

                                        }
                                    )
                                } else {
                                    ShortToast(context, "At least 1 board must be selected")
                                }

                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                }
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState
                )


            }
        }

        if (isUpdating) {
            LoadingDialog()
        }

    }


}


@Composable
private fun BoardsPreferencesSelectionScreen(
    allBoards: List<BoardPref>,
    selectedBoards: List<BoardPref>,
    onBoardSelected: (Int) -> Unit,
    onBoardDeselected: (Int) -> Unit,
    onBoardOrderChanged: (Int, Int) -> Unit,
    onUpdateBoards: () -> Unit,
    modifier: Modifier = Modifier
) {

    val gridSize = 80.dp
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        // Combined Grid for both selected and available boards
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = gridSize),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {

            // Available Boards Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Customize Your Boards",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Selected Boards Section
            if (selectedBoards.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Selected Boards",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(
                    items = selectedBoards,
                    key = { _, board -> "selected_${board.boardId}" }
                ) { index, board ->
                    DraggableBoardItem(
                        selectedBoards.size,
                        itemSize = gridSize,
                        board = board,
                        index = index,
                        onBoardDeselected = { onBoardDeselected(board.boardId) },
                        onPositionChange = { to -> onBoardOrderChanged(board.boardId, to) }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            // Available Boards Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Available Boards",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(
                items = allBoards,
                key = { board -> "available_${board.boardId}" }
            ) { board ->
                BoardItem(
                    board = board,
                    onClick = { onBoardSelected(board.boardId) },
                    enableBoarderHighlight = false
                )
            }
        }


        Button(
            onClick = onUpdateBoards,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1BB24B),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Update",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}


@Composable
private fun DraggableBoardItem(
    totalItems: Int,
    itemSize: Dp,
    board: BoardPref,
    index: Int,
    onBoardDeselected: () -> Unit,
    onPositionChange: (to: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val columns = ((screenWidth - 32.dp) / itemSize).toInt().coerceAtLeast(1)
    val rows = ceil(totalItems / columns.toFloat()).toInt()
    val cellSizePx = with(density) { ((screenWidth - 32.dp) / columns).toPx() }
    val cellHeightPx = with(density) { itemSize.toPx() }

    val colIndex = index % columns
    val rowIndex = index / columns

    val currentCellOffsetX = colIndex * cellSizePx
    val currentCellOffsetY = rowIndex * cellHeightPx


    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f) // Bring dragged item to the front
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(board) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        val col = ((offsetX + currentCellOffsetX) / cellSizePx).roundToInt()
                            .coerceIn(0, columns - 1)
                        val row = ((offsetY + currentCellOffsetY) / cellHeightPx).roundToInt()
                            .coerceIn(0, rows - 1)

                        val newPosition = row * columns + col
                        if (newPosition != index) onPositionChange(newPosition)
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        BoardItem(
            board = board,
            onClick = onBoardDeselected,
            modifier = Modifier
                .background(
                    color = if (isDragging) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
        )
    }
}


@Composable
private fun BoardItem(
    board: BoardPref,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enableBoarderHighlight: Boolean = false
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            1.dp,
            color = if (enableBoarderHighlight && board.isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        ),
    ) {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {

                    when (board.boardLabel) {
                        "services" -> {
                            Image(
                                painterResource(R.drawable.ic_board_services),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        "second_hands" -> {
                            Image(
                                painterResource(R.drawable.ic_board_seconds),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }

                        "local_jobs" ->{
                            Image(
                                painterResource(R.drawable.ic_board_local_job),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = board.boardName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }
    }
}
