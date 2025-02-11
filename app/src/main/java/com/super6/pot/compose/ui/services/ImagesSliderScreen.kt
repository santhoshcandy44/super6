package com.super6.pot.compose.ui.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.super6.pot.api.models.service.Service
import com.super6.pot.compose.ui.services.bookmark.BookmarkedServicesViewModel
import com.super6.pot.compose.ui.viewmodels.ServicesViewModel
import kotlinx.coroutines.launch


@Composable
fun ImagesSliderScreen(
    key:Int,
    navHostController: NavHostController,
    selectedImagePosition: Int, viewModel: ServicesViewModel,
    onPopBackStack:()-> Unit

) {

    val selectedService by viewModel.selectedItem.collectAsState()

    ImagesSliderScreenContent(selectedService, selectedImagePosition, onPopBackStack)
}


@Composable
fun FeedUserImagesSliderScreen(
    navHostController: NavHostController,
    selectedImagePosition: Int,
    viewModel: ServiceOwnerProfileViewModel,
    onPopBackStack:()-> Unit

) {

    val selectedService by viewModel.selectedItem.collectAsState()

    ImagesSliderScreenContent(selectedService, selectedImagePosition, onPopBackStack)
}


@Composable
fun BookmarkedImagesSliderScreen(
    navHostController: NavHostController,
    selectedImagePosition: Int,
    viewModel: BookmarkedServicesViewModel,
    onPopBackStack:()-> Unit

) {



    val selectedService by viewModel.selectedItem.collectAsState()
    ImagesSliderScreenContent(selectedService, selectedImagePosition, onPopBackStack)

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesSliderScreenContent(selectedService: Service?, selectedImagePosition: Int, onPopBackStack:()-> Unit) {


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


@Composable
private fun ImageSlider(current: Int, images: List<String>) {


    val coroutineScope = rememberCoroutineScope()

    val context= LocalContext.current

    // Create a pager state with the initial page
    val pagerState = rememberPagerState(initialPage = current, pageCount = { images.size })

    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // HorizontalPager for displaying images
            HorizontalPager(
                state = pagerState,
                pageSpacing = 24.dp,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->

                // Load the image for the current page
                val imageRequest = ImageRequest.Builder(context)
                    .size(Size.ORIGINAL) // Load original size
                    .data(images[page]) // Use the URL from the image list
                    .build()

                AsyncImage(
                    imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth())
            }

        }


        Spacer(modifier = Modifier.height(16.dp)) // Space between pager and other content


        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Button (visible when not on the first page)
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            val currentPage = pagerState.currentPage
                            val previousPage = currentPage - 1
                            coroutineScope.launch { pagerState.animateScrollToPage(previousPage) }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }
                }

                // Next Button (visible when not on the last page)
                if (pagerState.currentPage < images.size - 1) {
                    IconButton(
                        onClick = {
                            val currentPage = pagerState.currentPage
                            val totalPages = images.size
                            val nextPage = if (currentPage < totalPages - 1) currentPage + 1 else 0
                            coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }


}
