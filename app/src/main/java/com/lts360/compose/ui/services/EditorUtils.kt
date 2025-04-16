package com.lts360.compose.ui.services

import com.lts360.api.models.service.EditablePlan

class BitmapContainerFactory {
    private var idCounter = 0

    fun createContainer(path: String, width:Int, height:Int, format: String, errorMessage: String?): BitmapContainer {
        return BitmapContainer(
            path = path,
            width=width,
            height=height,
            format = format,
            containerId = generateId(),

            errorMessage = errorMessage
        )
    }

    private fun generateId(): String {
        return "CONTAINER_ID_${idCounter++}"
    }
}

data class ThumbnailContainer(
    val path: String,
    val width:Int,
    val height:Int,
    val format: String,
    val containerId: String,
    val errorMessage: String? = null
)

data class BitmapContainer(
    val path: String,
    val width:Int,
    val height:Int,
    val format: String,
    val containerId: String,
    val errorMessage: String? = null
)


data class ValidatedPlan(
    val isValid: Boolean,
    val editablePlan: EditablePlan,
)





