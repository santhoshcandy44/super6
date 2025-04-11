package com.lts360.test

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.lts360.BuildConfig
import com.lts360.R
import com.lts360.compose.ui.NoRippleInteractionSource
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.profile.TakePictureSheet
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.utils.FormatterUtils.humanReadableBytesSize
import com.lts360.libs.imagecrop.CropProfilePicActivityContracts
import com.lts360.libs.imagepicker.GalleryPagerActivityResultContracts
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ApplicantProfileForm(viewModel: ApplicantProfileViewModel = hiltViewModel()) {

    val step by viewModel.step.collectAsState()
    val completedStep by viewModel.completedStep.collectAsState()

    val jobProfessionalInfo by viewModel.jobProfessionalInfo.collectAsState()
    val jobProfessionalInfoError by viewModel.jobProfessionalInfoError.collectAsState()

    val isImagePickerLaunched by viewModel.isImagePickerLauncherLaunched.collectAsState()
    val isCameraPickerLaunched by viewModel.isCameraPickerLauncherLaunched.collectAsState()
    val cameraPickerUri by viewModel.cameraPickerUri.collectAsState()


    val educationList by viewModel.educationList.collectAsState()

    val experienceList by viewModel.experienceList.collectAsState()
    val hasNoExperience by viewModel.hasNoExperience.collectAsState()

    val selectedSkills by viewModel.selectedSkills.collectAsState()
    val certificates by viewModel.certificates.collectAsState()

    val userPrefsLanguages by viewModel.userPrefsLanguages.collectAsState()

    // Collect state from the ViewModel
    val fileName by viewModel.fileName.collectAsState()
    val fileSizeInBytes by viewModel.fileSizeInBytes.collectAsState()
    val lastModified by viewModel.lastModified.collectAsState()

    val jobLanguages = viewModel.jobLanguages

    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->

            Box(modifier = Modifier.fillMaxSize()){

                if(!isLoading){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {

                        StepProgressIndicator(
                            totalSteps = 7,
                            currentStep = step,
                            completedStep = completedStep
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (step) {
                                0 -> PersonalInfoFormM3(
                                    isImagePickerLauncherLaunched = isImagePickerLaunched,
                                    isCameraPickerLauncherLaunched = isCameraPickerLaunched,
                                    cameraPickerUri = cameraPickerUri,
                                    jobProfessionalInfo = jobProfessionalInfo,
                                    jobProfessionalInfoError = jobProfessionalInfoError,
                                    onFirstNameChange = viewModel::onFirstNameChange,
                                    onLastNameChange = viewModel::onLastNameChange,
                                    onGenderChange = viewModel::onGenderChange,
                                    onEmailChange = viewModel::onEmailChange,
                                    onIntroChange = viewModel::onIntroChange,
                                    onUpdateProfilePic = viewModel::onUpdateProfilePic,
                                    onUpdateImagePickerLaunched = viewModel::onUpdateImagePickerLaunched,
                                    onUpdateCameraPickerLaunched = viewModel::onUpdateCameraPickerLaunched,
                                    onUpdateCameraUri = viewModel::onUpdateCameraUri
                                )

                                1 -> EducationFormM3(
                                    educationList = educationList,
                                    onEducationListChange = viewModel::updateEducationList
                                )

                                2 -> ExperienceFormM3(
                                    experienceList = experienceList,
                                    hasNoExperience = hasNoExperience,
                                    onExperienceListChange = viewModel::updateExperienceList,
                                    onHasNoExperienceChange = viewModel::setHasNoExperience
                                )

                                3 -> SkillsFormM3(
                                    selectedSkills = selectedSkills,
                                    onSkillsChanged = viewModel::updateSelectedSkills,
                                    onSkillRemoved = viewModel::removeSkill
                                )

                                4 -> CertificationsFormM3(
                                    certificates = certificates,
                                    onCertificateUpdated = { index, updatedCertificate ->
                                        viewModel.updateCertificate(index, updatedCertificate)
                                    },
                                    onCertificateRemoved = { index ->
                                        viewModel.removeCertificate(index)
                                    },
                                    onAddCertificate = {
                                        viewModel.addCertificate()
                                    }
                                )

                                5 -> LanguagesFormM3(
                                    jobLanguages = jobLanguages,
                                    userPrefsLanguages = userPrefsLanguages,
                                    onAddLanguage = { viewModel.addLanguage() },
                                    onRemoveLanguage = { viewModel.removeLanguage(userPrefsLanguages.size - 1) },
                                    onLanguageSelected = { index, language ->
                                        viewModel.updateLanguage(
                                            index,
                                            language
                                        )
                                    },
                                    onProficiencySelected = { index, proficiency ->
                                        viewModel.updateProficiency(
                                            index,
                                            proficiency
                                        )
                                    })

                                6 -> ResumeUploadSectionM3(
                                    fileName = fileName,
                                    fileSizeInBytes = fileSizeInBytes,
                                    lastModified = lastModified,
                                    onUploadClicked = {
                                        // Handle file upload, e.g., open file picker
                                        val newFileName = "New_Resume.pdf"
                                        val newFileSizeInBytes: Long = 1024 * 1024 * 3 // 3MB
                                        val newLastModified = System.currentTimeMillis()

                                        // Update the ViewModel with new file details
                                        viewModel.updateFile(
                                            newFileName,
                                            newFileSizeInBytes,
                                            newLastModified
                                        )
                                    },
                                    onRemoveClicked = {
                                        // Remove the current file
                                        viewModel.removeFile()
                                    }
                                )
                            }
                        }

                        FormNavigationControlsM3(
                            currentStep = step,
                            onPrevious = {
                                viewModel.previousStep()
                            },
                            onNext = {
                                if (step == 0) {
                                    if (viewModel.validatePersonalInfoForm()) {

                                        viewModel.onUpdatePersonalInfoForm(
                                            jobProfessionalInfo = jobProfessionalInfo,
                                            onError = { errorMessage ->
                                                errorMessage?.let {
                                                    ShortToast(context, it)
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    if (step < 7) viewModel.nextStep() else viewModel.nextStep()
                                }
                            },
                            isUpdating
                        )
                    }
                }else{
                    CircularProgressIndicatorLegacy(
                        modifier = Modifier
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            }

        }

        if (isUpdating) {
            LoadingDialog()
        }
    }
}


@Composable
fun StepProgressIndicator(
    totalSteps: Int,
    currentStep: Int,
    completedStep: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until totalSteps) {
            val isCompleted = i <= completedStep && i != currentStep
            val isCurrent = i == currentStep

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFF80FF80)
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> Color.Gray
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${i + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (i < totalSteps - 1) {
                Spacer(modifier = Modifier.width(8.dp))
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .align(Alignment.CenterVertically),
                    color = if (i <= completedStep) Color(0xFF80FF80) else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}


@Composable
private fun FormNavigationControlsM3(
    currentStep: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isLoading:Boolean=false
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (currentStep > 0) {
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable(interactionSource = NoRippleInteractionSource(), indication = null) {
                        onPrevious()
                    }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }


        Surface(
            modifier = Modifier.clickable {
                onNext()
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ) {


            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(if (currentStep == 7) "Submit" else "Save & Continue")

                if(isLoading){
                    CircularProgressIndicatorLegacy(modifier = Modifier.size(24.dp))
                }else{
                    if (currentStep < 7) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
            }

        }

    }
}


@Composable
private fun BoxScope.PersonalInfoFormM3(
    isImagePickerLauncherLaunched: Boolean,
    isCameraPickerLauncherLaunched: Boolean,
    cameraPickerUri: Uri?,
    jobProfessionalInfo: JobProfessionalInfo,
    jobProfessionalInfoError: JobProfessionalInfoError,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onIntroChange: (String) -> Unit,
    onUpdateProfilePic: (Uri) -> Unit,
    onUpdateImagePickerLaunched: (Boolean) -> Unit,
    onUpdateCameraPickerLaunched: (Boolean) -> Unit,
    onUpdateCameraUri: (Uri) -> Unit,
) {

    val context = LocalContext.current

    var profilePickerState by rememberSaveable { mutableStateOf(false) }


    val cropLauncher = rememberLauncherForActivityResult(
        CropProfilePicActivityContracts.ImageCropper()
    ) { uri ->

        uri?.let {
            onUpdateProfilePic(it)
        }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(GalleryPagerActivityResultContracts.PickSingleImage()) { uri: Uri? ->

            uri?.let { selectedUri ->

                try {

                    // Step 1: Get InputStream from URI safely
                    val inputStream: InputStream? =
                        context.contentResolver.openInputStream(selectedUri)

                    // Step 2: Decode InputStream to Bitmap
                    val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }

                    if (bitmap != null) {
                        // Step 3: Get image width and height
                        val width = bitmap.width
                        val height = bitmap.height

                        // Step 4: Check if the image meets the minimum size (100px by 100px)
                        if (width >= 100 && height >= 100) {
                            // Step 5: Check the aspect ratio (1:1)
                            if (width == height) {
                                onUpdateProfilePic(selectedUri)
                            } else {
                                cropLauncher.launch(selectedUri)
                            }
                        } else {

                            Toast.makeText(context, "Image is too small", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            onUpdateCameraPickerLaunched(false)

        }


    val cameraImagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()

    ) { isCaptured ->

        if (isCaptured) {
            cameraPickerUri?.let {
                onUpdateProfilePic(it)
            }
        }
        onUpdateCameraPickerLaunched(false)

    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
            ) {
                jobProfessionalInfo.profilePic?.let {
                    AsyncImage(
                        it,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(R.drawable.user_placeholder),
                        error = painterResource(R.drawable.user_placeholder),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    Image(
                        painter = painterResource(R.drawable.user_placeholder),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                    IconButton(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        onClick = {
                            profilePickerState = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit profile pic"
                        )
                    }
                }
            }
        }

        item {
            CustomTextField(
                value = jobProfessionalInfo.firstName,
                onValueChange = onFirstNameChange,
                label = "First Name",
                errorMessage = jobProfessionalInfoError.firstName
            )
        }

        item {
            CustomTextField(
                value = jobProfessionalInfo.lastName,
                onValueChange = onLastNameChange,
                label = "Last Name",
                errorMessage = jobProfessionalInfoError.lastName
            )
        }

        item {
            GenderSelector(
                selectedGender = jobProfessionalInfo.gender,
                onGenderSelected = onGenderChange,
                errorMessage = jobProfessionalInfoError.gender
            )
        }

        item {
            CustomTextField(
                value = jobProfessionalInfo.email,
                onValueChange = onEmailChange,
                label = "Email",
                errorMessage = jobProfessionalInfoError.email
            )
        }

        item {
            CustomTextField(
                value = jobProfessionalInfo.intro,
                onValueChange = onIntroChange,
                label = "Intro",
                minLines = 5,
                errorMessage = jobProfessionalInfoError.intro
            )
        }
    }



    TakePictureSheet(
        profilePickerState,
        {
            if (isImagePickerLauncherLaunched)
                return@TakePictureSheet
            onUpdateImagePickerLaunched(true)
            imagePickerLauncher.launch(Unit)
            profilePickerState = false
        }, {
            if (isCameraPickerLauncherLaunched)
                return@TakePictureSheet

            onUpdateCameraPickerLaunched(true)

            val newUri = FileProvider.getUriForFile(
                context, "${BuildConfig.APPLICATION_ID}.provider",
                File(context.cacheDir, "IMG_${System.currentTimeMillis()}.jpg")
            )

            onUpdateCameraUri(newUri)
            cameraImagePickerLauncher.launch(newUri)
            profilePickerState = false
        }, {
            profilePickerState = false
        })
}


data class EducationEntry(
    val institution: String = "",
    val fieldOfStudy: String = "",
    val startYear: String = "",
    val endYear: String = "",
    val grade: String = ""
)


@Composable
private fun EducationFormM3(
    educationList: List<EducationEntry>,
    onEducationListChange: (List<EducationEntry>) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            Text(
                text = "Education Information",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        itemsIndexed(educationList) { index, entry ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    value = entry.institution,
                    onValueChange = {
                        onEducationListChange(
                            educationList.toMutableList().apply {
                                this[index] = this[index].copy(institution = it)
                            }
                        )
                    },
                    label = "Institution/School"
                )

                CustomTextField(
                    value = entry.fieldOfStudy,
                    onValueChange = {
                        onEducationListChange(
                            educationList.toMutableList().apply {
                                this[index] = this[index].copy(fieldOfStudy = it)
                            }
                        )
                    },
                    label = "Field of Study"
                )

                CustomTextField(
                    value = entry.startYear,
                    onValueChange = {
                        onEducationListChange(
                            educationList.toMutableList().apply {
                                this[index] = this[index].copy(startYear = it)
                            }
                        )
                    },
                    label = "Start Year"
                )

                CustomTextField(
                    value = entry.endYear,
                    onValueChange = {
                        onEducationListChange(
                            educationList.toMutableList().apply {
                                this[index] = this[index].copy(endYear = it)
                            }
                        )
                    },
                    label = "End Year"
                )

                CustomTextField(
                    value = entry.grade,
                    onValueChange = {
                        onEducationListChange(
                            educationList.toMutableList().apply {
                                this[index] = this[index].copy(grade = it)
                            }
                        )
                    },
                    label = "Grade (e.g., 3.8 GPA)"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (educationList.size > 1) {
                        RectangleButton(icon = Icons.Default.Remove, label = "Remove") {
                            onEducationListChange(
                                educationList.toMutableList().apply {
                                    removeAt(index)
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.aligned(Alignment.End)
            ) {
                if (educationList.size < 3) {
                    RectangleButton(icon = Icons.Default.Add, label = "Add School") {
                        onEducationListChange(educationList + EducationEntry())
                    }
                }
            }
        }
    }
}


data class ExperienceEntry(
    var companyName: String = "",
    var jobTitle: String = "",
    var employmentType: String = "",
    var location: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var isCurrentJob: Boolean = false
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperienceFormM3(
    experienceList: List<ExperienceEntry>,
    hasNoExperience: Boolean,
    onExperienceListChange: (List<ExperienceEntry>) -> Unit,
    onHasNoExperienceChange: (Boolean) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis // use selected date if needed
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Experience Information",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    Checkbox(
                        checked = hasNoExperience,
                        onCheckedChange = onHasNoExperienceChange
                    )
                }
                Text("I have no experience yet")
            }
        }

        if (!hasNoExperience) {
            itemsIndexed(experienceList) { index, entry ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomTextField(
                        value = entry.jobTitle,
                        onValueChange = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(jobTitle = it)
                            })
                        },
                        label = "Job Title"
                    )

                    CustomTextField(
                        value = entry.employmentType,
                        onValueChange = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(employmentType = it)
                            })
                        },
                        label = "Employment Type"
                    )

                    CustomTextField(
                        value = entry.companyName,
                        onValueChange = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(companyName = it)
                            })
                        },
                        label = "Company or Organization"
                    )

                    val isAnotherCurrentJobExists = experienceList.any {
                        it.isCurrentJob && it != entry
                    }

                    if (!isAnotherCurrentJobExists || entry.isCurrentJob) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                Checkbox(
                                    checked = entry.isCurrentJob,
                                    onCheckedChange = { isChecked ->
                                        onExperienceListChange(
                                            experienceList.toMutableList().apply {
                                                this[index] =
                                                    this[index].copy(isCurrentJob = isChecked)
                                                if (isChecked) {
                                                    forEachIndexed { i, item ->
                                                        if (i != index && item.isCurrentJob) {
                                                            this[i] =
                                                                item.copy(isCurrentJob = false)
                                                        }
                                                    }
                                                }
                                            })
                                    }
                                )
                            }

                            Text("I currently work here")
                        }
                    }

                    CustomTextField(
                        value = entry.startDate,
                        onValueChange = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(startDate = it)
                            })
                        },
                        label = "Start Date (e.g., Jan 2020)"
                    )

                    if (!entry.isCurrentJob) {
                        CustomTextField(
                            value = entry.endDate,
                            onValueChange = {
                                onExperienceListChange(experienceList.toMutableList().apply {
                                    this[index] = this[index].copy(endDate = it)
                                })
                            },
                            label = "End Date (e.g., Dec 2023)"
                        )
                    }

                    CustomTextField(
                        value = entry.location,
                        onValueChange = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(location = it)
                            })
                        },
                        label = "Location"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        if (experienceList.size > 1) {
                            RectangleButton(icon = Icons.Default.Remove, label = "Remove") {
                                onExperienceListChange(experienceList.toMutableList().apply {
                                    removeAt(index)
                                })
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.dp)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (experienceList.size < 5) {
                        RectangleButton(icon = Icons.Default.Add, label = "Add") {
                            onExperienceListChange(experienceList + ExperienceEntry())
                        }
                    }
                }
            }
        }
    }
}


data class JobSkill(val skill: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillsFormM3(
    selectedSkills: List<JobSkill>,
    onSkillsChanged: (List<JobSkill>) -> Unit,
    onSkillRemoved: (JobSkill) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val skillsChooserModalBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Skills", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            CustomTextField(
                value = selectedSkills.joinToString(", ") { it.skill },
                onValueChange = {}, // readOnly
                label = "Skill Name (e.g., Kotlin, Figma)",
                readOnly = true,
                onFocus = {
                    if (it) {
                        scope.launch { skillsChooserModalBottomSheetState.expand() }
                    }
                }
            )
        }

        if (selectedSkills.isNotEmpty()) {
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedSkills.forEach { skill ->
                        SkillChip(
                            skill = skill.skill,
                            onRemove = { onSkillRemoved(skill) }
                        )
                    }
                }
            }
        }
    }

    // Skill Picker Modal
    if (skillsChooserModalBottomSheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                focusManager.clearFocus()
                scope.launch { skillsChooserModalBottomSheetState.hide() }
            },
            sheetState = skillsChooserModalBottomSheetState,
            dragHandle = null,
            shape = RectangleShape,
            modifier = Modifier.safeDrawingPadding()
        ) {
            SkillsPickerScreen(
                allSkills = listOf(
                    "Kotlin", "Java", "Swift", "React", "Figma", "Photoshop",
                    "Python", "Node.js", "Flutter", "SQL", "Docker", "AWS"
                ),
                onSkillsSelected = { skills ->
                    onSkillsChanged(skills.map { JobSkill(it) })
                    focusManager.clearFocus()
                    scope.launch { skillsChooserModalBottomSheetState.hide() }
                }
            )
        }
    }
}


@Composable
fun SkillsPickerScreen(
    allSkills: List<String>,
    onSkillsSelected: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val filteredSkills = remember(searchQuery) {
        allSkills.filter { it.contains(searchQuery.text, ignoreCase = true) }
    }

    var selectedSkills by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize()) {
        com.lts360.compose.ui.main.SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isBackButtonEnabled = false,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedSkills.forEach { skill ->
                            SkillChip(
                                skill = skill,
                                onRemove = {
                                    selectedSkills = selectedSkills - skill
                                }
                            )
                        }
                    }
                }

                items(filteredSkills) { skill ->
                    val isSelected = selectedSkills.contains(skill)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSkills = if (isSelected) {
                                    selectedSkills - skill
                                } else {
                                    selectedSkills + skill
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Text(skill, modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { onSkillsSelected(selectedSkills) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
            }

        }
    }
}


@Composable
fun SkillChip(
    skill: String,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit = {},
    enableRemoveButton: Boolean = false
) {
    Surface(
        modifier = modifier.wrapContentSize(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable { onRemove() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = skill,
                style = MaterialTheme.typography.bodyMedium
            )

            if (enableRemoveButton) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove skill",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}


data class JobCertificateInfo(val issuedBy: String = "", val image: String? = null)

@Composable
private fun CertificationsFormM3(
    certificates: List<JobCertificateInfo>,
    onCertificateUpdated: (Int, JobCertificateInfo) -> Unit,
    onCertificateRemoved: (Int) -> Unit,
    onAddCertificate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Certifications",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        itemsIndexed(certificates) { index, certificate ->

            // Issued By Field
            CustomTextField(
                value = certificate.issuedBy,
                onValueChange = {
                    onCertificateUpdated(index, certificate.copy(issuedBy = it))
                },
                label = "Issued By",
                icon = Icons.Default.Business
            )

            Spacer(Modifier.height(16.dp))

            // Image Picker Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable {
                        // Open image picker logic here
                    }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                certificate.image?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Certificate image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Tap to upload certificate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Add/Remove Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (certificates.size > 1) {
                    RectangleButton(icon = Icons.Default.Remove, label = "Remove") {
                        onCertificateRemoved(certificates.size - 1)
                    }
                }

                if (certificates.size < 5) {
                    RectangleButton(icon = Icons.Default.Add, label = "Add") {
                        onAddCertificate()
                    }
                }
            }
        }
    }
}


data class JobProfileLanguage(val language: JobLanguageOption? = null, val proficiency: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagesFormM3(
    jobLanguages: List<JobLanguageOption>,
    userPrefsLanguages: List<JobProfileLanguage>,
    onAddLanguage: () -> Unit,
    onRemoveLanguage: () -> Unit,
    onLanguageSelected: (Int, JobLanguageOption) -> Unit,
    onProficiencySelected: (Int, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val languageChooserModalBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val proficiencyChooserModalBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Languages",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        itemsIndexed(userPrefsLanguages) { index, item ->
            CustomTextField(
                value = item.language?.name ?: "",
                onValueChange = {},
                label = "Language (e.g., English, Tamil)",
                readOnly = true,
                onFocus = {
                    if (it) {
                        selectedIndex = index
                        scope.launch { languageChooserModalBottomSheetState.expand() }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            CustomTextField(
                value = item.proficiency,
                onValueChange = {},
                label = "Proficiency (e.g., Fluent, Intermediate)",
                readOnly = true,
                onFocus = {
                    if (it) {
                        selectedIndex = index
                        scope.launch { proficiencyChooserModalBottomSheetState.expand() }
                    }
                }
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (userPrefsLanguages.size > 1) {
                    RectangleButton(icon = Icons.Default.Remove, label = "Remove") {
                        onRemoveLanguage()
                    }
                }

                if (userPrefsLanguages.size < 5) {
                    RectangleButton(icon = Icons.Default.Add, label = "Add") {
                        onAddLanguage()
                    }
                }
            }
        }
    }

    // Language Picker
    if (languageChooserModalBottomSheetState.currentValue == SheetValue.Expanded) {
        ModalBottomSheet(
            onDismissRequest = {
                focusManager.clearFocus()
                scope.launch { languageChooserModalBottomSheetState.hide() }
            },
            sheetState = languageChooserModalBottomSheetState,
            dragHandle = null,
            shape = RectangleShape,
            modifier = Modifier.safeDrawingPadding()
        ) {
            LanguagePickerScreen(
                jobLanguages,
                onLanguageSelected = { selectedLanguage ->
                    onLanguageSelected(selectedIndex, selectedLanguage)
                    focusManager.clearFocus()
                    scope.launch { languageChooserModalBottomSheetState.hide() }
                }
            )
        }
    }

    // Proficiency Picker
    if (proficiencyChooserModalBottomSheetState.currentValue == SheetValue.Expanded) {
        ModalBottomSheet(
            onDismissRequest = {
                focusManager.clearFocus()
                scope.launch { proficiencyChooserModalBottomSheetState.hide() }
            },
            sheetState = proficiencyChooserModalBottomSheetState,
            dragHandle = null,
            shape = RectangleShape,
            modifier = Modifier.safeDrawingPadding()
        ) {
            ProficiencyPickerScreen { selectedProficiency ->
                onProficiencySelected(selectedIndex, selectedProficiency)
                focusManager.clearFocus()
                scope.launch { proficiencyChooserModalBottomSheetState.hide() }
            }
        }
    }
}


@Composable
fun RectangleButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp), // Rectangle-like
        modifier = modifier.size(width = 32.dp, height = 32.dp), // Adjust size as needed
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White
        )
    }
}


@Composable
fun LanguagePickerScreen(
    allLanguages: List<JobLanguageOption>,
    onLanguageSelected: (JobLanguageOption) -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    var filteredLanguages by remember { mutableStateOf(allLanguages) }

    LaunchedEffect(searchQuery) {
        filteredLanguages = allLanguages.filter {
            it.name.contains(searchQuery.text, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        com.lts360.compose.ui.main.SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isBackButtonEnabled = false,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(filteredLanguages) { language ->
                Text(
                    text = language.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(language) }
                        .padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun ProficiencyPickerScreen(
    proficiencies: List<String> = listOf("Fluent", "Intermediate", "Basic"),
    onProficiencySelected: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Select Proficiency",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(16.dp)
        )

        proficiencies.forEach { level ->
            Text(
                text = level,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onProficiencySelected(level)
                    }
                    .padding(16.dp)
            )
        }
    }
}


@Composable
private fun ResumeUploadSectionM3(
    fileName: String,
    fileSizeInBytes: Long,
    lastModified: Long,
    onUploadClicked: () -> Unit,
    onRemoveClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Upload Resume",
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = "Always update latest updated Resume",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.customColorScheme.grayTextVariant1
            )

            // Resume Card Display
            if (fileName.isNotEmpty()) {
                ResumeCard(
                    fileName = fileName,
                    fileSizeInBytes = fileSizeInBytes,
                    lastModified = lastModified,
                    onRemoveClicked = onRemoveClicked
                )
            }

            // Upload Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Color.LightGray),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onUploadClicked() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Choose a file",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // File Types and Size Info
            Text(
                text = "PDF, DOC/DOCX (MAX 3MB)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.customColorScheme.grayTextVariant1
            )
        }

        // Default Resume Info
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "This resume will be used by default, but you can change it when applying.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.customColorScheme.grayTextVariant1
            )
        }
    }
}

@Composable
fun ResumeCard(
    fileName: String,
    fileSizeInBytes: Long,
    lastModified: Long,
    removeButtonEnabled: Boolean = true,
    onRemoveClicked: () -> Unit = {}
) {
    val fileExtension = fileName.substringAfterLast('.', "").lowercase()
    val isPdf = fileExtension == "pdf"
    val isDoc = fileExtension == "doc" || fileExtension == "docx"

    val fileColor = when {
        isPdf -> Color(0xFFD32F2F) // Red for PDF
        isDoc -> Color(0xFF1976D2) // Blue for DOC/DOCX
        else -> Color.Gray
    }

    val fileIcon = Icons.Default.Description
    val fileSizeInMB = humanReadableBytesSize(fileSizeInBytes)

    val lastModifiedDate = remember(lastModified) {
        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(lastModified))
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = fileIcon,
                contentDescription = null,
                tint = fileColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = fileColor,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (removeButtonEnabled) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    onRemoveClicked()
                                }
                        )
                    }

                }

                Text(
                    text = "Last used: $lastModifiedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.customColorScheme.grayTextVariant1
                )
                Text(
                    text = "Size: $fileSizeInMB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.customColorScheme.grayTextVariant1
                )
            }
        }
    }
}


@Composable
fun GenderSelector(
    selectedGender: String,
    isGenderSelectEnabled: Boolean = true,
    errorMessage: String? = null,
    onGenderSelected: (String) -> Unit
) {
    val genders = listOf("Male", "Female", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            genders.forEach { gender ->
                val isSelected = gender == selectedGender
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(enabled = isGenderSelectEnabled) { onGenderSelected(gender) }
                ) {
                    Text(
                        text = gender,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        errorMessage?.let {
            ErrorText(it)
        }

    }
}


@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    readOnly: Boolean = false,
    singLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singLine) 1 else Int.MAX_VALUE,
    trailingIcon: ImageVector? = null,
    onFocus: (Boolean) -> Unit = {},
    errorMessage: String? = null
) {

    val focusRequester = remember { FocusRequester() }

    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singLine,
            readOnly = readOnly,
            maxLines = maxLines,
            minLines = minLines,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        1.dp,
                        if (errorMessage != null) MaterialTheme.colorScheme.error else Color.LightGray
                    ), RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .defaultMinSize(minHeight = 28.dp)
                .onFocusChanged { focusState ->
                    onFocus(focusState.isFocused)
                }
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->

                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                style = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    fontSize = 14.sp
                                )
                            )
                        }
                        innerTextField()
                    }


                    trailingIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = "Search",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                }
            }
        )
        errorMessage?.let {
            ErrorText(errorMessage)
        }
    }
}

