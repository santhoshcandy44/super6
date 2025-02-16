package com.lts360.api.auth.managers

interface RetryListener {
    fun onRetry(newToken:String)
}
