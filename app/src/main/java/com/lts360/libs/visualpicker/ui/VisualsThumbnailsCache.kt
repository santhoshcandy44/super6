package com.lts360.libs.visualpicker.ui

import android.graphics.Bitmap
import android.util.LruCache

object VisualsThumbnailsCache {

    private val memorySize = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()

    private val lruCache = object : LruCache<String, Bitmap>(memorySize) {

        override fun sizeOf(key: String?, value: Bitmap?): Int {
            return value?.let {
                it.byteCount / 1024
            } ?: 0
        }
    }

    fun getBitmap(key: String): Bitmap? {
        return lruCache.get(key)
    }

    fun putBitmap(key: String, bitmap: Bitmap?){
       lruCache.put(key, bitmap)
    }
}