package com.super6.pot.api.auth.managers

interface RetryListener {
    fun onRetry(newToken:String)
}
