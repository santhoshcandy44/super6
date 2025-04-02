package com.lts360.compose.ui.main

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lts360.R
import com.lts360.app.database.models.app.Board
import com.lts360.compose.ui.main.prefs.BoardsSetupActivity
import kotlinx.coroutines.launch


@Composable
fun Boards(
    boards: List<Board>,
    pagerState: PagerState,
    servicesContent: @Composable () -> Unit,
    secondsContent: @Composable () -> Unit,
    onPageChanged: (Int) -> Unit
) {

    val context = LocalContext.current


    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val greenColor = Color(0xFF1BB24B)

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
        onPageChanged(selectedTabIndex)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        // LazyRow for horizontal scrolling of tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(8.dp)
        ) {

            itemsIndexed(boards) { tabIndex, item ->
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                ) {
                    /*    if(tabIndex==boards.size-1){
                            Image(Icons.Default.Settings, contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp))
                        }else{

                        }*/

                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
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
                                    text = item.boardName, textAlign = TextAlign.Center,
                                    color = if (tabIndex == selectedTabIndex) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }
            }


            item {
                Box(modifier = Modifier.wrapContentSize()) {
                    Image(
                        painterResource(R.drawable.ic_setup_boards),
                        contentDescription = null,
                        modifier = Modifier
                            .height(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                context.startActivity(
                                    Intent(
                                        context,
                                        BoardsSetupActivity::class.java
                                    )
                                )
                            },
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }

        }


        HorizontalPager(pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (boards[page].boardId) {
                1 -> {
                    servicesContent()
                }

                2 -> {
                    secondsContent()
                }
            }

        }


    }
}
