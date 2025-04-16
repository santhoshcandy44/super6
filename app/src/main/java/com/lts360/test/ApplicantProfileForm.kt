package com.lts360.test

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.lts360.BuildConfig
import com.lts360.R
import com.lts360.api.utils.ResultError
import com.lts360.components.utils.getFileNameForUri
import com.lts360.components.utils.getFileSizeForUri
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantProfileForm(viewModel: ApplicantProfileViewModel = hiltViewModel()) {

    val step by viewModel.step.collectAsState()
    val completedStep by viewModel.completedStep.collectAsState()

    val applicantProfile by viewModel.applicantProfile.collectAsState()

    val professionalInfo by viewModel.professionalInfo.collectAsState()
    val professionalInfoError by viewModel.professionalInfoError.collectAsState()

    val isImagePickerLaunched by viewModel.isImagePickerLauncherLaunched.collectAsState()
    val isCameraPickerLaunched by viewModel.isCameraPickerLauncherLaunched.collectAsState()
    val cameraPickerUri by viewModel.cameraPickerUri.collectAsState()

    val educationList by viewModel.educationList.collectAsState()

    val experienceList by viewModel.experienceList.collectAsState()
    val hasNoExperience by viewModel.hasNoExperience.collectAsState()

    val selectedSkills by viewModel.selectedSkills.collectAsState()
    val skillInfoError by viewModel.skillInfoError.collectAsState()

    val certificates by viewModel.certificates.collectAsState()

    val userPrefsLanguages by viewModel.userPrefsLanguages.collectAsState()

    val applicantResumeDocument by viewModel.applicantResumeDocument.collectAsState()

    val applicantLanguages = viewModel.languages

    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val resumeError by viewModel.resumeError.collectAsState()

    val error by viewModel.error.collectAsState()

    val context = LocalContext.current

    val mimeTypes = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    val resumePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val contentResolver = context.contentResolver

                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val fileName: String? = getFileNameForUri(context, it)
                val fileSizeInBytes: Long = getFileSizeForUri(context, it)
                val fileExtension = fileName?.substringAfterLast('.', "")?.lowercase()

                if (fileName != null && fileSizeInBytes > 0 && fileExtension != null &&
                    fileExtension in listOf("pdf", "doc", "docx")
                ) {
                    viewModel.updateFile(
                        ApplicantResumeDocument(
                            fileName,
                            fileSizeInBytes,
                            fileExtension.uppercase(),
                            it.toString()
                        )
                    )
                } else {
                    ShortToast(context, "Failed to upload resume. Invalid file type or size.")
                }
            }
        }
    )

    var updateCertificateIndex by remember { mutableIntStateOf(-1) }

    val certificatePickLauncher = rememberLauncherForActivityResult(
        contract = GalleryPagerActivityResultContracts.PickSingleImage(),
        onResult = { uri: Uri? ->
            uri?.let {
                val contentResolver = context.contentResolver

                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                }

                val fileName: String? = getFileNameForUri(context, it)
                val fileSizeInBytes: Long = getFileSizeForUri(context, it)
                val fileExtension = fileName?.substringAfterLast('.', "")?.lowercase()

                if (fileName != null &&
                    fileSizeInBytes > 0 &&
                    fileExtension != null &&
                    fileExtension in listOf("jpg", "jpeg", "png")
                ) {

                    if (updateCertificateIndex in certificates.indices) {

                        val updatedCert = certificates[updateCertificateIndex].copy(
                            image = it.toString(),
                            fileName = fileName,
                            fileSize = fileSizeInBytes,
                            type = fileExtension.uppercase()
                        )

                        viewModel.updateCertificate(updateCertificateIndex, updatedCert)
                    }
                } else {
                    ShortToast(context, "Failed to upload certificate. Invalid image type or size.")
                }
            }
        }
    )

    val content by viewModel.content.collectAsState()

    val bottomSheetScaffold = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    )
    val scope = rememberCoroutineScope()

    BackHandler(bottomSheetScaffold.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            bottomSheetScaffold.bottomSheetState.hide()
        }
    }


    val onRefresh: () -> Unit = {
        viewModel.isRefreshing(true)
        viewModel.onGetApplicantProfile(
            userId = viewModel.userId,
            onSuccess = {
                viewModel.isRefreshing(false)
            }
        ) {
            viewModel.isRefreshing(false)
            it?.let {
                ShortToast(context, it)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            sheetShape = RectangleShape,
            sheetDragHandle = null,
            sheetContent = {

                if (bottomSheetScaffold.bottomSheetState.isVisible) {

                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton({
                                scope.launch {
                                    bottomSheetScaffold.bottomSheetState.hide()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            }
                        })

                    Box(modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)) {
                        when (content) {
                            "edit_profile" -> {
                                PersonalInfoForm(
                                    isImagePickerLauncherLaunched = isImagePickerLaunched,
                                    isCameraPickerLauncherLaunched = isCameraPickerLaunched,
                                    cameraPickerUri = cameraPickerUri,
                                    professionalInfo = professionalInfo,
                                    professionalInfoError = professionalInfoError,
                                    onFirstNameChange = viewModel::onFirstNameChange,
                                    onLastNameChange = viewModel::onLastNameChange,
                                    onGenderChange = viewModel::onGenderChange,
                                    onEmailChange = viewModel::onEmailChange,
                                    onIntroChange = viewModel::onIntroChange,
                                    onUpdateProfilePic = viewModel::onUpdateProfilePic,
                                    onUpdateImagePickerLaunched = viewModel::onUpdateImagePickerLaunched,
                                    onUpdateCameraPickerLaunched = viewModel::onUpdateCameraPickerLaunched,
                                    onUpdateCameraUri = viewModel::onUpdateCameraUri,
                                    isEditScreen = true,
                                    onSaveClicked = {
                                        if (viewModel.validatePersonalInfoForm()) {
                                            viewModel.onUpdatePersonalInfoForm(
                                                professionalInfo = professionalInfo,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            "edit_education" -> {
                                EducationForm(
                                    educationList = educationList,
                                    onEducationListChange = viewModel::updateEducationList,
                                    isEditScreen = true,
                                    onSaveClicked = {
                                        if (viewModel.validateEducationInfoForm()) {
                                            viewModel.onUpdateEducationInfoForm(
                                                applicantEducationEntries = educationList,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            "edit_experience" -> {
                                ExperienceForm(
                                    experienceList = experienceList,
                                    hasNoExperience = hasNoExperience,
                                    onExperienceListChange = viewModel::updateExperienceList,
                                    onHasNoExperienceChange = viewModel::setHasNoExperience,
                                    isEditScreen = true,
                                    onSaveClicked = {

                                        if (hasNoExperience) {
                                            viewModel.onUpdateNoExperienceInfoForm(
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        } else {
                                            if (viewModel.validateExperienceInfoForm()) {
                                                viewModel.onUpdateExperienceInfoForm(
                                                    applicantExperienceEntries = experienceList,
                                                    onSuccess = {
                                                        ShortToast(context, it)
                                                    },
                                                    onError = { errorMessage ->
                                                        errorMessage?.let {
                                                            ShortToast(context, it)
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                    }
                                )
                            }

                            "edit_skill" -> {
                                SkillsForm(
                                    selectedSkills = selectedSkills,
                                    onSkillsChanged = viewModel::updateSelectedSkills,
                                    onSkillRemoved = viewModel::removeSkill,
                                    skillSError = skillInfoError,
                                    isEditScreen = true,
                                    onSaveClicked = {
                                        if (viewModel.validateSkillInfoForm()) {

                                            viewModel.onUpdateSkillInfoForm(
                                                applicantSkillsEntries = selectedSkills,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            "edit_certificate" -> {
                                CertificationsForm(
                                    certificates = certificates,
                                    onCertificateUpdated = { index, updatedCertificate ->
                                        viewModel.updateCertificate(index, updatedCertificate)
                                    },
                                    onCertificateRemoved = { index ->
                                        viewModel.removeCertificate(index)
                                    },
                                    onAddCertificate = {
                                        viewModel.addCertificate()
                                    },
                                    onPickCertificateImage = {
                                        updateCertificateIndex = it // before launching picker
                                        certificatePickLauncher.launch(Unit)
                                    },
                                    isEditScreen = true,
                                    onSaveClicked = {
                                        if (viewModel.validateCertificateInfoForm()) {

                                            viewModel.onUpdateCertificateInfoForm(
                                                applicantCertificateInfoList = certificates,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            "edit_language" -> {
                                LanguagesForm(
                                    languageOptions = applicantLanguages,
                                    userPrefsLanguages = userPrefsLanguages,
                                    onAddLanguage = { viewModel.addLanguage() },
                                    onRemoveLanguage = {
                                        viewModel.removeLanguage(
                                            userPrefsLanguages.size - 1
                                        )
                                    },
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
                                    },
                                    isEditScreen = true,
                                    onSaveClicked = {
                                        if (viewModel.validateLanguageInfoForm()) {

                                            viewModel.onUpdateLanguageInfoForm(
                                                applicantLanguageEntries = userPrefsLanguages,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            "edit_resume" -> {

                                ResumeUploadSection(
                                    applicantResumeDocument = applicantResumeDocument,
                                    onUploadClicked = {
                                        resumePickLauncher.launch(mimeTypes)
                                    },
                                    onRemoveClicked = {
                                        viewModel.removeFile()
                                    },
                                    resumeError = resumeError,
                                    isEditScreen = true,
                                    onSaveClicked = {
                                        if (viewModel.validateResumeInfoForm()) {
                                            applicantResumeDocument?.let { nonNullApplicantResumeDocument ->
                                                if (nonNullApplicantResumeDocument.resume.startsWith(
                                                        "content://"
                                                    )
                                                ) {
                                                    viewModel.onUpdateResumeInfoForm(
                                                        applicantResumeDocument = nonNullApplicantResumeDocument,
                                                        onSuccess = {
                                                            ShortToast(context, it)
                                                        },
                                                        onError = { errorMessage ->
                                                            errorMessage?.let {
                                                                ShortToast(context, it)
                                                            }
                                                        }
                                                    )
                                                }

                                            } ?: run {
                                                ShortToast(context, "Choose resume")
                                            }
                                        }
                                    },
                                )


                            }
                        }
                    }
                }
            },
            scaffoldState = bottomSheetScaffold,
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {


                if (!isLoading) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        if (step < 0 || (error != null && error !is ResultError.Unknown)) {
                            ApplicantProfileScreen(
                                applicantProfile = applicantProfile,
                                error = error,
                                onEditProfile = {
                                    viewModel.updateContent("edit_profile")
                                    scope.launch {
                                        bottomSheetScaffold.bottomSheetState.expand()
                                    }
                                }, onEditEducation = {
                                    viewModel.updateContent("edit_education")
                                    scope.launch {
                                        bottomSheetScaffold.bottomSheetState.expand()
                                    }
                                }, onEditExperience = {
                                    viewModel.updateContent("edit_experience")
                                    scope.launch {
                                        bottomSheetScaffold.bottomSheetState.expand()
                                    }
                                }, onEditSkill = {
                                    viewModel.updateContent("edit_skill")
                                    scope.launch {
                                        bottomSheetScaffold.bottomSheetState.expand()
                                    }
                                }, onEditCertificate = {
                                    viewModel.updateContent("edit_certificate")
                                    scope.launch {
                                        bottomSheetScaffold.bottomSheetState.expand()
                                    }
                                }, onEditLanguage = {
                                    viewModel.updateContent("edit_language")
                                    scope.launch {
                                        bottomSheetScaffold.bottomSheetState.expand()
                                    }
                                }, onEditResume = {
                                    viewModel.updateContent("edit_resume")
                                    scope.launch {
                                        bottomSheetScaffold.bottomSheetState.expand()
                                    }
                                }, isRefreshing = isRefreshing,
                                onRefresh = onRefresh
                            )
                        } else {


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
                                    0 -> PersonalInfoForm(
                                        isImagePickerLauncherLaunched = isImagePickerLaunched,
                                        isCameraPickerLauncherLaunched = isCameraPickerLaunched,
                                        cameraPickerUri = cameraPickerUri,
                                        professionalInfo = professionalInfo,
                                        professionalInfoError = professionalInfoError,
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

                                    1 -> EducationForm(
                                        educationList = educationList,
                                        onEducationListChange = viewModel::updateEducationList
                                    )

                                    2 -> ExperienceForm(
                                        experienceList = experienceList,
                                        hasNoExperience = hasNoExperience,
                                        onExperienceListChange = viewModel::updateExperienceList,
                                        onHasNoExperienceChange = viewModel::setHasNoExperience
                                    )

                                    3 -> SkillsForm(
                                        selectedSkills = selectedSkills,
                                        onSkillsChanged = viewModel::updateSelectedSkills,
                                        onSkillRemoved = viewModel::removeSkill,
                                        skillSError = skillInfoError
                                    )


                                    4 -> LanguagesForm(
                                        languageOptions = applicantLanguages,
                                        userPrefsLanguages = userPrefsLanguages,
                                        onAddLanguage = { viewModel.addLanguage() },
                                        onRemoveLanguage = {
                                            viewModel.removeLanguage(
                                                userPrefsLanguages.size - 1
                                            )
                                        },
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

                                    5 -> ResumeUploadSection(
                                        applicantResumeDocument = applicantResumeDocument,
                                        onUploadClicked = {
                                            resumePickLauncher.launch(mimeTypes)
                                        },
                                        resumeError = resumeError,
                                        onRemoveClicked = {
                                            viewModel.removeFile()
                                        }
                                    )


                                    6 -> CertificationsForm(
                                        certificates = certificates,
                                        onCertificateUpdated = { index, updatedCertificate ->
                                            viewModel.updateCertificate(
                                                index,
                                                updatedCertificate
                                            )
                                        },
                                        onCertificateRemoved = { index ->
                                            viewModel.removeCertificate(index)
                                        },
                                        onAddCertificate = {
                                            viewModel.addCertificate()
                                        },
                                        onPickCertificateImage = {
                                            updateCertificateIndex =
                                                it // before launching picker
                                            certificatePickLauncher.launch(Unit)
                                        }
                                    )
                                }
                            }

                            FormNavigationControls(
                                currentStep = step,
                                onPrevious = {
                                    viewModel.previousStep()
                                },
                                onNext = {
                                    if (step == 0) {
                                        if (viewModel.validatePersonalInfoForm()) {

                                            viewModel.onUpdatePersonalInfoForm(
                                                professionalInfo = professionalInfo,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (step == 1) {
                                        if (viewModel.validateEducationInfoForm()) {

                                            viewModel.onUpdateEducationInfoForm(
                                                applicantEducationEntries = educationList,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    } else if (step == 2) {
                                        if (hasNoExperience) {
                                            viewModel.onUpdateNoExperienceInfoForm(
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        } else {
                                            if (viewModel.validateExperienceInfoForm()) {
                                                viewModel.onUpdateExperienceInfoForm(
                                                    applicantExperienceEntries = experienceList,
                                                    onSuccess = {
                                                        ShortToast(context, it)
                                                    },
                                                    onError = { errorMessage ->
                                                        errorMessage?.let {
                                                            ShortToast(context, it)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    } else if (step == 3) {
                                        if (viewModel.validateSkillInfoForm()) {

                                            viewModel.onUpdateSkillInfoForm(
                                                applicantSkillsEntries = selectedSkills,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    } else if (step == 4) {
                                        if (viewModel.validateLanguageInfoForm()) {

                                            viewModel.onUpdateLanguageInfoForm(
                                                applicantLanguageEntries = userPrefsLanguages,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    } else if (step == 5) {
                                        if (viewModel.validateResumeInfoForm()) {
                                            applicantResumeDocument?.let { nonNullApplicantResumeDocument ->

                                                if (nonNullApplicantResumeDocument.resume.startsWith(
                                                        "content://"
                                                    )
                                                ) {
                                                    viewModel.onUpdateResumeInfoForm(
                                                        applicantResumeDocument = nonNullApplicantResumeDocument,
                                                        onSuccess = {
                                                            ShortToast(context, it)
                                                        },
                                                        onError = { errorMessage ->
                                                            errorMessage?.let {
                                                                ShortToast(context, it)
                                                            }
                                                        }
                                                    )
                                                } else {
                                                    viewModel.nextStep()
                                                }

                                            } ?: run {
                                                ShortToast(context, "Choose resume")
                                            }
                                        }
                                    } else if (step == 6) {
                                        if (viewModel.validateCertificateInfoForm()) {

                                            viewModel.onUpdateCertificateInfoForm(
                                                applicantCertificateInfoList = certificates,
                                                onSuccess = {
                                                    ShortToast(context, it)
                                                },
                                                onError = { errorMessage ->
                                                    errorMessage?.let {
                                                        ShortToast(context, it)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                },
                                onSkip = {
                                    if (step == 6) {
                                        viewModel.fetchApplicantProfile()
                                    }
                                },
                                isUpdating
                            )
                        }


                    }


                } else {
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
private fun FormNavigationControls(
    currentStep: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit = {},
    isLoading: Boolean = false
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

        Row(
            modifier = Modifier.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep == 6) {
                Text("Skip", modifier = Modifier.clickable {
                    onSkip()
                })
                Spacer(Modifier.width(8.dp))
            }
            CircleButton(
                onClick = onNext,
                isLoading = isLoading,
                label = if (currentStep == 6) "Complete" else "Save & Continue",
                icon = if (currentStep < 7) Icons.AutoMirrored.Filled.ArrowForward else null
            )
        }
    }
}


@Composable
private fun CircleButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(enabled = !isLoading) { onClick() },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(text = label)

            if (isLoading) {
                CircularProgressIndicatorLegacy(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            }
            icon?.let {
                Icon(
                    icon,
                    contentDescription = null
                )
            }
        }
    }
}


@Composable
private fun PersonalInfoForm(
    isImagePickerLauncherLaunched: Boolean,
    isCameraPickerLauncherLaunched: Boolean,
    cameraPickerUri: Uri?,
    professionalInfo: ApplicantProfessionalInfo,
    professionalInfoError: ApplicantProfessionalInfoError,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onIntroChange: (String) -> Unit,
    onUpdateProfilePic: (Uri) -> Unit,
    onUpdateImagePickerLaunched: (Boolean) -> Unit,
    onUpdateCameraPickerLaunched: (Boolean) -> Unit,
    onUpdateCameraUri: (Uri) -> Unit,
    isEditScreen: Boolean = false,
    onSaveClicked: () -> Unit = {}
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

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(text = "Personal Information", style = MaterialTheme.typography.headlineSmall)
            }

            item {
                Box(
                    modifier = Modifier
                        .size(100.dp),
                    contentAlignment = Alignment.Center

                ) {

                    professionalInfo.profilePic?.let {
                        AsyncImage(
                            if (it.startsWith("content://")) it.toUri() else it,
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
                    value = professionalInfo.firstName,
                    onValueChange = onFirstNameChange,
                    label = "First Name",
                    errorMessage = professionalInfoError.firstName
                )
            }

            item {
                CustomTextField(
                    value = professionalInfo.lastName,
                    onValueChange = onLastNameChange,
                    label = "Last Name",
                    errorMessage = professionalInfoError.lastName
                )
            }

            item {
                GenderSelector(
                    selectedGender = professionalInfo.gender,
                    onGenderSelected = onGenderChange,
                    errorMessage = professionalInfoError.gender
                )
            }

            item {
                CustomTextField(
                    value = professionalInfo.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    errorMessage = professionalInfoError.email
                )
            }

            item {
                CustomTextField(
                    value = professionalInfo.intro,
                    onValueChange = onIntroChange,
                    label = "Intro",
                    minLines = 5,
                    errorMessage = professionalInfoError.intro
                )
            }
        }

        if (isEditScreen) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                CircleButton(
                    onClick = onSaveClicked,
                    label = "Save",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EducationForm(
    educationList: List<ApplicantEducationEntry>,
    onEducationListChange: (List<ApplicantEducationEntry>) -> Unit,
    isEditScreen: Boolean = false,
    onSaveClicked: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedField by remember { mutableStateOf<Pair<Int, String>?>(null) }
    val datePickerState = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = {
                selectedField = null
                showDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    millis?.let {
                        selectedField?.let { (index, type) ->
                            onEducationListChange(
                                educationList.toMutableList().apply {
                                    this[index] = if (type == "start") {
                                        this[index].copy(startYear = it)
                                    } else {
                                        this[index].copy(endYear = it)
                                    }
                                }
                            )
                        }
                    }
                    selectedField = null
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedField = null
                    showDialog = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
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
                            onEducationListChange(educationList.toMutableList().apply {
                                this[index] = this[index].copy(institution = it)
                            })
                        },
                        label = "Institution/School",
                        errorMessage = entry.error?.institution
                    )

                    CustomTextField(
                        value = entry.fieldOfStudy,
                        onValueChange = {
                            onEducationListChange(educationList.toMutableList().apply {
                                this[index] = this[index].copy(fieldOfStudy = it)
                            })
                        },
                        label = "Field of Study",
                        errorMessage = entry.error?.fieldOfStudy
                    )

                    CustomTextField(
                        value = if (entry.startYear != 0L) {
                            SimpleDateFormat(
                                "dd-MM-yyyy",
                                Locale.getDefault()
                            ).format(Date(entry.startYear))
                        } else {
                            ""
                        },
                        label = "Start Year",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = Icons.Default.Edit,
                        onTrailingIconClick = {
                            selectedField = index to "start"
                            showDialog = true
                        },
                        errorMessage = entry.error?.startYear
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            Checkbox(
                                checked = entry.currentlyStudying,
                                onCheckedChange = {
                                    onEducationListChange(
                                        educationList.toMutableList().apply {
                                            this[index] =
                                                this[index].copy(
                                                    currentlyStudying = it,
                                                    endYear = 0,
                                                    grade = 0.0
                                                )
                                        }
                                    )
                                }
                            )
                        }
                        Text("Currently studying at")

                        entry.error?.currentlyStudying?.let {
                            ErrorText(it)
                        }
                    }

                    if (!entry.currentlyStudying) {
                        CustomTextField(
                            label = "End Year",
                            value = if (entry.endYear != 0L) {
                                SimpleDateFormat(
                                    "dd-MM-yyyy",
                                    Locale.getDefault()
                                ).format(Date(entry.endYear))
                            } else {
                                ""
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = Icons.Default.Edit,
                            onTrailingIconClick = {
                                selectedField = index to "end"
                                showDialog = true
                            },
                            errorMessage = entry.error?.endYear
                        )

                        var gradeInput by remember {
                            mutableStateOf(
                                if (entry.grade > 0.0) {
                                    if (entry.grade == entry.grade.toInt().toDouble())
                                        entry.grade.toInt().toString()
                                    else
                                        entry.grade.toString()
                                } else ""
                            )
                        }

                        CustomTextField(
                            value = gradeInput,
                            onValueChange = {
                                if (it.matches(Regex("^\\d*(\\.\\d{0,1})?$"))) {
                                    gradeInput = it

                                    val parsedValue = it.toDoubleOrNull() ?: 0.0
                                    onEducationListChange(educationList.toMutableList().apply {
                                        this[index] = this[index].copy(grade = parsedValue)
                                    })
                                }
                            },
                            label = "Grade/Percentage/CGPA",
                            keyBoardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal
                            ),
                            errorMessage = entry.error?.grade
                        )
                    }
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
                        RectangleButton(icon = Icons.Default.Add, label = "Add School/Institute") {
                            onEducationListChange(educationList + ApplicantEducationEntry())
                        }
                    }
                }
            }
        }

        if (isEditScreen) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                CircleButton(
                    onClick = onSaveClicked,
                    label = "Save",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperienceForm(
    experienceList: List<ApplicantExperienceEntry>,
    hasNoExperience: Boolean,
    onExperienceListChange: (List<ApplicantExperienceEntry>) -> Unit,
    onHasNoExperienceChange: (Boolean) -> Unit,
    isEditScreen: Boolean = false,
    onSaveClicked: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedField by remember { mutableStateOf<Pair<Int, String>?>(null) }
    val datePickerState = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = {
                selectedField = null
                showDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    millis?.let {
                        selectedField?.let { (index, type) ->
                            onExperienceListChange(
                                experienceList.toMutableList().apply {
                                    this[index] = if (type == "start") {
                                        this[index].copy(startDate = it)
                                    } else {
                                        this[index].copy(endDate = it)
                                    }
                                }
                            )
                        }
                    }
                    selectedField = null
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedField = null
                    showDialog = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
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
                    CustomTextField(
                        value = entry.jobTitle,
                        onValueChange = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(jobTitle = it)
                            })
                        },
                        label = "Job Title"
                    )
                    entry.error?.jobTitle?.let {
                        ErrorText(it)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    EmploymentTypeDropdown(
                        selectedType = entry.employmentType,
                        onSelected = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(employmentType = it)
                            })
                        }
                    )
                    entry.error?.employmentType?.let {
                        ErrorText(it)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    CustomTextField(
                        value = entry.companyName,
                        onValueChange = {
                            onExperienceListChange(experienceList.toMutableList().apply {
                                this[index] = this[index].copy(companyName = it)
                            })
                        },
                        label = "Company or Organization"
                    )

                    entry.error?.companyName?.let {
                        ErrorText(it)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

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

                        Spacer(modifier = Modifier.height(16.dp))

                    }

                    CustomTextField(
                        value = if (entry.startDate != 0L) {
                            SimpleDateFormat(
                                "dd-MM-yyyy",
                                Locale.getDefault()
                            ).format(Date(entry.startDate))
                        } else {
                            ""
                        },
                        onValueChange = {},
                        label = "Start Date (e.g., Jan 2020)",
                        readOnly = true,
                        trailingIcon = Icons.Default.Edit,
                        onTrailingIconClick = {
                            selectedField = index to "start"
                            showDialog = true
                        }
                    )

                    entry.error?.startDate?.let {
                        ErrorText(it)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!entry.isCurrentJob) {
                        CustomTextField(
                            value = if (entry.endDate != 0L) {
                                SimpleDateFormat(
                                    "dd-MM-yyyy",
                                    Locale.getDefault()
                                ).format(Date(entry.endDate))
                            } else {
                                ""
                            },
                            onValueChange = {},
                            label = "End Date (e.g., Dec 2023)",
                            readOnly = true,
                            trailingIcon = Icons.Default.Edit,
                            onTrailingIconClick = {
                                selectedField = index to "end"
                                showDialog = true
                            }
                        )

                        entry.error?.endDate?.let {
                            ErrorText(it)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
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

                    entry.error?.location?.let {
                        ErrorText(it)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(thickness = 1.dp)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        if (experienceList.size < 5) {
                            RectangleButton(icon = Icons.Default.Add, label = "Add") {
                                onExperienceListChange(experienceList + ApplicantExperienceEntry())
                            }
                        }
                    }
                }
            }
        }

        if (isEditScreen) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                CircleButton(
                    onClick = onSaveClicked,
                    label = "Save",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmploymentTypeDropdown(
    selectedType: String,
    onSelected: (String) -> Unit
) {
    val employmentTypes = listOf(
        "full_time" to "Full Time",
        "part_time" to "Part Time",
        "contract" to "Contract",
        "intern" to "Internship",
        "freelance" to "Freelance"
    )

    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = employmentTypes.find { it.first == selectedType }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }) {
        CustomTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = "Employment Type",
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            trailingIcon = Icons.Default.ArrowDropDown
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            employmentTypes.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    }
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillsForm(
    selectedSkills: List<ApplicantSkill>,
    onSkillsChanged: (List<ApplicantSkill>) -> Unit,
    onSkillRemoved: (ApplicantSkill) -> Unit,
    skillSError: String? = null,
    isEditScreen: Boolean = false,
    onSaveClicked: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val skillsChooserModalBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(text = "Skills", style = MaterialTheme.typography.headlineSmall)
            }

            item {
                CustomTextField(
                    value = selectedSkills.joinToString(", ") { it.skill },
                    onValueChange = {},
                    label = "Skill Name",
                    readOnly = true,
                    onFocus = {
                        if (it) {
                            scope.launch { skillsChooserModalBottomSheetState.expand() }
                        }
                    }
                )

                skillSError?.let {
                    ErrorText(it)
                }
            }

            item {
                if (selectedSkills.isNotEmpty()) {
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

        if (isEditScreen) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                CircleButton(
                    onClick = onSaveClicked,
                    label = "Save",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
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
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.safeDrawingPadding()
        ) {
            SkillsPickerScreen(
                allSkills = listOf(
                    ApplicantSkill("Kotlin", "SK001"),
                    ApplicantSkill("Java", "SK002"),
                    ApplicantSkill("Swift", "SK003"),
                    ApplicantSkill("React", "SK004"),
                    ApplicantSkill("Figma", "SK005"),
                    ApplicantSkill("Photoshop", "SK006"),
                    ApplicantSkill("Python", "SK007"),
                    ApplicantSkill("Node.js", "SK008"),
                    ApplicantSkill("Flutter", "SK009"),
                    ApplicantSkill("SQL", "SK010"),
                    ApplicantSkill("Docker", "SK011"),
                    ApplicantSkill("AWS", "SK012")
                ),
                onSkillsSelected = { skills ->
                    onSkillsChanged(skills)
                    focusManager.clearFocus()
                    scope.launch { skillsChooserModalBottomSheetState.hide() }
                },
                preSelectedSkills = selectedSkills
            )
        }
    }
}


@Composable
private fun SkillsPickerScreen(
    preSelectedSkills: List<ApplicantSkill>,
    allSkills: List<ApplicantSkill>,
    onSkillsSelected: (List<ApplicantSkill>) -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val filteredSkills = remember(searchQuery) {
        allSkills.filter { it.skill.contains(searchQuery.text, ignoreCase = true) }
    }

    var selectedSkills by remember(preSelectedSkills) { mutableStateOf(preSelectedSkills) }

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

                if (selectedSkills.isNotEmpty()) {
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
                                    skill = skill.skill,
                                    onRemove = {
                                        selectedSkills = selectedSkills - skill
                                    }
                                )
                            }
                        }
                    }
                }


                items(filteredSkills) { skill ->
                    val isSelected = selectedSkills.map { it.skillCode }.contains(skill.skillCode)
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
                        Text(skill.skill, modifier = Modifier.weight(1f))
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


@Composable
private fun CertificationsForm(
    certificates: List<ApplicantCertificateInfo>,
    onCertificateUpdated: (Int, ApplicantCertificateInfo) -> Unit,
    onCertificateRemoved: (Int) -> Unit,
    onAddCertificate: () -> Unit,
    onPickCertificateImage: (Int) -> Unit,
    isEditScreen: Boolean = false,
    onSaveClicked: () -> Unit = {}
) {

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Certificates",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            itemsIndexed(certificates) { index, certificate ->

                CustomTextField(
                    value = certificate.issuedBy,
                    onValueChange = {
                        onCertificateUpdated(index, certificate.copy(issuedBy = it))
                    },
                    label = "Issued By"
                )

                certificate.error?.issuedBy?.let { ErrorText(it) }


                Spacer(Modifier.height(16.dp))

                // Image Picker Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable {
                            onPickCertificateImage(index)
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

                certificate.error?.image?.let { ErrorText(it) }

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

        if (isEditScreen) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                CircleButton(
                    onClick = onSaveClicked,
                    label = "Save",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagesForm(
    languageOptions: List<ApplicantLanguageOption>,
    userPrefsLanguages: List<ApplicantLanguage>,
    onAddLanguage: () -> Unit,
    onRemoveLanguage: () -> Unit,
    onLanguageSelected: (Int, ApplicantLanguageOption) -> Unit,
    onProficiencySelected: (Int, ApplicantProficiencyOption) -> Unit,
    isEditScreen: Boolean = false,
    onSaveClicked: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val languageChooserModalBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val proficiencyChooserModalBottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .weight(1f),
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
                    value = item.proficiency?.name ?: "",
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

        if (isEditScreen) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                CircleButton(
                    onClick = onSaveClicked,
                    label = "Save",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
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
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.safeDrawingPadding()
        ) {
            LanguagePickerScreen(
                languageOptions,
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
            containerColor = MaterialTheme.colorScheme.surface,
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
private fun LanguagePickerScreen(
    allLanguages: List<ApplicantLanguageOption>,
    onLanguageSelected: (ApplicantLanguageOption) -> Unit
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
private fun ProficiencyPickerScreen(
    proficiencies: List<ApplicantProficiencyOption> = listOf(
        ApplicantProficiencyOption(
            "Fluent", "fluent"
        ),
        ApplicantProficiencyOption(
            "Basic", "basic"
        ),
        ApplicantProficiencyOption(
            "Intermediate", "intermediate"
        ),
    ),
    onProficiencySelected: (ApplicantProficiencyOption) -> Unit
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
                text = level.name,
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
private fun ResumeUploadSection(
    applicantResumeDocument: ApplicantResumeDocument?,
    onUploadClicked: () -> Unit,
    onRemoveClicked: () -> Unit,
    resumeError: String?,
    isEditScreen: Boolean = false,
    onSaveClicked: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
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

                    applicantResumeDocument?.let {
                        ResumeCard(
                            it,
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
                resumeError?.let {
                    ErrorText(it)
                }
            }

            // Default Resume Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "This resume will be used by default, but you can change it when applying.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.customColorScheme.grayTextVariant1
                )
            }
        }

        if (isEditScreen && applicantResumeDocument?.resume?.startsWith("content://") == true) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                CircleButton(
                    onClick = onSaveClicked,
                    label = "Save",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

    }


}

@Composable
fun ResumeCard(
    applicantResumeDocument: ApplicantResumeDocument,
    removeButtonEnabled: Boolean = true,
    onRemoveClicked: () -> Unit = {}
) {
    val isPdf = applicantResumeDocument.type == "PDF"
    val isDoc = applicantResumeDocument.type == "DOC" || applicantResumeDocument.type == "DOCX"

    val fileColor = when {
        isPdf -> Color(0xFFD32F2F) // Red for PDF
        isDoc -> Color(0xFF1976D2) // Blue for DOC/DOCX
        else -> Color.Gray
    }

    val fileIcon = Icons.Default.Description
    val fileSizeInMB = humanReadableBytesSize(applicantResumeDocument.fileSize)

    val lastUsedDate = remember(applicantResumeDocument.lastUsed) {
        applicantResumeDocument.lastUsed?.let {
            SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(it))
        }
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
                        text = applicantResumeDocument.fileName,
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

                lastUsedDate?.let {

                    Text(
                        text = "Last used: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.customColorScheme.grayTextVariant1
                    )
                }

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
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    readOnly: Boolean = false,
    singLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singLine) 1 else Int.MAX_VALUE,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: () -> Unit = {},
    onFocus: (Boolean) -> Unit = {},
    keyBoardOptions: KeyboardOptions = KeyboardOptions.Default,
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
            keyboardOptions = keyBoardOptions,
            modifier = modifier
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
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    onTrailingIconClick()
                                }
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

@Composable
fun RectangleButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.size(width = 32.dp, height = 32.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White
        )
    }
}

