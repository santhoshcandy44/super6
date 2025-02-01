package com.super6.pot.ui.services.manage


import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.size.Size
import com.super6.pot.R
import com.super6.pot.ui.ShimmerBox
import com.super6.pot.ui.services.ThumbnailContainer
import com.super6.pot.ui.manage.models.Container
import com.super6.pot.ui.manage.services.viewmodels.PublishedServicesViewModel
import com.super6.pot.utils.LogUtils.TAG
import com.super6.pot.ui.chat.MAX_IMAGES
import com.super6.pot.ui.chat.createImagePartForSingleUri
import com.super6.pot.ui.chat.isValidImageDimensions
import kotlinx.coroutines.Job


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceImagesScreen(navHostController: NavHostController, onPopBackStack:()-> Unit, viewModel: PublishedServicesViewModel) {



    val containers = viewModel.editableContainers

    val userId = viewModel.userId


    val editableService = requireNotNull(viewModel.selectedService.collectAsState().value) {
        "Selected service should not be null"
    }


    // Collect state from the ViewModel
    val refreshImageIndex by viewModel.refreshImageIndex.collectAsState()
    val isPickerLaunch by viewModel.isPickerLaunch.collectAsState()


    val context = LocalContext.current

    // Create a launcher for picking multiple images
    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGES)
    ) { uris ->

            // Proceed if there are URIs to handle
            if (uris.isNotEmpty()) {

                uris.forEach { uri ->

                    if (containers.size < MAX_IMAGES) {

                        val newItem = viewModel.createLoadingItem(uri)

                        val result = isValidImageDimensions(context, uri)

                        val errorMessage = when{
                            !result.isValidDimension -> "Invalid Dimension"
                            !result.isValidFormat -> "Invalid Format"
                            else -> null
                        }


                        if (errorMessage==null) {


                            val imagePart = createImagePartForSingleUri(
                                context = context,
                                uri = uri,
                                isSingle = true
                            ) {}

                            editableService.let { nonNullableService ->
                                viewModel.onUploadImage(
                                    userId, nonNullableService.serviceId,
                                    newItem.image?.imageId ?: -1,
                                    imagePart,
                                    {
                                        it?.let { ongoingRequestJob ->
                                            viewModel.addLoadingItem(newItem.copy(ongoingRequest = ongoingRequestJob))
                                        }
                                    },
                                    {
                                        it.apply {
                                            viewModel.updateContainerImage(
                                                newItem.containerId,
                                                imageId = imageId,
                                                imageUrl = imageUrl,
                                                width = width,
                                                height = height,
                                                size = size,
                                                format = format
                                            )

                                            viewModel.updateOrAddImage(
                                                serviceId = editableService.serviceId,
                                                imageId = imageId,
                                                imageUrl = imageUrl,
                                                width = width,
                                                height = height,
                                                size = size,
                                                format = format
                                            )

                                        }
                                    },
                                    {
                                        viewModel.updateContainerState(newItem.containerId, it)
                                    })
                            }


                        }
                        else {

                            viewModel.addLoadingItem(newItem)
                            viewModel.onImageValidationFailedUpdate(newItem.containerId, errorMessage = errorMessage)
                        }
                    }else{
                        Toast.makeText(context, "Only 8 images are allowed", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        viewModel.togglePickerLaunch()


    }

    // Create a launcher for picking a single image
    val pickSingleImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia() // Or ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { visualUri ->

            val container = containers[refreshImageIndex]
            viewModel.setRefreshImageIndex(-1)

            val result = isValidImageDimensions(context, visualUri)

            val errorMessage = when{
                !result.isValidDimension -> "Invalid Dimension"
                !result.isValidFormat -> "Invalid Format"
                else -> null
            }

            if (errorMessage==null) {

                if(container.image==null){
                    val imagePart = createImagePartForSingleUri(
                        context = context,
                        uri = uri,
                        isSingle = true
                    ) {}

                    editableService.let { nonNullableService ->
                        viewModel.onUploadImage(
                            userId, nonNullableService.serviceId,
                            -1,
                            imagePart,
                            {
                                it?.let { ongoingRequestJob ->

                                    viewModel.updateImageContainerOngoingRequest(
                                        container.containerId,
                                        ongoingRequestJob,
                                        visualUri
                                    )
                                }
                            },
                            {
                                it.apply {
                                    viewModel.updateContainerImage(
                                        container.containerId,
                                        imageId = imageId,
                                        imageUrl = imageUrl,
                                        width = width,
                                        height = height,
                                        size = size,
                                        format = format
                                    )
                                    viewModel.updateOrAddImage(
                                        serviceId = editableService.serviceId,
                                        imageId = imageId,
                                        imageUrl = imageUrl,
                                        width = width,
                                        height = height,
                                        size = size,
                                        format = format
                                    )

                                }
                            },
                            {
                                viewModel.updateContainerState(container.containerId, it)
                            })
                    }
                }else{

                    val imagePart = createImagePartForSingleUri(
                        context = context,
                        uri = uri,
                        isSingle = true
                    ) {}

                    editableService.let { nonNullableService ->
                        viewModel.onUpdateImage(
                            userId, nonNullableService.serviceId,
                            containers[refreshImageIndex].image?.imageId ?: -1,
                            imagePart,
                            {
                                it?.let { ongoingRequestJob ->

                                    viewModel.updateImageContainerOngoingRequest(
                                        container.containerId,
                                        ongoingRequestJob,
                                        visualUri
                                    )

                                }
                            },
                            {
                                it.apply {
                                    viewModel.updateContainerImage(
                                        container.containerId,
                                        imageId = imageId,
                                        imageUrl = imageUrl,
                                        width = width,
                                        height = height,
                                        size = size,
                                        format = format)


                                    viewModel.updateOrAddImage(
                                        serviceId = editableService.serviceId,
                                        imageId = imageId,
                                        imageUrl = imageUrl,
                                        width = width,
                                        height = height,
                                        size = size,
                                        format = format)

                                }
                            },
                            {
                                viewModel.updateContainerState(container.containerId, it)
                            })
                    }

                }


            } else {

                viewModel.onImageValidationFailedUpdate(container.containerId,visualUri, errorMessage)
            }


        }
        viewModel.togglePickerLaunch()
    }



    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onPopBackStack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Manage Images",
                        style = MaterialTheme.typography.titleMedium
                    )
                })
        }
    ) { paddingValues ->
        Column(

            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),


            ) {

            ContainerList(
                containers,
                {
                    if (isPickerLaunch) return@ContainerList
                    viewModel.togglePickerLaunch()
                    viewModel.setRefreshImageIndex(it)
                    pickSingleImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                { containerId, ongoingReq ->
                    // Cancel the ongoing request if any
                    ongoingReq?.takeIf { !it.isCancelled }?.cancel()
                    viewModel.updateContainerIsRemoving(containerId)

                }, {
                    viewModel.updateAndRemoveContainer(it)
                },
                { containerId, imageId ->



                    viewModel.onDeleteImage(
                        userId,
                        editableService.serviceId,
                        imageId,
                        onSuccess = {
                            // Update service and repository on successful deletion
                            viewModel.updateAndRemoveContainer(containerId)
                            editableService.let {
                                viewModel.removeImageFromSelectedService(
                                    it.serviceId,
                                    imageId
                                )
                            }

                        },
                        onError = { errorMessage ->
                            // Handle failure scenario
                            Log.d(TAG, errorMessage.toString())
                            viewModel.handleFailureAndUpdateContainer(
                                containerId,
                                errorMessage
                            )

                        }
                    )


                }, {

                    if (isPickerLaunch)
                        return@ContainerList

                    viewModel.togglePickerLaunch()

                    pickImagesLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                }
            )

        }

    }
}

@Composable
fun ContainerList(
    containers: List<Container>,
    onReload: (Int) -> Unit,
    onRemoving: (String, Job?) -> Unit,
    onSuccess: (String) -> Unit,
    onDeleteImage: (String, Int) -> Unit,
    onUploadImages: () -> Unit,

    ) {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth(),
        columns = GridCells.Adaptive(140.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {


        item(span = { GridItemSpan(maxLineSpan) }) {  // let item span across all columns in Grid

            Text(
                text = "Upload Images",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (containers.isNotEmpty()) {
            itemsIndexed(
                containers,
                key = { _, item -> item.containerId }) { index, selectedImage ->

                val (image, isLoading, error, ongoingReq, isRemoving, previewUri) = selectedImage

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(140.dp) // Container size
                            .clip(RoundedCornerShape(4.dp))
                        // Clips to rounded corners
                    ) {



                        ServiceImage(imageUrl = previewUri?.toString() ?: image?.imageUrl)

                        // Reload button
                        IconButton(
                            onClick = {
                                onReload(index)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp) // Ensure button size
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(50)
                                )
                                .border(1.dp, Color.Gray, RoundedCornerShape(50))
                                .zIndex(1f) // Ensure button is above the image
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh, // Replace with your actual drawable
                                contentDescription = "Reload",
                                modifier = Modifier.padding(4.dp) // Size of the icon within the button
                            )
                        }

                        // Remove button


                        DeleteButton(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(24.dp)// Size of the icon within the button
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(50)
                                )
                                .border(1.dp, Color.Gray, RoundedCornerShape(50))
                                .zIndex(1f),

                            recentContainer = selectedImage,

                            { container ->
                                onRemoving(container.containerId, ongoingReq)
                            },
                            { container ->
                                onSuccess(container.containerId)
                            },
                            { container, imageId ->
                                onDeleteImage(container.containerId, imageId)
                            }
                        )

                        if (isRemoving) {
                            ShimmerBox(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                shimmerBackgroundContainerColor = Color.Red.copy(alpha = 0.4f), // Background color with alpha
                                shimmerColor = Color.Red.copy(alpha = 0.2f)
                            )
                        }

                        if (isLoading) {
                            ShimmerBox(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                shimmerBackgroundContainerColor = Color.Green.copy(alpha = 0.4f),
                                shimmerColor = Color.Green.copy(alpha = 0.2f)

                            )
                        }
                    }
                    error?.let {
                        Text(
                            text = it,
                            textAlign = TextAlign.Center,
                            color = Color.Red
                        )
                    }

                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(8.dp))

            UploadServiceImagesContainer {

                onUploadImages()
            }

        }
    }
}


@Composable
fun ServiceImage(
    imageUrl: String?,
    placeholder: Painter = painterResource(id = R.drawable.service_upload_images),
    error: Painter = painterResource(id = R.drawable.image_loading_failed),
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        if (imageUrl != null) {
            var hasError by rememberSaveable { mutableStateOf(false) }

            val imageRequest = remember(imageUrl) {

                ImageRequest.Builder(context)
                    .size(Size.ORIGINAL)
                    .data(imageUrl) // Use savedUrl.value here
                    .placeholder(R.drawable.service_upload_images)
                    .error(R.drawable.image_loading_failed) // Set error image directly
                    .crossfade(true)
                    .build()
            }

            if (hasError) {
                Image(
                    painter = error,
                    contentDescription = "No image available",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                )
            } else {
                // Image loading with effect
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentScale = ContentScale.Crop,
                    contentDescription = "Image",
                    loading = {
                        ShimmerBox(
                            modifier = Modifier.fillMaxSize(),
                            shimmerBackgroundContainerColor = Color.LightGray
                        )
                    },
                    onSuccess = {
                    },
                    onError = {
                        hasError = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                )
            }

        } else {
            Image(
                painter = placeholder,
                contentDescription = "No image available",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            )
        }
    }
}


@Composable
fun UploadServiceImagesContainer(
    uploadImagesError: String? = null,
    pickerLaunchClicked: () -> Unit) {

    Column {

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.service_upload_images),
                    contentDescription = "Upload Service Images",
                    modifier = Modifier.size(120.dp, 60.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "JPEG or PNG",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upload Service Images (Minimum 1 Required)",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "W: 150px - 4000px & H: 150px - 2416px",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    onClick = {
                        pickerLaunchClicked()
                    }
                ) {
                    Text(
                        "Browse Images",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }


            }
        }

        // Display error message if there's an error
        uploadImagesError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 4.dp
                ) // Adjust padding as needed
            )
        }
    }


}


@Composable
fun UploadServiceThumbnailContainer(
    pickerLaunchClicked: () -> Unit,
    thumbnailContainer: ThumbnailContainer?=null,
    path: String?=null,
    imageUrl:String?=null,
    isPath:Boolean=true,
) {

    Column(modifier = Modifier.fillMaxWidth()){

        if(path==null  && imageUrl==null){

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16/9f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.service_upload_images),
                        contentDescription = "Upload Thumbnail",
                        modifier = Modifier.size(120.dp, 60.dp)
                    )


                }

            }

        }else{

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f) // Set the aspect ratio to 16:9
            ) {


                if (imageUrl != null && !isPath) {
                    // If there is an image URL, display the image from the URL
                    Log.e(TAG, "Loading image from URL")
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (path != null && isPath) {
                    // If there is a bitmap in thumbnailContainer, display the bitmap
                    Log.e(TAG, "Displaying Bitmap")
                    AsyncImage(
                        path,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                append("JPEG or PNG.")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(" Recommended: (1280 x 720) by 16:9")
                }

            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides 0.dp,
        ) {
            Button(shape = RectangleShape,
                onClick = {
                    pickerLaunchClicked()
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp),

                ) {
                Text(
                    "Upload",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

        }



        // Display error message if there's an error
        thumbnailContainer?.errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 4.dp
                )
            )
        }
    }


}







@Composable
fun DeleteButton(
    modifier: Modifier = Modifier,
    recentContainer: Container,
    isRemovingCallBack: (Container) -> Unit,
    success: (Container) -> Unit,
    onDeleteImage: (Container, Int) -> Unit,
) {

    val image = recentContainer.image


    val enableClick = remember(recentContainer) {
        mutableStateOf(recentContainer.isRemoving)
    }


    // Handle the click event for the delete button
    IconButton(
        onClick = {

            if (!enableClick.value) {
                enableClick.value = true
                isRemovingCallBack(recentContainer)
                if (image != null) {
                    // Perform deletion and handle the result
                    onDeleteImage(recentContainer, image.imageId)

                } else {
                    // No image to delete, just call success callback
                    success(recentContainer)
                }
            }

        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Remove, // Replace with your actual drawable
            contentDescription = "Remove",
            modifier = Modifier.padding(4.dp) // Size of the icon within the button
        )
    }


}



