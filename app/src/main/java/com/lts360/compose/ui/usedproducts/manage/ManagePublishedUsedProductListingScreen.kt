package com.lts360.compose.ui.usedproducts.manage

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.gson.Gson
import com.lts360.api.models.service.EditableLocation
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.chat.createImagePartForUri
import com.lts360.compose.ui.chat.getFileExtensionFromImageFormat
import com.lts360.compose.ui.chat.isValidImageDimensions
import com.lts360.compose.ui.main.ManagePublishedUsedProductListingLocationBottomSheetScreen
import com.lts360.compose.ui.profile.TakeProfilePictureSheet
import com.lts360.compose.ui.services.manage.DeleteServiceBottomSheet
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.compose.ui.services.manage.ExposedDropdownCountry
import com.lts360.compose.ui.services.manage.ReloadImageIconButton
import com.lts360.compose.ui.services.manage.RemoveImageIconButton
import com.lts360.compose.ui.services.manage.UploadServiceImagesContainer
import com.lts360.compose.ui.services.manage.models.ContainerType
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel
import com.lts360.libs.imagepicker.GalleryPagerActivityResultContracts
import com.lts360.libs.utils.createFileDCIMExternalStorage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePublishedUsedProductListingScreen(
    onPopBackStack: () -> Unit,
    viewModel: PublishedUsedProductsListingViewModel

) {


    val userId = viewModel.userId


    val serviceTitleError by viewModel.titleError.collectAsState()
    val shortDescriptionError by viewModel.shortDescriptionError.collectAsState()
    val selectedCountryError by viewModel.selectedCountryError.collectAsState()
    val selectedStateError by viewModel.selectedStateError.collectAsState()
    val priceError by viewModel.priceError.collectAsState()
    val priceUnitError by viewModel.priceUnitError.collectAsState()
    val imageContainersError by viewModel.imageContainersError.collectAsState()
    val selectedLocationError by viewModel.selectedLocationError.collectAsState()


    val serviceTitle by viewModel.title.collectAsState()
    val serviceShortDescription by viewModel.shortDescription.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedState by viewModel.selectedState.collectAsState()
    val price by viewModel.price.collectAsState()
    val priceUnit by viewModel.priceUnit.collectAsState()


    val selectedLocation by viewModel.selectedLocation.collectAsState()
    // Ensure containers reflect the latest state of imageContainers
    val imageContainers by viewModel.imageContainers.collectAsState()

    /*
        val isLoading by viewModel.isDraftLoading.collectAsState()
    */
    val isLoading = false

    val isPublishing by viewModel.isPublishing.collectAsState()


    val editableService by viewModel.selectedUsedProductListing.collectAsState()

    // Mutable state for isPickerLaunch and refreshImageIndex
    var isPickerLaunch by remember { mutableStateOf(false) }
    var refreshImageIndex by remember { mutableIntStateOf(-1) }

    val context = LocalContext.current


    // Create a launcher for picking multiple images
    val pickImagesLauncher = rememberLauncherForActivityResult(
        GalleryPagerActivityResultContracts.PickMultipleImages(MAX_IMAGES)
    ) { uris ->
        // Check if the number of selected images is less than 12
        if (imageContainers.size < MAX_IMAGES) {
            // Proceed if there are URIs to handle
            if (uris.isNotEmpty()) {

                uris.forEach { uri ->
                    val result = isValidImageDimensions(context, uri)
                    val errorMessage = if (result.isValidDimension) null else "Invalid Dimension"
                    viewModel.addContainer(
                        uri.toString(),
                        result.width,
                        result.height,
                        result.format.toString(),
                        errorMessage = errorMessage
                    )
                }

            }
        } else {
            // Show a toast message if the image limit is reached
            Toast.makeText(context, "Only $MAX_IMAGES images are allowed", Toast.LENGTH_SHORT)
                .show()
        }

        // Reset picker launch flag
        isPickerLaunch = false
    }


    // Create a launcher for picking multiple images
    val pickSingleImageLauncher = rememberLauncherForActivityResult(
        GalleryPagerActivityResultContracts.PickSingleImage()
    ) { uri ->

        if (refreshImageIndex != -1) {
            uri?.let {
                val result = isValidImageDimensions(context, uri)
                val errorMessage = if (result.isValidDimension) null else "Invalid Dimension"

                viewModel.updateContainer(
                    refreshImageIndex,
                    uri.toString(),
                    result.width,
                    result.height,
                    result.format.toString(),
                    errorMessage = errorMessage
                )
            }
        }
        // Reset picker launch flag
        isPickerLaunch = false

    }


    var pickerSheetState by rememberSaveable { mutableStateOf(false) }
    var cameraData: Uri? by rememberSaveable { mutableStateOf(null) }
    var requestedData: Uri? by rememberSaveable { mutableStateOf(null) }

    val cameraPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->

        if (refreshImageIndex != -1) {
            requestedData?.let {
                val result = isValidImageDimensions(context, it)
                val errorMessage = if (result.isValidDimension) null else "Invalid Dimension"

                viewModel.updateContainer(
                    refreshImageIndex,
                    it.toString(),
                    result.width,
                    result.height,
                    result.format.toString(),
                    errorMessage = errorMessage
                )
            }
        }else{
            // Check if the number of selected images is less than 12
            if (imageContainers.size < MAX_IMAGES) {
                if (isSuccess) {
                    requestedData?.let {
                        cameraData = it
                        val result = isValidImageDimensions(context, it)
                        val errorMessage = if (result.isValidDimension) null else "Invalid Dimension"
                        viewModel.addContainer(
                            requestedData.toString(),
                            result.width,
                            result.height,
                            result.format.toString(),
                            errorMessage = errorMessage
                        )
                        requestedData = null
                    }

                } else {
                    // Get the ContentResolver
                    requestedData?.let {
                        context.contentResolver.delete(it, null, null)
                    }
                    requestedData = null
                }

            } else {
                // Show a toast message if the image limit is reached
                Toast.makeText(context, "Only $MAX_IMAGES images are allowed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        // Reset picker launch flag
        isPickerLaunch = false
    }


    val coroutineScope = rememberCoroutineScope()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )


    BackHandler {

        if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.hide()
            }
        } else {
//        viewModel.inValidateSelectedService()
            onPopBackStack()
        }

    }


    val sheetState = rememberModalBottomSheetState()

    var bottomSheetState by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(bottomSheetState) {

        if (bottomSheetState) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }


    var expanded by remember { mutableStateOf(false) }
    val priceUnits = viewModel.priceUnits

    val selectedPriceUnit by viewModel.priceUnit.collectAsState()

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {

            if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {

                ManagePublishedUsedProductListingLocationBottomSheetScreen(
                    bottomSheetScaffoldState.bottomSheetState.currentValue, {

                        editableService?.let { nonNullableEditableService ->
                            viewModel.updateLocation(
                                EditableLocation(
                                    serviceId = nonNullableEditableService.productId,
                                    latitude = it.latitude,
                                    longitude = it.longitude,
                                    locationType = it.locationType,
                                    geo = it.geo
                                )
                            )

                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }

                    }, { recentLocation ->

                        editableService?.let { nonNullableEditableService ->

                            viewModel.updateLocation(
                                EditableLocation(
                                    serviceId = nonNullableEditableService.productId,
                                    latitude = recentLocation.latitude,
                                    longitude = recentLocation.longitude,
                                    locationType = recentLocation.locationType,
                                    geo = recentLocation.geo
                                )
                            )

                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }

                    }, { district, callback ->


                        editableService?.let { nonNullableEditableService ->

                            viewModel.updateLocation(
                                EditableLocation(
                                    serviceId = nonNullableEditableService.productId,
                                    latitude = district.coordinates.latitude,
                                    longitude = district.coordinates.longitude,
                                    locationType = district.district,
                                    geo = district.district
                                )
                            )

                        }

                        callback()

                    },
                    {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }
                    },
                    viewModel,
                    false

                )
            }


        },
        sheetDragHandle = null,
        sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
        sheetSwipeEnabled = false, // Allow gestures to hide/show bottom sheet
//            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { _ ->


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
                            text = "Manage Published Seconds",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) { contentPadding ->


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        strokeWidth = 4.dp
                    )
                } else {
                    // Main content
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxWidth(),
                        columns = GridCells.Adaptive(140.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        item(span = { GridItemSpan(maxLineSpan) }) {  // let item span across all columns in Grid
                            Text(
                                text = "Update Product",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }


                        item(span = { GridItemSpan(maxLineSpan) }) {

                            Column(modifier = Modifier.fillMaxWidth()) {

                                // Text Field for UsedProductListing Title
                                OutlinedTextField(
                                    value = serviceTitle,
                                    onValueChange = { viewModel.updateTitle(it) },
                                    label = { Text("Name") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )

                                if (serviceTitle.length > 100) {
                                    Text(
                                        text = "Limit: ${serviceTitle.length}/${100}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        ) // Adjust padding as needed
                                    )
                                }

                                // Display error message if there's an error
                                serviceTitleError?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Text Field for Short Description
                                OutlinedTextField(
                                    value = serviceShortDescription,
                                    onValueChange = { viewModel.updateShortDescription(it) },
                                    label = { Text("Description") },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    minLines = 4
                                )


                                if (serviceShortDescription.length > 250) {
                                    Text(
                                        text = "Limit: ${serviceShortDescription.length}/${250}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        ) // Adjust padding as needed
                                    )
                                }

                                shortDescriptionError?.let {
                                    ErrorText(it)
                                }


                                Spacer(modifier = Modifier.height(8.dp))


                                OutlinedTextField(
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    value = price,
                                    onValueChange = {
                                        viewModel.updatePrice(it)
                                    },
                                    label = { Text("Price") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 1,
                                    isError = priceError != null
                                )

                                priceError?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }

                            }

                        }



                        item(span = { GridItemSpan(maxLineSpan) }) {

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                OutlinedTextField(
                                    value = selectedPriceUnit,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Currency") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(
                                            ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                        )
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    priceUnits.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(text = unit) },
                                            onClick = {
                                                viewModel.updatePriceUnit(unit)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Display error message if there's an error
                            priceUnitError?.let {
                                ErrorText(it)
                            }

                        }


                        if (imageContainers.isNotEmpty()) {
                            itemsIndexed(imageContainers) { index, bitmapContainer ->

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp) // Container size
                                            .clip(RoundedCornerShape(4.dp)) // Clips to rounded corners
                                    ) {

                                        AsyncImage(
                                            if (bitmapContainer.type == ContainerType.BITMAP) {
                                                bitmapContainer.bitmapContainer?.path

                                            } else {
                                                bitmapContainer.container?.image?.imageUrl

                                            },
                                            contentDescription = "UsedProductListing image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp)
                                                .border(
                                                    1.dp,
                                                    Color.Gray,
                                                    RoundedCornerShape(4.dp)
                                                )
                                        )

                                        ReloadImageIconButton {
                                            refreshImageIndex = index
                                            pickerSheetState = true
                                        }

                                        RemoveImageIconButton {
                                            viewModel.removeContainer(index)
                                        }

                                    }

                                    bitmapContainer.bitmapContainer?.errorMessage?.let {
                                        Text(
                                            text = it,
                                            textAlign = TextAlign.Center,
                                            color = Color.Red
                                        )
                                    }


                                }

                            }
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {  // let item span across all columns in Grid

                            // Upload Images Section
                            Spacer(modifier = Modifier.height(8.dp))

                            UploadServiceImagesContainer(imageContainersError) {
                                pickerSheetState = true
                            }

                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {

                            ExposedDropdownCountry(
                                selectedCountry,
                                selectedState,
                                selectedCountryError,
                                selectedStateError,
                                {
                                    viewModel.updateCountry(it.value)
                                }
                            ) {
                                viewModel.updateState(it.name)
                            }
                        }



                        item(span = { GridItemSpan(maxLineSpan) }) {

                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Location TextField
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedLocation?.geo ?: "",
                                    onValueChange = { /* Handle location change */ },
                                    label = { Text("Location") },
                                    trailingIcon = {

                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                                }
                                            }
                                        ) {

                                            Icon(
                                                Icons.AutoMirrored.Filled.RotateLeft,
                                                contentDescription = null
                                            )

                                        }

                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)

                                )

                                selectedLocationError?.let {
                                    ErrorText(it)
                                }
                            }

                        }


                        item(span = { GridItemSpan(maxLineSpan) }) {
                            // Upload Images Section
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Action Buttons
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF57D457
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = {

                                        editableService?.let { nonNullSelectedUsedProductListing ->
                                            if (viewModel.validateAll()) {


                                                val bodyProductId =
                                                    nonNullSelectedUsedProductListing.productId.toString()
                                                        .toRequestBody("text/plain".toMediaTypeOrNull())

                                                val bodyTitle =
                                                    serviceTitle.toRequestBody("text/plain".toMediaTypeOrNull())
                                                val bodyShortDescription =
                                                    serviceShortDescription.toRequestBody("text/plain".toMediaTypeOrNull())


                                                val bodyPriceUnit =
                                                    priceUnit.toRequestBody("text/plain".toMediaTypeOrNull())

                                                val bodyCountry = selectedCountry.toString()
                                                    .toRequestBody("text/plain".toMediaTypeOrNull())

                                                val bodyState = selectedState.toString()
                                                    .toRequestBody("text/plain".toMediaTypeOrNull())


                                                val keepImages =
                                                    imageContainers.mapNotNull { it.container }
                                                        .mapNotNull { it.image }
                                                        .map { it.imageId }

                                                val bodyPrice = price.toDouble().let {
                                                    Gson().toJson(it)  // Convert the float to a JSON representation
                                                        .toRequestBody("text/plain".toMediaType())  // Convert the JSON string to RequestBody
                                                }


                                                val bodyKeepImageIds = keepImages.toString()
                                                    .toRequestBody("application/json".toMediaTypeOrNull())


                                                val bodyLocation = selectedLocation?.let {
                                                    Gson().toJson(it)
                                                        .toRequestBody("application/json".toMediaType())
                                                }


                                                val bodyImages =
                                                    imageContainers.mapNotNull { it.bitmapContainer }
                                                        .mapIndexed { index, bitmapContainer ->
                                                            createImagePartForUri(
                                                                context,
                                                                Uri.parse(bitmapContainer.path),
                                                                "IMAGE_${index}_${
                                                                    getFileExtensionFromImageFormat(
                                                                        bitmapContainer.format
                                                                    )
                                                                }",
                                                                bitmapContainer.format,
                                                                "images[]"
                                                            )
                                                        }.filterNotNull()


                                                viewModel.onUpdateUsedProductListing(
                                                    bodyProductId,
                                                    bodyTitle,
                                                    bodyShortDescription,
                                                    bodyPrice,
                                                    bodyPriceUnit,
                                                    bodyState,
                                                    bodyCountry,
                                                    bodyImages,
                                                    bodyLocation,
                                                    bodyKeepImageIds,
                                                    {
                                                        Toast.makeText(
                                                            context,
                                                            it,
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                    }) {

                                                    Toast.makeText(
                                                        context,
                                                        it,
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }

                                            }

                                        }

                                    },
                                    modifier = Modifier.fillMaxWidth()

                                ) {
                                    Text("Update")
                                }

                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    onClick = {
                                        bottomSheetState = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Delete")
                                }
                            }
                        }

                    }

                }


            }

        }
    }


    if (bottomSheetState) {

        ModalBottomSheet(
            modifier = Modifier
                .padding(16.dp)
                .safeDrawingPadding(),
            dragHandle = null,
            onDismissRequest = {
                bottomSheetState = false
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(16.dp)
        ) {


            DeleteServiceBottomSheet("Are you sure you want to delete this product? This action cannot be undone.",
                {

                    editableService?.let { editableServiceNonNull ->
                        viewModel.onDeleteService(userId, editableService!!.productId, {
                            viewModel.removeSelectedService(editableServiceNonNull.productId)
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            onPopBackStack()

                        }) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    bottomSheetState = false

                }) {
                bottomSheetState = false
            }

        }
    }



    TakeProfilePictureSheet(pickerSheetState, onGallerySelected = {
        if (isPickerLaunch)
            return@TakeProfilePictureSheet
        isPickerLaunch = true
        if(refreshImageIndex!=-1){
            pickSingleImageLauncher.launch(Unit)
        }else{
            pickImagesLauncher.launch(Unit)
        }
        pickerSheetState = false
    }, onCameraSelected = {
        createFileDCIMExternalStorage(
            context = context,
            "Lts360/Seconds"
        )?.let {
            requestedData = it
            cameraPickerLauncher.launch(
                it
            )
        }
        pickerSheetState = false
    }) {
        pickerSheetState = false
    }


    if (isPublishing) {
        LoadingDialog()
    }


}