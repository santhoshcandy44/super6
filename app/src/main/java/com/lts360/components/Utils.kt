package com.lts360.components

import android.app.ActivityManager
import android.content.Context
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.main.MainActivity


fun isMainActivityInStack(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = activityManager.appTasks  // Gets a list of tasks

    for (task in tasks) {
        // This could be further refined depending on how the task and stackId map to your activity.
        if (task.taskInfo.baseActivity?.className == MainActivity::class.java.name) {

            return true  // MainActivity is found in the stack
        }
    }

    return false  // MainActivity is not in the stack
}


fun isAuthActivityInStack(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = activityManager.appTasks  // Gets a list of tasks

    for (task in tasks) {
        // This could be further refined depending on how the task and stackId map to your activity.
        if (task.taskInfo.baseActivity?.className == AuthActivity::class.java.name) {

            return true  // MainActivity is found in the stack
        }
    }

    return false  // MainActivity is not in the stack
}

