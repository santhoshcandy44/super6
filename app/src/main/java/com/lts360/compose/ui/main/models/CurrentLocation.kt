package com.lts360.compose.ui.main.models


data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
    val geo: String,
    val locationType: String,
    val timestamp: Long = System.currentTimeMillis(),
)
