package com.super6.pot.api.auth.managers

interface CriticalListener {
    fun onSuccess(newToken:String)
    fun onFailed(responseCode:Int)
    fun onError(e: Exception? = null)
}