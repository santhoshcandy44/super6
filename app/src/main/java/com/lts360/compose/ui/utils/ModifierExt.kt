package com.lts360.compose.ui.utils


import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.touchConsumer(
    pass: PointerEventPass,
    onDown: () -> Unit = {},
    onUp: () -> Unit = {}
) = this.then(
    Modifier.pointerInput(pass) {
        awaitEachGesture {
            val down = awaitFirstDown(pass = pass)
            down.consume()
            onDown()
            val up = waitForUpOrCancellation(pass)
            if (up != null) {
                onUp()
            }
        }
    }
)
