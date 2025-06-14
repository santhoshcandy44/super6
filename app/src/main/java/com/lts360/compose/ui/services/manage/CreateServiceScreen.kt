package com.lts360.compose.ui.services.manage

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.google.gson.Gson
import com.lts360.R
import com.lts360.app.database.models.service.DraftCountry
import com.lts360.app.database.models.service.DraftIndustry
import com.lts360.app.database.models.service.DraftLocation
import com.lts360.app.database.models.service.DraftService
import com.lts360.app.database.models.service.DraftState
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.chat.createImagePartForUri
import com.lts360.compose.ui.chat.getFileExtensionFromImageFormat
import com.lts360.compose.ui.chat.isValidImageDimensions
import com.lts360.compose.ui.chat.isValidThumbnailDimensionsFormat
import com.lts360.compose.ui.main.CreateServiceLocationBottomSheetScreen
import com.lts360.compose.ui.services.ValidatedPlan
import com.lts360.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel
import com.lts360.libs.imagepicker.GalleryPagerActivityResultContracts
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceScreen(
    onServiceCreated: () -> Unit,
    onServiceDeleted: () -> Unit,
    onBack: () -> Unit,
    viewModel: ServicesWorkflowViewModel

) {

    val status by viewModel.status.collectAsState()
    val draftId by viewModel.draftId.collectAsState()


    val thumbnailContainer by viewModel.thumbnailContainer.collectAsState()

    val serviceTitleError by viewModel.titleError.collectAsState()
    val shortDescriptionError by viewModel.shortDescriptionError.collectAsState()
    val longDescriptionError by viewModel.longDescriptionError.collectAsState()
    val selectedIndustryError by viewModel.industryError.collectAsState()
    val selectedCountryError by viewModel.selectedCountryError.collectAsState()
    val selectedStateError by viewModel.selectedStateError.collectAsState()

    val imageContainersError by viewModel.imageContainersError.collectAsState()
    val selectedLocationError by viewModel.selectedLocationError.collectAsState()
    val plansError by viewModel.plansError.collectAsState()


    val serviceTitle by viewModel.title.collectAsState()
    val serviceShortDescription by viewModel.shortDescription.collectAsState()
    val serviceLongDescription by viewModel.longDescription.collectAsState()
    val selectedIndustry by viewModel.selectedIndustry.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedState by viewModel.selectedState.collectAsState()

    val selectedLocation by viewModel.selectedLocation.collectAsState()
    // Ensure containers reflect the latest state of imageContainers
    val imageContainers by viewModel.imageContainers.collectAsState()
    val plans by viewModel.plans.collectAsState()

    /*
        val isLoading by viewModel.isDraftLoading.collectAsState()
    */
    val isLoading = false

    val isPublishing by viewModel.isPublishing.collectAsState()

    // Observe the state from ViewModel
    val deleteDraftDialogVisibility by viewModel.deleteDraftDialogVisibility.collectAsState()

    val isPickerLaunch by viewModel.isPickerLaunch.collectAsState()
    val refreshImageIndex by viewModel.refreshImageIndex.collectAsState()
    val slots by viewModel.slots.collectAsState()

    val context = LocalContext.current


    // Create a launcher for picking multiple images
    val pickImagesLauncher = if(slots>0){
        rememberLauncherForActivityResult(
            GalleryPagerActivityResultContracts.PickMultipleImages(slots, allowSingleItemChoose = true)
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

            viewModel.setPickerLaunch(false)

        }


    }else{
        null
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

        viewModel.setPickerLaunch(false)

    }


    val pickThumbnailImageLauncher = rememberLauncherForActivityResult(
        GalleryPagerActivityResultContracts.PickSingleImage()
    ) { uri ->
        uri?.let {
            // Proceed if there are URIs to handle
            val result = isValidThumbnailDimensionsFormat(context, uri)

            val errorMessage = when {
                !result.isValidDimension -> "Invalid Dimension"
                !result.isValidFormat -> "Invalid Format"
                else -> null
            }
            viewModel.updateThumbnailContainer(
                uri.toString(),
                result.width,
                result.height,
                result.format.toString(),
                errorMessage = errorMessage
            )

        }

        viewModel.setPickerLaunch(false)

    }


    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.hide()
            }
        } else {
            onBack()
        }
    }

    BottomSheetScaffold(
        sheetDragHandle = null,
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {

            if(bottomSheetScaffoldState.bottomSheetState.currentValue==SheetValue.Expanded){

                CreateServiceLocationBottomSheetScreen(
                    bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden,
                    {
                        viewModel.updateLocation(
                            DraftLocation(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                locationType = it.locationType,
                                geo = it.geo
                            )
                        )

                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }
                    },
                    {
                        viewModel.updateLocation(
                            DraftLocation(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                locationType = it.locationType,
                                geo = it.geo
                            )
                        )

                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }

                    },
                    { district ->

                        viewModel.updateLocation(
                            DraftLocation(
                                latitude = district.coordinates.latitude,
                                longitude = district.coordinates.longitude,
                                locationType = district.district,
                                geo = district.district
                            )
                        )

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
        sheetShape = RectangleShape,
        sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
        sheetSwipeEnabled = false, // Allow gestures to hide/show bottom sheet
//            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { _ ->


        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon"
                            )
                        }

                    },
                    title = {
                        Text(
                            text = "Create Service",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                )
            },
            modifier = Modifier
                .fillMaxSize()
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
                                text = "Add New Service",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {

                            UploadServiceThumbnailContainer(
                                {

                                    if (isPickerLaunch)
                                        return@UploadServiceThumbnailContainer

                                    viewModel.setPickerLaunch(true)

                                    pickThumbnailImageLauncher.launch(
                                       Unit
                                    )
                                },
                                thumbnailContainer,
                                thumbnailContainer?.path
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {

                            Column(modifier = Modifier.fillMaxWidth()) {

                                // Text Field for Service Title
                                OutlinedTextField(
                                    value = serviceTitle,
                                    onValueChange = { viewModel.updateTitle(it) },
                                    label = { Text("Service Title") },
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
                                    label = { Text("Service Short Description") },
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

                                ExposedDropdownIndustry(selectedIndustry) {
                                    viewModel.updateIndustry(it.value)
                                }

                                selectedIndustryError?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = serviceLongDescription,
                                    onValueChange = { viewModel.updateLongDescription(it) },
                                    label = { Text("Service Long Description") },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    minLines = 6
                                )

                                if (serviceLongDescription.length > 5000) {
                                    Text(
                                        text = "Limit: ${serviceLongDescription.length}/${5000}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        ) // Adjust padding as needed
                                    )
                                }


                                longDescriptionError?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                // Plans Section
                                Text(
                                    text = "Add Plans",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                        }


                        itemsIndexed(
                            plans,
                            key = { index, _ -> index },
                            span = { _, _ -> GridItemSpan(maxLineSpan) }) { index, plan ->
                            PlanItem(
                                plan = plan.editablePlan,
                                updatePlan = { updatedPlan ->
                                    viewModel.updatePlan(
                                        index,
                                        ValidatedPlan(true, updatedPlan)
                                    )
                                },
                                onPlanRemove = {
                                    viewModel.removePlan(it)
                                },
                                onValidate = !plan.isValid
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }



                        item(span = { GridItemSpan(maxLineSpan) }) {

                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Add Plan Button

                                IconButton(
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            color = Color.Gray,
                                            shape = CircleShape
                                        )
                                        .size(40.dp),
                                    onClick = {

                                        if (plans.size >= 3) {
                                            Toast.makeText(
                                                context,
                                                "Maximum 3 plans can be added",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            viewModel.addPlan()
                                        }

                                    }
                                ) {
                                    Text(text = "+")
                                }

                                plansError?.let {
                                    ErrorText(it)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Plans Section
                                Text(
                                    text = "Set Location",
                                    style = MaterialTheme.typography.titleMedium
                                )


                            }
                        }

                        if (imageContainers.isNotEmpty()) {
                            itemsIndexed(imageContainers) { index, bitmapContainer ->

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    ) {

                                        AsyncImage(
                                            bitmapContainer.path,
                                            contentDescription = "Service image",
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
                                            if (isPickerLaunch)
                                                return@ReloadImageIconButton
                                            viewModel.setPickerLaunch(true)

                                            viewModel.updateRefreshImageIndex(index)
                                            pickSingleImageLauncher.launch(Unit)
                                        }

                                        RemoveImageIconButton {
                                            viewModel.removeContainer(index)
                                        }

                                    }

                                    bitmapContainer.errorMessage?.let {
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
                                if(imageContainers.size == MAX_IMAGES){
                                    ShortToast(
                                        context,
                                        "You already selected maximum $MAX_IMAGES images"
                                    )
                                }else{
                                    if (isPickerLaunch)
                                        return@UploadServiceImagesContainer
                                    viewModel.setPickerLaunch(true)
                                    pickImagesLauncher?.launch(
                                        Unit
                                    )
                                }
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

                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Action Buttons
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                    ) {

                                        if (status == "draft") {
                                            //
                                            OutlinedButton(
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                onClick = {
                                                    if (serviceTitle.isNotEmpty() && serviceShortDescription.isNotEmpty()) {

                                                        val draftService = DraftService(
                                                            id = draftId,
                                                            title = serviceTitle,
                                                            shortDescription = serviceShortDescription,
                                                            longDescription = serviceLongDescription,
                                                            industry = selectedIndustry,
                                                            country = selectedCountry,
                                                            state = selectedState,
                                                            status = "draft"
                                                        )

                                                        viewModel.updateDraft(
                                                            draftId,
                                                            draftService,
                                                            imageContainers,
                                                            plans.map { it.editablePlan },
                                                            selectedLocation,
                                                            thumbnailContainer
                                                        ) {
                                                            Toast.makeText(
                                                                context,
                                                                "Draft updated",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }

                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Title/Short Description can't be empty",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 8.dp)
                                            ) {
                                                Text("Update")
                                            }

                                        } else {
                                            OutlinedButton(
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                onClick = {

                                                    if (serviceTitle.isNotEmpty() && serviceShortDescription.isNotEmpty()) {


                                                        val draftService = DraftService(
                                                            title = serviceTitle,
                                                            shortDescription = serviceShortDescription,
                                                            longDescription = serviceLongDescription,
                                                            status = "draft",
                                                            industry = selectedIndustry,
                                                            country = selectedCountry,
                                                            state = selectedState
                                                        )



                                                        viewModel.draft(
                                                            draftService,
                                                            imageContainers,
                                                            plans.map { it.editablePlan },
                                                            selectedLocation,
                                                            thumbnailContainer,

                                                            ) {
                                                            Toast.makeText(
                                                                context,
                                                                "Draft saved",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }

                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Title/Short Description can't be empty",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 8.dp)
                                            ) {
                                                Text("Draft")
                                            }
                                        }

                                    }

                                    Button(
                                        shape = RoundedCornerShape(8.dp),
                                        onClick = {
                                            if (viewModel.validateAll()) {


                                                val bodyTitle =
                                                    serviceTitle.toRequestBody("text/plain".toMediaTypeOrNull())
                                                val bodyShortDescription =
                                                    serviceShortDescription.toRequestBody("text/plain".toMediaTypeOrNull())
                                                val bodyLongDescription =
                                                    serviceLongDescription.toRequestBody("text/plain".toMediaTypeOrNull())

                                                val bodyIndustry = selectedIndustry.toString()
                                                    .toRequestBody("text/plain".toMediaTypeOrNull())

                                                val bodyCountry = selectedCountry.toString()
                                                    .toRequestBody("text/plain".toMediaTypeOrNull())

                                                val bodyState = selectedState.toString()
                                                    .toRequestBody("text/plain".toMediaTypeOrNull())

                                                val bodyPlans =
                                                    Gson().toJson(plans.map {
                                                        it.editablePlan
                                                    })
                                                        .toRequestBody("application/json".toMediaType())


                                                val bodyLocation = selectedLocation?.let {
                                                    Gson().toJson(it)
                                                        .toRequestBody("application/json".toMediaType())
                                                }

                                                val bodyImages =
                                                    imageContainers.mapIndexed { index, bitmapContainer ->
                                                        createImagePartForUri(
                                                            context,
                                                            bitmapContainer.path.toUri(),
                                                            "IMAGE_${index}_${
                                                                getFileExtensionFromImageFormat(
                                                                    bitmapContainer.format
                                                                )
                                                            }",
                                                            bitmapContainer.format,
                                                            "images[]"
                                                        )
                                                    }.filterNotNull()

                                                val bodyThumbnail = thumbnailContainer!!.let {
                                                    createImagePartForUri(
                                                        context,
                                                        it.path.toUri(),
                                                        "IMAGE_THUMBNAIL_${
                                                            getFileExtensionFromImageFormat(
                                                                it.format
                                                            )
                                                        }",
                                                        it.format,
                                                        "thumbnail"
                                                    )
                                                }

                                                if (bodyThumbnail == null || bodyImages.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to create service",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                    return@Button
                                                }

                                                viewModel.onCreateService(
                                                    bodyTitle,
                                                    bodyShortDescription,
                                                    bodyLongDescription,
                                                    bodyIndustry,
                                                    bodyCountry,
                                                    bodyState,
                                                    bodyThumbnail,
                                                    bodyImages,
                                                    bodyPlans,
                                                    bodyLocation,
                                                    {
                                                        onServiceCreated()
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
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Publish")
                                    }
                                }


                                if (status == "draft") {

                                    // Delete Draft Button
                                    Button(
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        shape = RectangleShape,
                                        onClick = {
                                            viewModel.setDeleteDraftDialogVisibility(true)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text("Delete Draft")
                                    }
                                }
                            }

                        }

                    }

                }


            }


        }
    }



    if (isPublishing) {
        LoadingDialog()
    }

    // Alert Dialog
    if (deleteDraftDialogVisibility) {
        AlertDialog(
            onDismissRequest = {
                viewModel.setDeleteDraftDialogVisibility(false)
            },
            title = { Text("Delete Draft") },
            text = { Text("This action cannot be undone. Are you sure you want to delete this draft?") },
            confirmButton = {
                TextButton(
                    onClick = {

                        viewModel.deleteDraft(draftId) {
                            onServiceDeleted()
                        }

                        viewModel.setDeleteDraftDialogVisibility(false)
                    }
                ) {
                    Text("Confirm")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setDeleteDraftDialogVisibility(false)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownIndustry(
    selectedValue: Int = -1,
    isError: Boolean = false,
    onSelected: (DraftIndustry) -> Unit,
) {

    val context = LocalContext.current
    val resources = context.resources

    val industryNames: Array<String> = resources.getStringArray(R.array.dropdown_items)
    val industryValues: Array<Int> =
        resources.getIntArray(R.array.industry_values).toTypedArray()

    val industries = industryNames.zip(industryValues) { name, value ->
        DraftIndustry(name = name, value = value)
    }

    var expanded by remember { mutableStateOf(false) }

    var selectedIndustry by remember(selectedValue) {
        mutableStateOf(if (selectedValue != -1) industries.find {
            it.value == selectedValue
        } else null)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            isError = isError,
            value = selectedIndustry?.name ?: "Select Industry",
            onValueChange = {},
            readOnly = true,
            label = { if (selectedIndustry?.name != null) Text("Select Industry") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            industries.forEach { industry ->
                DropdownMenuItem(
                    text = { Text(industry.name) },
                    onClick = {
                        selectedIndustry = industry
                        onSelected(industry)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownCountry(
    selectedCountryValue: String? = null,
    selectedStateValue: String? = null,
    countryError: String?,
    stateError: String?,
    onCountrySelected: (DraftCountry) -> Unit,
    onStateSelected: (DraftState) -> Unit,
) {

    val context = LocalContext.current
    val resources = context.resources

    val countryNames: Array<String> = resources.getStringArray(R.array.country_items)
    val countryValues: Array<String> = resources.getStringArray(R.array.country_values)
    val countries = countryNames.zip(countryValues) { name, value ->
        DraftCountry(name = name, value = value)
    }

    val statesMap = mapOf(
        "IN" to listOf(
            "Andaman and Nicobar Islands",
            "Andhra Pradesh",
            "Arunachal Pradesh",
            "Assam",
            "Bihar",
            "Chandigarh",
            "Chhattisgarh",
            "Dadra and Nagar Haveli and Daman and Diu",
            "Delhi",
            "Goa",
            "Gujarat",
            "Haryana",
            "Himachal Pradesh",
            "Jammu and Kashmir",
            "Jharkhand",
            "Karnataka",
            "Kerala",
            "Ladakh",
            "Lakshadweep",
            "Madhya Pradesh",
            "Maharashtra",
            "Manipur",
            "Meghalaya",
            "Mizoram",
            "Nagaland",
            "Odisha",
            "Puducherry",
            "Punjab",
            "Rajasthan",
            "Sikkim",
            "Tamil Nadu",
            "Telangana",
            "Tripura",
            "Uttar Pradesh",
            "Uttarakhand",
            "West Bengal"
        )
    )

    var expandedCountry by remember { mutableStateOf(false) }
    var expandedState by remember { mutableStateOf(false) }

    var selectedCountry by remember(selectedCountryValue) {
        mutableStateOf(
            if (selectedCountryValue != null)
                countries.find { it.value == selectedCountryValue }
            else null
        )
    }

    var selectedState by remember(selectedStateValue) {
        mutableStateOf(selectedStateValue)
    }


    Column(modifier = Modifier.fillMaxWidth()) {

        ExposedDropdownMenuBox(
            expanded = expandedCountry,
            onExpandedChange = { expandedCountry = !expandedCountry }
        ) {
            OutlinedTextField(
                isError = countryError != null,
                value = selectedCountry?.name ?: "Select Country",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Country") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountry)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expandedCountry,
                onDismissRequest = { expandedCountry = false }
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country.name) },
                        onClick = {
                            selectedCountry = country
                            selectedState = null
                            onCountrySelected(country)
                            expandedCountry = false
                        }
                    )
                }
            }
        }


        countryError?.let {
            ErrorText(it)
        }

        selectedCountry?.let {

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedState,
                onExpandedChange = { expandedState = !expandedState }) {
                OutlinedTextField(
                    isError = stateError != null,
                    value = selectedState ?: "Select State",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select State") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedState,
                    onDismissRequest = { expandedState = false }
                ) {
                    val states = statesMap[it.value] ?: emptyList()

                    states.forEach { state ->
                        DropdownMenuItem(
                            text = { Text(state) },
                            onClick = {
                                selectedState = state
                                onStateSelected(
                                    DraftState(
                                        name = state,
                                        countryValue = it.value
                                    )
                                )
                                expandedState = false
                            }
                        )
                    }
                }
            }

            stateError?.let {
                ErrorText(it)
            }
        }
    }

}


@Composable
fun BoxScope.ReloadImageIconButton(onClick: () -> Unit) {

    IconButton(
        onClick = {
            onClick()

        },
        modifier = Modifier
            .align(Alignment.TopEnd)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .border(1.dp, Color.Gray, RoundedCornerShape(50))
            .size(24.dp)
            .zIndex(1f)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reload",
            modifier = Modifier.padding(4.dp)
        )
    }

}

@Composable
fun BoxScope.RemoveImageIconButton(onClick: () -> Unit) {

    IconButton(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .align(Alignment.TopStart)
            .size(24.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
            .border(1.dp, Color.Gray, RoundedCornerShape(50))
            .zIndex(1f)
    ) {
        Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Remove",
            modifier = Modifier.padding(4.dp)
        )
    }

}

