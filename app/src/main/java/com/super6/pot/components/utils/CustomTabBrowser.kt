package com.super6.pot.components.utils


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import com.super6.pot.R


fun openUrlInCustomTab(context: Context, url: String) {

    val toolbarColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)

    // Create color scheme parameters
    val colorSchemeParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(toolbarColor)
        .build()


    val customTabsIntent = CustomTabsIntent.Builder()
//        .setDefaultColorSchemeParams(colorSchemeParams)
        .setShowTitle(true)  // Show page title
        .setUrlBarHidingEnabled(true)
        .setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        .build()


    val packageName = getChromePackageName(context)

    if (packageName!=null) {
        // Unofficial way to disable download button
        customTabsIntent.intent.putExtra("org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_DOWNLOAD_BUTTON", true)

        // Unofficial way to disable star button
        customTabsIntent.intent.putExtra("org.chromium.chrome.browser.customtabs.EXTRA_DISABLE_STAR_BUTTON",true)

        // Set flags to avoid saving the page in history
        customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK
        customTabsIntent.intent.setPackage(packageName)
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } else {
        openUrlInBrowserFallback(context,url)

    }
}

private fun getChromePackageName(context: Context): String? {
    val packageManager = context.packageManager
    val customTabsPackages = packageManager.queryIntentServices(
        Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION), 0
    )

    for (info in customTabsPackages) {
        if (info.serviceInfo.packageName.equals("com.android.chrome", true)) {
            return info.serviceInfo.packageName
        }
    }
    return null
}

private fun openUrlInBrowserFallback(context: Context, url: String) {
    try {
        Toast.makeText(context, "Opening in a default browser", Toast.LENGTH_SHORT).show()

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    } catch (e: ActivityNotFoundException) {
        // If no app can handle the URL, show an error or fallback
        Toast.makeText(context, "No app available to open the URL", Toast.LENGTH_LONG).show()
        // Handle the error (e.g., show a message to the user)
        e.printStackTrace()
    }
}

