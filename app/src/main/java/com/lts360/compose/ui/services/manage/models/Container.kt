package com.lts360.compose.ui.services.manage.models

import android.net.Uri
import com.lts360.api.models.service.EditableImage
import com.lts360.compose.ui.services.BitmapContainer
import kotlinx.coroutines.Job


class ContainerFactory {
    private var idCounter = 0 // Instance variable to track unique IDs

    fun createContainer(image: EditableImage?, isLoading: Boolean = false, previewUri: Uri?=null): Container {
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



class CombinedContainerFactory {
    private var idCounter = 0 // Instance variable to track unique IDs

    fun createCombinedContainerForEditableImage(image: EditableImage?, isLoading: Boolean = false, previewUri: Uri?=null): CombinedContainer {
        return CombinedContainer(
           Container(
               image = image,
               isLoading = isLoading,
               previewUri = previewUri,
               containerId = generateId() // Generate a unique ID for each new container
           ),
            null,
            ContainerType.REMOTE

        )
    }


    fun createCombinedContainerForBitmap(path: String, width:Int, height:Int, format: String, errorMessage: String?): CombinedContainer {
        return CombinedContainer(
            null,
            BitmapContainer(
                path = path,
                width=width,
                height=height,
                format = format,
                containerId = generateId(),
                errorMessage = errorMessage
            ),
            ContainerType.BITMAP
        )
    }

    private fun generateId(): String {
        return "IMAGE_ID_${idCounter++}" // Generate a unique ID
    }
}

data class CombinedContainer(
    val container: Container?,
    val bitmapContainer: BitmapContainer?,
    val type: ContainerType
)


enum class ContainerType {
    REMOTE,
    BITMAP
}






