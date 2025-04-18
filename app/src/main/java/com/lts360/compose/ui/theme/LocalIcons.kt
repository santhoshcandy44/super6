package com.lts360.compose.ui.theme

import com.lts360.R

data class LocalIcons(
    val interest: Int,
    val bookmark: Int,
    val bookmarkedRed:Int,
    val manageSeconds: Int,
    val manageServices: Int,
    val settings: Int,
    val invite: Int,
    val help: Int,
    val exit: Int,
    val add:Int,
    val privacyPolicy:Int,
    val signNow:Int,
    val accountTypeBusiness:Int,
    val accountTypePersonal: Int,
    val location:Int,
    val logInUser:Int,
    val logInPassword:Int,
    val locationAccessOnBoarding:Int,
    val deleteNotification:Int,
    val notificationMarkAsRead:Int,
    val wall:Int
)

val LightIcons = LocalIcons(
    interest = R.drawable.ic_light_interest,
    bookmark = R.drawable.ic_light_bookmark,
    bookmarkedRed = R.drawable.ic_bookmarked,
    settings = R.drawable.ic_light_settings,
    manageServices = R.drawable.ic_light_manage_services,
    manageSeconds = R.drawable.ic_light_manage_seconds,
    invite = R.drawable.ic_light_invite_friends,
    help = R.drawable.ic_light_help,
    exit = R.drawable.ic_light_exit,
    add = R.drawable.ic_light_add,
    privacyPolicy= R.drawable.ic_light_privacy_policy,
    signNow = R.drawable.ic_light_sign_now,
    accountTypeBusiness = R.drawable.ic_light_account_type_business,
    accountTypePersonal = R.drawable.ic_light_account_type_personal,
    location = R.drawable.ic_light_location,
    logInUser = R.drawable.ic_light_login_user,
    logInPassword = R.drawable.ic_light_login_password,
    locationAccessOnBoarding = R.drawable.ic_light_location_access_on_boarding,
    deleteNotification = R.drawable.ic_light_notification_delete,
    notificationMarkAsRead = R.drawable.ic_light_notification_mark_as_read,
    wall = R.drawable.light_app_wall

)

val DarkIcons = LocalIcons(
    interest = R.drawable.ic_dark_interest,
    bookmark = R.drawable.ic_dark_bookmark,
    bookmarkedRed = R.drawable.ic_bookmarked,
    settings = R.drawable.ic_dark_settings,
    manageServices = R.drawable.ic_dark_manage_services,
    manageSeconds = R.drawable.ic_dark_manage_seconds,
    invite = R.drawable.ic_light_invite_friends,
    help = R.drawable.ic_dark_help,
    exit = R.drawable.ic_dark_exit,
    add = R.drawable.ic_dark_add,
    privacyPolicy= R.drawable.ic_dark_privacy_policy,
    signNow = R.drawable.ic_dark_sign_now,
    accountTypeBusiness = R.drawable.ic_dark_account_type_business,
    accountTypePersonal = R.drawable.ic_dark_account_type_personal,
    location = R.drawable.ic_dark_location,
    logInUser = R.drawable.ic_dark_login_user,
    logInPassword = R.drawable.ic_dark_login_password,
    locationAccessOnBoarding = R.drawable.ic_dark_location_access_on_boarding,
    deleteNotification = R.drawable.ic_dark_notification_delete,
    notificationMarkAsRead = R.drawable.ic_dark_notification_mark_as_read,
    wall = R.drawable.dark_app_wall

)



