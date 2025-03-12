package com.lts360.components

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Serializable> Intent.getSerializableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
        key,
        T::class.java
    )
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}


inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        getParcelableExtra(key, T::class.java)
    }
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        // Use the newer method for API 33 and above
        getParcelableArrayListExtra(key, T::class.java)
    }
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra<T>(key)
}

