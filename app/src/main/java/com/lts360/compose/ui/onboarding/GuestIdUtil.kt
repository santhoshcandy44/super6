package com.lts360.compose.ui.onboarding

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.lts360.components.utils.LogUtils.TAG
import java.util.UUID
import kotlin.math.abs


object GuestIdUtil {

    // Method to create a guest ID based on Android ID
    fun generateGuestId(context: Context): Long? {
        // Get the Android ID (unique to each device)
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        // Check if the Android ID is not null or empty
        if (androidId.isNullOrEmpty()) {
            Log.e(TAG, "Android ID is null or empty")
            return null
        }

        // Generate UUID from Android ID
        val uuid = UUID.nameUUIDFromBytes("super6_${androidId}".toByteArray())

        // Access the most significant and least significant bits
        val mostSignificantBits = uuid.mostSignificantBits
        val leastSignificantBits = uuid.leastSignificantBits

        // Combine the most and least significant bits using XOR
        val combinedBits = ((mostSignificantBits xor leastSignificantBits) and 0xFFFFFFFFL)

        // Return the combined value as an absolute integer
        return abs(combinedBits)
    }
}