package com.super6.pot.ui.services

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.super6.pot.api.models.service.EditablePlan
import com.super6.pot.app.database.daos.service.DraftImageDao
import com.super6.pot.app.database.daos.service.DraftLocationDao
import com.super6.pot.app.database.daos.service.DraftPlanDao
import com.super6.pot.app.database.daos.service.DraftServiceDao
import com.super6.pot.app.database.daos.service.DraftThumbnailDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class BitmapContainerFactory {
    private var idCounter = 0 // Instance variable to track unique IDs

    fun createContainer(path: String, width:Int, height:Int, format: String, errorMessage: String?): BitmapContainer {
        return BitmapContainer(
            path = path,
            width=width,
            height=height,
            format = format,
            containerId = generateId(), // Generate a unique ID for each new container
            errorMessage = errorMessage
        )
    }

    private fun generateId(): String {
        return "CONTAINER_ID_${idCounter++}" // Generate a unique ID
    }
}

data class ThumbnailContainer(
    val path: String,
    val width:Int,
    val height:Int,
    val format: String,
    val containerId: String,
    val errorMessage: String? = null,
)


data class BitmapContainer(
    val path: String,
    val width:Int,
    val height:Int,
    val format: String,
    val containerId: String,
    val errorMessage: String? = null,
)


data class ValidatedPlan(
    val isValid: Boolean,
    val editablePlan: EditablePlan,
)



@HiltViewModel
class CreateServiceViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val draftServiceDao: DraftServiceDao,
    private val draftLocationDao: DraftLocationDao,
    private val draftPlansDao: DraftPlanDao,
    private val draftImagesDao: DraftImageDao,
    private val draftThumbnailDao: DraftThumbnailDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


}


