package com.lts360.libs.imagepicker.models

import android.net.Uri

data class ImageMediaData(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val type: String,
    val dateAdded: Long,
    val width: Int,
    val height: Int,
    val path: String,
    val duration:Long = -1L,
    val isSelected:Boolean=false)
