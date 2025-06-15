package com.lts360.compose.ui.localjobs.manage


import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.google.gson.Gson
import com.lts360.api.models.service.EditableLocation
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.chat.createImagePartForUri
import com.lts360.compose.ui.chat.getFileExtensionFromImageFormat
import com.lts360.compose.ui.chat.isValidImageDimensions
import com.lts360.compose.ui.localjobs.manage.viewmodels.LocalJobWorkFlowViewModel
import com.lts360.compose.ui.main.CreateLocalJobLocationBottomSheetScreen
import com.lts360.compose.ui.profile.TakePictureSheet
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.compose.ui.services.manage.ExposedDropdownCountry
import com.lts360.compose.ui.services.manage.ReloadImageIconButton
import com.lts360.compose.ui.services.manage.RemoveImageIconButton
import com.lts360.compose.ui.services.manage.UploadServiceImagesContainer
import com.lts360.libs.imagepicker.GalleryPagerActivityResultContracts
import com.lts360.libs.ui.ShortToast
import com.lts360.libs.utils.createImageFileDCIMExternalStorage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLocalJobScreen(
    onLocalJobCreated: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: LocalJobWorkFlowViewModel) {


    val localJob by viewModel.localJob.collectAsState()
    val salaryUnits = viewModel.salaryUnits

    val errors by viewModel.errors.collectAsState()

    /*
        val isLoading by viewModel.isDraftLoading.collectAsState()
    */

    val isLoading = false

    val isPublishing by viewModel.isPublishing.collectAsState()

    val isPickerLaunch by viewModel.isPickerLaunch.collectAsState()
    val refreshImageIndex by viewModel.refreshImageIndex.collectAsState()

    val context = LocalContext.current
    val slots by viewModel.slots.collectAsState()

    val pickImagesLauncher = if (slots > 0) {
        rememberLauncherForActivityResult(
            GalleryPagerActivityResultContracts.PickMultipleImages(
                slots,
                allowSingleItemChoose = true
            )
        ) { uris ->
            if (localJob.imageContainers.size < MAX_IMAGES) {
                if (uris.isNotEmpty()) {

                    uris.take(MAX_IMAGES - localJob.imageContainers.size).forEach { uri ->
                        val result = isValidImageDimensions(context, uri)
                        val errorMessage =
                            if (result.isValidDimension) null else "Invalid Dimension"
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
                ShortToast(context, "Only $MAX_IMAGES images are allowed")
            }

            viewModel.setPickerLaunch(false)

        }

    } else {
        null
    }


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
        } else {
            if (localJob.imageContainers.size < MAX_IMAGES) {
                if (isSuccess) {
                    requestedData?.let {
                        cameraData = it
                        val result = isValidImageDimensions(context, it)
                        val errorMessage =
                            if (result.isValidDimension) null else "Invalid Dimension"
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
                ShortToast(context, "Only $MAX_IMAGES images are allowed")

            }
        }

        viewModel.setPickerLaunch(false)

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
            onPopBackStack()
        }
    }

    var maritalStatusExpanded by remember { mutableStateOf(false) }
    var salaryExpanded by remember { mutableStateOf(false) }


    BottomSheetScaffold(
        sheetDragHandle = null,
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            if (bottomSheetScaffoldState.bottomSheetState.isVisible) {
                CreateLocalJobLocationBottomSheetScreen(
                    bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden,
                    {
                        viewModel.updateLocation(
                            EditableLocation(
                                serviceId = -1,
                                latitude = it.latitude,
                                longitude = it.longitude,
                                locationType = it.locationType,
                                geo = it.geo))
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.hide()
                        }
                    },
                    {
                        viewModel.updateLocation(
                            EditableLocation(
                                serviceId = -1,
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
                            EditableLocation(
                                serviceId = -1,
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
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = false
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
                            text = "Create Local Job",
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
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxWidth(),
                        columns = GridCells.Adaptive(140.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Add New Local Job",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.fillMaxWidth()) {

                                OutlinedTextField(
                                    value = localJob.title,
                                    onValueChange = { viewModel.updateTitle(it) },
                                    label = { Text("Title") },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    isError = errors.title != null
                                )

                                if (localJob.title.length > 100) {
                                    Text(
                                        text = "Limit: ${localJob.title.length}/${100}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }

                                errors.title?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))


                                OutlinedTextField(
                                    value = localJob.description,
                                    onValueChange = { viewModel.updateDescription(it) },
                                    label = { Text("Description") },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    minLines = 4,
                                    isError = errors.description != null
                                )


                                if (localJob.description.length > 250) {
                                    Text(
                                        text = "Limit: ${localJob.description.length}/${250}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }

                                errors.description?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = localJob.company,
                                    onValueChange = {
                                        viewModel.updateCompany(it)
                                    },
                                    label = { Text("Company") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 1,
                                    isError = errors.company != null
                                )

                                errors.company?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                AgeRangeSelector(
                                    value = localJob.ageMin.toFloat()..localJob.ageMax.toFloat(),
                                    onUpdateAgeMin = {
                                        viewModel.updateAgeMin(it)
                                    },
                                    onUpdateAgeMax = {
                                        viewModel.updateAgeMax(it)
                                    },
                                    error = errors.age
                                )

                                errors.age?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))


                                ExposedDropdownMenuBox(
                                    expanded = maritalStatusExpanded,
                                    onExpandedChange = {
                                        maritalStatusExpanded = !maritalStatusExpanded
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp))
                                ) {
                                    OutlinedTextField(
                                        value = localJob.maritalStatuses
                                            .filter { it.isSelected }
                                            .joinToString(", ") { it.status.value },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Marital Status") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = maritalStatusExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                            .fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = maritalStatusExpanded,
                                        onDismissRequest = { maritalStatusExpanded = false }
                                    ) {
                                        localJob.maritalStatuses.forEachIndexed { index, item ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Checkbox(
                                                            checked = item.isSelected,
                                                            onCheckedChange = null
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(text = item.status.value)
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.toggleMaritalStatus(index)
                                                }
                                            )
                                        }
                                    }
                                }



                                errors.maritalStatuses?.let {
                                    ErrorText(it)
                                }

                                Spacer(modifier = Modifier.height(8.dp))


                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    Column(modifier = Modifier.fillMaxWidth()
                                        .weight(1f)){
                                        ExposedDropdownMenuBox(
                                            expanded = salaryExpanded,
                                            onExpandedChange = { salaryExpanded = !salaryExpanded },
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(4.dp)
                                                )

                                        ) {
                                            OutlinedTextField(
                                                value = localJob.salaryUnit,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Salary") },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = salaryExpanded)
                                                },
                                                modifier = Modifier
                                                    .menuAnchor(
                                                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                                    )
                                                    .fillMaxWidth()
                                            )

                                            ExposedDropdownMenu(
                                                expanded = salaryExpanded,
                                                onDismissRequest = { salaryExpanded = false }
                                            ) {
                                                salaryUnits.forEach {
                                                    DropdownMenuItem(
                                                        text = { Text(text = it) },
                                                        onClick = {
                                                            viewModel.updateSalaryUnit(it)
                                                            salaryExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        errors.salaryUnit?.let {
                                            ErrorText(it)
                                        }
                                    }

                                    Column(modifier = Modifier.fillMaxWidth()
                                        .weight(1f)){

                                        OutlinedTextField(
                                            value = if (localJob.salaryMin == -1) "" else localJob.salaryMin.toString(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            onValueChange = {
                                                viewModel.updateSalaryMin(it.toIntOrNull() ?: -1)
                                            },
                                            label = { Text("Salary Min") },
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            minLines = 1,
                                            isError = errors.salaryMin != null
                                        )

                                        errors.salaryMin?.let {
                                            ErrorText(it)
                                        }

                                    }


                                    Column(modifier = Modifier.fillMaxWidth()
                                        .weight(1f)){

                                        OutlinedTextField(
                                            value = if (localJob.salaryMax == -1) "" else localJob.salaryMax.toString(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            onValueChange = {
                                                viewModel.updateSalaryMax(it.toIntOrNull() ?: -1)
                                            },
                                            label = { Text("Salary Max (Optional)") },
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            minLines = 1,
                                            isError = errors.salaryMax != null
                                        )

                                        errors.salaryMax?.let {
                                            ErrorText(it)
                                        }
                                    }

                                }

                            }

                        }

                        if (localJob.imageContainers.isNotEmpty()) {
                            itemsIndexed(localJob.imageContainers) { index, bitmapContainer ->

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    ) {

                                        AsyncImage(
                                            bitmapContainer.path,
                                            contentDescription = "Local job image",
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
                                            viewModel.updateRefreshImageIndex(index)
                                            pickerSheetState = true
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

                        item(span = { GridItemSpan(maxLineSpan) }) {


                            UploadServiceImagesContainer(errors.imageContainers) {
                                if (localJob.imageContainers.size == MAX_IMAGES) {
                                    ShortToast(
                                        context,
                                        "You already selected maximum $MAX_IMAGES images"
                                    )
                                } else {
                                    pickerSheetState = true
                                }
                            }

                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {

                            ExposedDropdownCountry(
                                localJob.country,
                                localJob.state,
                                errors.country,
                                errors.state,
                                {
                                    viewModel.updateCountry(it.value)
                                }
                            ) {
                                viewModel.updateState(it.name)
                            }

                        }


                        item(span = { GridItemSpan(maxLineSpan) }) {

                            Column(modifier = Modifier.fillMaxWidth()) {

                                OutlinedTextField(
                                    readOnly = true,
                                    value = localJob.selectedLocation?.geo ?: "",
                                    onValueChange = { },
                                    label = { Text("Location") },
                                    trailingIcon = {

                                        IconButton({
                                            coroutineScope.launch {
                                                bottomSheetScaffoldState.bottomSheetState.expand()
                                            }
                                        }) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.RotateLeft,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )

                                errors.selectedLocation?.let {
                                    ErrorText(it)
                                }
                            }

                        }


                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Button(
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    if (viewModel.validateAll()) {

                                        val localJobId = (-1).toString()
                                            .toRequestBody("text/plain".toMediaTypeOrNull())

                                        val bodyTitle =
                                            localJob.title.toRequestBody("text/plain".toMediaTypeOrNull())
                                        val bodyDescription =
                                            localJob.description.toRequestBody("text/plain".toMediaTypeOrNull())
                                        val bodyCompany =
                                            localJob.company.toRequestBody("text/plain".toMediaTypeOrNull())

                                        val bodyAgeMin = localJob.ageMin.let {
                                            Gson().toJson(it)
                                                .toRequestBody("text/plain".toMediaTypeOrNull())
                                        }

                                        val bodyAgeMax = localJob.ageMax.let {
                                            Gson().toJson(it)
                                                .toRequestBody("text/plain".toMediaTypeOrNull())
                                        }

                                        val bodyMaritalStatusList = localJob.maritalStatuses.filter {
                                            it.isSelected
                                        }.map { it.status.key }.map {
                                            it.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                        }

                                        val bodySalaryUnit = localJob.salaryUnit
                                            .toRequestBody("text/plain".toMediaTypeOrNull())

                                        val bodySalaryMin = localJob.salaryMin.let {
                                            Gson().toJson(it)
                                                .toRequestBody("text/plain".toMediaType())
                                        }

                                        val bodySalaryMax = localJob.salaryMax.let {
                                            Gson().toJson(it)
                                                .toRequestBody("text/plain".toMediaType())
                                        }

                                        val bodyKeepImageIds = emptyList<RequestBody>()

                                        val bodyImages =
                                            localJob.imageContainers.mapIndexed { index, bitmapContainer ->
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

                                        val bodyCountry = localJob.country.toString()
                                            .toRequestBody("text/plain".toMediaTypeOrNull())

                                        val bodyState = localJob.state.toString()
                                            .toRequestBody("text/plain".toMediaTypeOrNull())

                                        val bodyLocation = localJob.selectedLocation?.let {
                                            Gson().toJson(it)
                                                .toRequestBody("application/json".toMediaType())
                                        }

                                        viewModel.onCreateLocalJob(
                                            localJobId,
                                            bodyTitle,
                                            bodyDescription,
                                            bodyCompany,
                                            bodyAgeMin,
                                            bodyAgeMax,
                                            bodyMaritalStatusList,
                                            bodySalaryUnit,
                                            bodySalaryMin,
                                            bodySalaryMax,
                                            bodyImages,
                                            bodyKeepImageIds,
                                            bodyCountry,
                                            bodyState,
                                            bodyLocation,
                                            {
                                                onLocalJobCreated()
                                                ShortToast(context, it)
                                            }) {
                                            ShortToast(context, it)
                                        }

                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Publish")
                            }
                        }
                    }

                }

            }
        }
    }


    TakePictureSheet(pickerSheetState, onGallerySelected = {
        if (isPickerLaunch)
            return@TakePictureSheet
        viewModel.setPickerLaunch(true)
        if (refreshImageIndex != -1) {
            pickSingleImageLauncher.launch(Unit)
        } else {
            pickImagesLauncher?.launch(Unit)
        }
        pickerSheetState = false
    }, onCameraSelected = {
        createImageFileDCIMExternalStorage(
            context = context,
            "Lts360/Local Jobs"
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



@Composable
fun AgeRangeSelector(
    value: ClosedFloatingPointRange<Float>,
    ageRange: ClosedFloatingPointRange<Float> = 18f..60f,
    error: String?,
    onUpdateAgeMin:(Int)-> Unit,
    onUpdateAgeMax: (Int)-> Unit
) {
    var sliderPosition by remember {
        mutableStateOf(value)
    }

    Column {
        Text("Age Range: ${sliderPosition.start.toInt()} - ${sliderPosition.endInclusive.toInt()}")

        RangeSlider(
            value = value,
            onValueChange = { newRange ->
                sliderPosition = newRange
                onUpdateAgeMin(sliderPosition.start.toInt())
                onUpdateAgeMax(sliderPosition.endInclusive.toInt())
            },
            onValueChangeFinished = {},
            valueRange = ageRange,
            steps = (ageRange.endInclusive - ageRange.start).toInt() - 1,
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            ErrorText(it)
        }
    }
}



