package com.super6.pot.app

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsLogger {

    // Log a custom message
    fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    // Record an exception with additional context
    fun recordException(exception: Throwable, customKeys: Map<String, String> = emptyMap()) {
        // Log the exception
        FirebaseCrashlytics.getInstance().recordException(exception)

        // Set custom keys (if any) for additional context
        for ((key, value) in customKeys) {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        }
    }

    // Set a custom key globally
    fun setCustomKey(key: String, value: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }
}