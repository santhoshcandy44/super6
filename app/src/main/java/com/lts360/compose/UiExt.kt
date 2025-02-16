package com.lts360.compose

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

// Global function to execute actions based on lifecycle state
fun dropUnlessResumedV2(lifecycleOwner: LifecycleOwner, block: () -> Unit) {
    // Execute the action only if the lifecycle is RESUMED
    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        block()
    }
}
