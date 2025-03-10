package com.lts360.compose.ui.news.qr.models

import kotlinx.serialization.Serializable

@Serializable
data class TextData(val text:String)


// Data class for URL QR Code
@Serializable
data class UrlData(val url: String)

// Data class for Event QR Code
@Serializable
data class EventData(
    val title: String,
    val location: String,
    val startDate: String,
    val endDate: String
)

// Data class for Contact QR Code (vCard format)
@Serializable
data class ContactData(
    val name: String,
    val phone: String,
    val email: String,
    val company: String
)
