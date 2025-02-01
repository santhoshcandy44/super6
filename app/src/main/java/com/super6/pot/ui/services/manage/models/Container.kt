package com.super6.pot.ui.services.manage.models

import android.net.Uri
import com.super6.pot.api.models.service.EditableImage
import kotlinx.coroutines.Job


class ContainerFactory {
    private var idCounter = 0 // Instance variable to track unique IDs

    fun createContainer(image: EditableImage?, isLoading: Boolean = false,previewUri: Uri?=null): Container {
        return Container(
            image = image,
            isLoading = isLoading,
            previewUri = previewUri,
            containerId = generateId() // Generate a unique ID for each new container
        )
    }

    private fun generateId(): String {
        return "IMAGE_ID_${idCounter++}" // Generate a unique ID
    }
}


data class Container(
    val image: EditableImage?,
    val isLoading: Boolean,
    val error: String? = null,
    var ongoingRequest: Job? = null,
    var isRemoving: Boolean = false,
    val previewUri: Uri?=null,
    val containerId: String,
    val uniqueTimestamp: Long = System.currentTimeMillis(),
)