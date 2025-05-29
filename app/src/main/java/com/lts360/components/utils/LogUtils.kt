package com.lts360.components.utils

import android.util.Log
import com.lts360.components.utils.LogUtils.TAG


object LogUtils {
    const val TAG = "SUPER6_APP"
}

fun debugLogger(message:String, tag:String=TAG){
    Log.e(tag, message)
}

fun errorLogger(message:String, tag:String=TAG){
    Log.e(tag, message)
}