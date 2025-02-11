package com.super6.test


import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.compose.ui.auth.WelcomeScreen
import com.super6.pot.compose.ui.chat.viewmodels.ChatListViewModel
import com.super6.pot.compose.ui.chat.viewmodels.ChatViewModel
import com.super6.pot.compose.ui.main.HomeScreen
import com.super6.pot.compose.ui.main.MainActivity
import com.super6.pot.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.super6.pot.compose.ui.services.ImagesSliderScreen
import com.super6.pot.compose.ui.theme.AppTheme
import com.super6.pot.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TestActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }

    }




    override fun onCreate(savedInstanceState: Bundle?) {




        val color = Color.Blue

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                color.toArgb(),
                darkScrim = color.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.auto(
                color.toArgb(),
                darkScrim = color.toArgb()
            )
        )

        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {
                    }
                }

            }
        }
    }
}


@Composable
fun Boards() {

    val boards = listOf("Second Hands", "Siz Market", "Jobs")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(pageCount = { boards.size })

    val greenColor = Color(0xFF1BB24B)

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }


    Column(
        modifier = Modifier.fillMaxSize(),
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
                        colors =

                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (tabIndex == selectedTabIndex) greenColor
                            else Color.Unspecified

                        ),
                        selected = tabIndex == selectedTabIndex,
                        onClick = { selectedTabIndex = tabIndex },
                        label = {
                            Text(
                                text = item, textAlign = TextAlign.Center,
                                color = if (tabIndex == selectedTabIndex) Color.White
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },

                    )
                }
            }

        }

        HorizontalPager(pagerState, modifier = Modifier.fillMaxSize()) { page ->

            when (page) {
                0 -> {
                    LazyVerticalGrid(
                        GridCells.Adaptive(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {

                        items(30) {
                            OutlinedCard {
                                Column(
                                    modifier = Modifier
                                        .wrapContentSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .aspectRatio(1f)
                                    ) {
                                        AsyncImage(
                                            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSwEE2zLFoC38W_x52g1bExZZw-lL0Jw9Wfvw&s",
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // 🔹 Favorite Icon Background Box
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd) // 🔹 Positions it at the top-right
                                                .padding(8.dp) // 🔹 Adds spacing from edges
                                                .size(32.dp) // 🔹 Fixed size
                                                .background(Color.LightGray.copy(alpha = 0.4f), shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // 🔹 Favorite Icon
                                            Image(
                                                imageVector = Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(Color.White),
                                                modifier = Modifier.size(24.dp) // 🔹 Set proper size (without extra padding)
                                            )
                                        }

                                    }

                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Old Phone mobile")

                                        Text(
                                            "Salem, india",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        // 🔹 Aligns the price to the END
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(
                                                "400$",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = greenColor
                                            )
                                        }

                                    }

                                }
                            }


                        }

                    }
                }

                1 -> Text("Wow")
            }

        }

    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {


    AppTheme {
        Surface {

        }
    }
}

