package com.lts360.compose.ui.chat

import android.graphics.Bitmap
import androidx.activity.ComponentActivity

open class ChatUtilNativeBaseActivity : ComponentActivity() {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    external fun createThumbnail(bitmap: Bitmap): Bitmap
}
