package com.lts360.libs.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun ScaleView(
    modifier: Modifier = Modifier,
    items: List<Int>, // Default number of items
    onValueChanged: (Int) -> Unit = {}
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = items.size / 2)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val boxWidth = maxWidth
        val centerX = with(density) { boxWidth.toPx() } / 2  // Convert dp to px properly

        LazyRow(
            state = listState,

            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),

            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = boxWidth / 2),
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {

            itemsIndexed(items) { index, item ->
                if (index == 0) {
                    ScaleItem(item, minWidth = 0.dp, valueEnabled = false)
                } else {
                    ScaleItem(item, valueEnabled = false)
                }
            }
        }


        // Center Indicator
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .height(40.dp)
                .background(Color.Red)
        )


        LaunchedEffect(listState, constraints.maxWidth) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .collect { visibleItems ->
                    val startPadding = with(density) { (boxWidth / 2).toPx() }  // Convert to px

                    val closestItem = visibleItems.minByOrNull { item ->
                        abs((item.offset + startPadding) + (item.size / 2) - centerX).toInt()
                    }

                    closestItem?.let {
                        onValueChanged(items[it.index]) // Correctly detect the selected item
                    }
                }
        }


    }


}

@Composable
fun ScaleItem(
    value: Int,
    minWidth: Dp = 10.dp,
    lineAlignment: Alignment.Horizontal = Alignment.End,
    valueEnabled: Boolean = true
) {


    Column(
        modifier = Modifier
            .wrapContentWidth()
            .height(80.dp)
            .background(Color.Magenta)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Blue)
                .align(lineAlignment),
            contentAlignment = Alignment.BottomStart
        ) {
            Canvas(modifier = Modifier.wrapContentSize()) {
                val tickHeight = if (value % 10 == 0) 40f else 20f
                drawLine(
                    color = if (value % 10 == 0) Color.Yellow else Color.White,
                    start = Offset(size.width / 2, size.height),
                    end = Offset(size.width / 2, size.height - tickHeight),
                    strokeWidth = 4f
                )
            }
        }

        if (valueEnabled) {
            Text(
                text = if (value % 10 == 0) value.toString() else "",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }

    }

}