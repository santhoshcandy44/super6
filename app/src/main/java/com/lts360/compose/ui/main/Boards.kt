package com.lts360.compose.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lts360.app.database.models.app.Board
import kotlinx.coroutines.launch


@Composable
fun Boards(
    boards:List<String>,
    pagerState:PagerState,
    servicesContent: @Composable () -> Unit,
    secondsContent: @Composable () -> Unit,
    onPageChanged: (Int) -> Unit
) {



    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val greenColor = Color(0xFF1BB24B)

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
        onPageChanged(selectedTabIndex)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // LazyRow for horizontal scrolling of tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)

        ) {
            itemsIndexed(boards) { tabIndex, item ->
                Box(
                    modifier = Modifier
                        .wrapContentSize()

                ) {
                    FilterChip(
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (tabIndex == selectedTabIndex) greenColor
                            else Color.Unspecified

                        ),
                        selected = tabIndex == selectedTabIndex,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(tabIndex)
                            }
                        },
                        label = {
                            Text(
                                text = item, textAlign = TextAlign.Center,
                                color = if (tabIndex == selectedTabIndex) Color.White
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

        }

        HorizontalPager(pagerState, modifier = Modifier.fillMaxSize()) { page ->

            when (page) {
                0 -> {
                    servicesContent()
                }

                1 -> {
                    secondsContent()
                }
            }

        }


    }
}
