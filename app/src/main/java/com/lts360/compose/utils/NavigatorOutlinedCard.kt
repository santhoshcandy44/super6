package com.lts360.compose.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lts360.compose.dropUnlessResumedV2


@Composable
fun NavigatorOutlinedCard(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    debounceInterval: Long = 500L,// Debounce interval in milliseconds
    shape: Shape = RectangleShape,
    onCardClicked: () -> Unit,
    content: @Composable () -> Unit,
) {


    // State to track the last click time (for debouncing)
    var lastClickTimeMillis by remember { mutableLongStateOf(0L) }

    val lifecycleOwner = LocalLifecycleOwner.current


    OutlinedCard(
        shape = shape,
        onClick = {
            val currentTimeMillis = System.currentTimeMillis()
            // Check both local clicked state and debounce interval
            if ((currentTimeMillis - lastClickTimeMillis) >= debounceInterval && !isLoading) {
                // Update the last click time
                lastClickTimeMillis = currentTimeMillis
                // Execute the action only if the button is not in loading state
                dropUnlessResumedV2(lifecycleOwner) {
                    onCardClicked()
                }

            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }

}