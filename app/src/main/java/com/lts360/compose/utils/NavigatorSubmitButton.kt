package com.lts360.compose.utils

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lts360.compose.dropUnlessResumedV2

@Composable
fun NavigatorSubmitButton(
    isLoading: Boolean,
    onNextButtonClicked: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    debounceInterval: Long = 500L,// Debounce interval in milliseconds
) {

    val lifecycleOwner = LocalLifecycleOwner.current


    // State to track the last click time (for debouncing)
    var lastClickTimeMillis by remember { mutableLongStateOf(0L) }


    Button(
        onClick = {
            val currentTimeMillis = System.currentTimeMillis()
            // Check both local clicked state and debounce interval
            if ((currentTimeMillis - lastClickTimeMillis) >= debounceInterval && !isLoading) {
                // Update the last click time
                lastClickTimeMillis = currentTimeMillis

                dropUnlessResumedV2(lifecycleOwner) {
                    onNextButtonClicked()
                }

            }
        },
        modifier = modifier,
        enabled = !isLoading, // Disable button if loading
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        content()
    }
}
