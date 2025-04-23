package com.lts360.compose.ui.localjobs.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.api.models.service.UsedProductListing
import com.lts360.compose.ui.bookmarks.BookmarksViewModel
import com.lts360.compose.ui.localjobs.LocalJobsViewmodel
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.ImageSlider
import com.lts360.compose.ui.usedproducts.SecondsOwnerProfileViewModel


@Composable
fun LocalJobsImagesSliderScreen(
    selectedImagePosition: Int, viewModel: LocalJobsViewmodel,
    onPopBackStack:()-> Unit

) {
    val selectedService by viewModel.selectedItem.collectAsState()

    LocalJobsImagesSliderScreenContent(selectedService, selectedImagePosition, onPopBackStack)
}




@Composable
fun BookmarkedLocalJobsImagesSliderScreen(
    selectedImagePosition: Int,
    viewModel: BookmarksViewModel,
    onPopBackStack:()-> Unit

) {
    val selectedService by viewModel.selectedItem.collectAsState()
    val item = selectedService

    if(item !is LocalJob) return

    LocalJobsImagesSliderScreenContent(item, selectedImagePosition, onPopBackStack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalJobsImagesSliderScreenContent(selectedService: LocalJob?, selectedImagePosition: Int,
                                             onPopBackStack:()-> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(2.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = dropUnlessResumed {
                            onPopBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Viewer",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    scrolledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(Color.Black)
                    .fillMaxSize()) {

                ImageSlider(selectedImagePosition, selectedService?.images?.map {
                    it.imageUrl
                } ?: emptyList())

            }
        })
}


