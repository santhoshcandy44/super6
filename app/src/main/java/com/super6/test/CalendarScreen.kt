package com.super6.test

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// Tamil month names
val tamilMonths = listOf(
    "சித்திரை", "வைகாசி", "ஆணி", "ஆடீ", "ஆவணி", "புரட்டாசி",
    "ஓகோ", "கும்பம்", "புரிபூ", "மாசி", "பங்குனி"
)


@Composable
fun CalendarScreen() {

    // Pager state
    val pagerState = rememberPagerState(pageCount = { tamilMonths.size })

    Column(modifier = Modifier.fillMaxSize()) {
        // Background image of the devotional god

        AsyncImage(
            "https://i.pinimg.com/236x/bd/1b/c4/bd1bc47e6d4ae2db1d56da511a5bebd7.jpg", // Make sure your image is in drawable folder
            contentDescription = "Devotional God",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)

        )


        // Calendar Pages using Pager
        HorizontalPager(
            pageSpacing = 8.dp,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) { page ->
            CalendarPage(page, month = tamilMonths[page], pagerState)
        }
    }
}


@Composable
fun CalendarPage(page: Int, month: String, pagerState: PagerState) {
    // Month details (could also add events or festivals)

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {


        AsyncImage("https://srirangaminfo.com/cal/2024/2401.jpg",
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true,
                ) {
                    scope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
                .graphicsLayer {
                    val pageOffSet = ((pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                            ).absoluteValue

                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                    )
                    /* scaleY =   lerp(
                         start = 0.75f,
                         stop = 1f,
                         fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                     )*/
                }
        )

    }
}
