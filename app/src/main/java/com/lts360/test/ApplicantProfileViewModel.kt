package com.lts360.test

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.app.ApplicantProfileApiService
import com.lts360.api.auth.AuthClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class JobLanguageOption(
    val name: String,
    val code: String
)

data class JobProfessionalInfoError(
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val intro: String? = null
)


data class ApplicantProfile(
    val jobProfessionalInfo: JobProfessionalInfo,
    val completedStep: Int
)

@HiltViewModel
class ApplicantProfileViewModel @Inject constructor() : ViewModel() {

    val jobLanguages = listOf(
        JobLanguageOption("English", "en"),
        JobLanguageOption("Spanish", "es"),
        JobLanguageOption("Mandarin Chinese", "zh"),
        JobLanguageOption("Hindi", "hi"),
        JobLanguageOption("Arabic", "ar"),
        JobLanguageOption("Bengali", "bn"),
        JobLanguageOption("Portuguese", "pt"),
        JobLanguageOption("Russian", "ru"),
        JobLanguageOption("Japanese", "ja"),
        JobLanguageOption("Punjabi", "pa"),
        JobLanguageOption("German", "de"),
        JobLanguageOption("Javanese", "jv"),
        JobLanguageOption("Korean", "ko"),
        JobLanguageOption("French", "fr"),
        JobLanguageOption("Turkish", "tr"),
        JobLanguageOption("Vietnamese", "vi"),
        JobLanguageOption("Italian", "it"),
        JobLanguageOption("Marathi", "mr"),
        JobLanguageOption("Urdu", "ur"),
        JobLanguageOption("Telugu", "te"),
        JobLanguageOption("Tamil", "ta"),
        JobLanguageOption("Gujarati", "gu"),
        JobLanguageOption("Polish", "pl"),
        JobLanguageOption("Ukrainian", "uk"),
        JobLanguageOption("Malayalam", "ml"),
        JobLanguageOption("Kannada", "kn"),
        JobLanguageOption("Oriya (Odia)", "or"),
        JobLanguageOption("Thai", "th"),
        JobLanguageOption("Dutch", "nl"),
        JobLanguageOption("Greek", "el"),
        JobLanguageOption("Swedish", "sv"),
        JobLanguageOption("Romanian", "ro"),
        JobLanguageOption("Hungarian", "hu"),
        JobLanguageOption("Czech", "cs"),
        JobLanguageOption("Hebrew", "he"),
        JobLanguageOption("Persian (Farsi)", "fa"),
        JobLanguageOption("Malay", "ms"),
        JobLanguageOption("Burmese", "my"),
        JobLanguageOption("Amharic", "am"),
        JobLanguageOption("Serbian", "sr"),
        JobLanguageOption("Finnish", "fi"),
        JobLanguageOption("Norwegian", "no"),
        JobLanguageOption("Slovak", "sk"),
        JobLanguageOption("Croatian", "hr"),
        JobLanguageOption("Zulu", "zu"),
        JobLanguageOption("Xhosa", "xh"),
        JobLanguageOption("Afrikaans", "af"),
        JobLanguageOption("Swahili", "sw"),
        JobLanguageOption("Nepali", "ne"),
        JobLanguageOption("Sinhala", "si")
    )

    companion object {
        const val MAX_STEP = 7
    }

    private val _step = MutableStateFlow(0)
    val step = _step.asStateFlow()

    private val _completedStep = MutableStateFlow(0)
    val completedStep = _completedStep.asStateFlow()

    private val _isImagePickerLauncherLaunched = MutableStateFlow(false)
    val isImagePickerLauncherLaunched = _isImagePickerLauncherLaunched.asStateFlow()

    private val _isCameraPickerLauncherLaunched = MutableStateFlow(false)
    val isCameraPickerLauncherLaunched = _isCameraPickerLauncherLaunched.asStateFlow()

    private val _cameraPickerUri = MutableStateFlow<Uri?>(null)
    val cameraPickerUri = _cameraPickerUri.asStateFlow()


    private val _jobProfessionalInfo = MutableStateFlow(JobProfessionalInfo())
    val jobProfessionalInfo = _jobProfessionalInfo.asStateFlow()

    private val _jobProfessionalInfoError = MutableStateFlow(JobProfessionalInfoError())
    val jobProfessionalInfoError = _jobProfessionalInfoError.asStateFlow()

    private val _educationList = MutableStateFlow(listOf(EducationEntry()))
    val educationList = _educationList.asStateFlow()

    private val _experienceList = MutableStateFlow(listOf(ExperienceEntry()))
    val experienceList = _experienceList.asStateFlow()

    private val _hasNoExperience = MutableStateFlow(false)
    val hasNoExperience = _hasNoExperience.asStateFlow()

    private val _selectedSkills = MutableStateFlow<List<JobSkill>>(emptyList())
    val selectedSkills = _selectedSkills.asStateFlow()

    private val _certificates = MutableStateFlow(listOf(JobCertificateInfo()))
    val certificates = _certificates.asStateFlow()

    private val _userPrefsLanguages = MutableStateFlow(listOf(JobProfileLanguage()))
    val userPrefsLanguages = _userPrefsLanguages.asStateFlow()

    private val _fileName = MutableStateFlow("My_Resume.pdf")
    val fileName = _fileName.asStateFlow()

    private val _fileSizeInBytes = MutableStateFlow<Long>(1024 * 1024 * 2) // 2MB
    val fileSizeInBytes = _fileSizeInBytes.asStateFlow()

    private val _lastModified = MutableStateFlow(System.currentTimeMillis())
    val lastModified = _lastModified.asStateFlow()

    private val _applicantProfile = MutableStateFlow<ApplicantProfile?>(null)
    val applicantProfile = _applicantProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()


    init {
        viewModelScope.launch {
            delay(5000)
            val applicantProfile = ApplicantProfile(
                jobProfessionalInfo = JobProfessionalInfo(
                    profilePic = "https://media.istockphoto.com/id/1437816897/photo/business-woman-manager-or-human-resources-portrait-for-career-success-company-we-are-hiring.jpg?s=612x612&w=0&k=20&c=tyLvtzutRh22j9GqSGI33Z4HpIwv9vL_MZw_xOE19NQ=",
                    firstName = "John",
                    lastName = "Doe",
                    gender = "Male",
                    email = "john.doe@example.com",
                    intro = "Passionate developer with 5+ years of experience."
                ),
                completedStep = 0
            )
            _applicantProfile.value = applicantProfile

            val completedStep = (applicantProfile.completedStep).coerceAtMost(MAX_STEP)
            _completedStep.value = completedStep
            _step.value = (completedStep + 1).coerceAtMost(MAX_STEP)
            _isLoading.value = false
        }
    }


    fun onFirstNameChange(newValue: String) {
        _jobProfessionalInfo.value = _jobProfessionalInfo.value.copy(firstName = newValue)
    }

    fun onLastNameChange(newValue: String) {
        _jobProfessionalInfo.value = _jobProfessionalInfo.value.copy(lastName = newValue)
    }

    fun onGenderChange(newValue: String) {
        _jobProfessionalInfo.value = _jobProfessionalInfo.value.copy(gender = newValue)
    }

    fun onEmailChange(newValue: String) {
        _jobProfessionalInfo.value = _jobProfessionalInfo.value.copy(email = newValue)
    }

    fun onIntroChange(newValue: String) {
        _jobProfessionalInfo.value = _jobProfessionalInfo.value.copy(intro = newValue)
    }


    // Functions to update states
    fun onUpdateProfilePic(uri: Uri) {
        _jobProfessionalInfo.value = _jobProfessionalInfo.value.copy(profilePic = uri.path)
    }

    fun onUpdateImagePickerLaunched(launched: Boolean) {
        _isImagePickerLauncherLaunched.value = launched
    }

    fun onUpdateCameraPickerLaunched(launched: Boolean) {
        _isCameraPickerLauncherLaunched.value = launched
    }

    fun onUpdateCameraUri(uri: Uri) {
        _cameraPickerUri.value = uri
    }


    fun validatePersonalInfoForm(): Boolean {
        val info = _jobProfessionalInfo.value
        var isValid = true

        val updatedErrors = JobProfessionalInfoError(
            firstName = if (info.firstName.isBlank()) {
                isValid = false
                "First name is required"
            } else null,

            lastName = if (info.lastName.isBlank()) {
                isValid = false
                "Last name is required"
            } else null,

            gender = if (info.gender.isBlank() || info.gender !in listOf("Male","Female", "Others")) {
                isValid = false
                "Please select a gender"
            } else null,

            email = if (info.email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(info.email)
                    .matches()
            ) {
                isValid = false
                "Invalid email address"
            } else null,

            intro = if (info.intro.length < 10) {
                isValid = false
                "Intro must be at least 10 characters"
            } else null
        )

        _jobProfessionalInfoError.value = updatedErrors
        return isValid
    }

    fun onUpdatePersonalInfoForm(
        jobProfessionalInfo: JobProfessionalInfo,
        onSuccess: () -> Unit = {},
        onError: (message:String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updatePersonalInfoForm(jobProfessionalInfo)) {
                    is Result.Success  -> {

                        val data = Json.decodeFromString<JobProfessionalInfo>(result.data.data)

                        _jobProfessionalInfo.value = data
                        onSuccess()
                    }
                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            }finally {
                _isUpdating.value = false
            }
        }
    }

    private suspend fun updatePersonalInfoForm(jobProfessionalInfo: JobProfessionalInfo): Result<ResponseReply> {

        return try {
            val updateBody =  Json.encodeToString(jobProfessionalInfo).toRequestBody("application/json".toMediaType())

            val response = AuthClient.instance.create(ApplicantProfileApiService::class.java)
                .updateJobProfessionalInfo(updateBody)

            if (response.isSuccessful) {
                // Handle successful response
                val body = response.body()

                if (body != null && body.isSuccessful) {
                    Result.Success(body)

                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }


    fun updateEducationList(updatedList: List<EducationEntry>) {
        _educationList.value = updatedList
    }

    fun updateExperienceList(updatedList: List<ExperienceEntry>) {
        _experienceList.value = updatedList
    }

    fun setHasNoExperience(value: Boolean) {
        _hasNoExperience.value = value
    }

    fun resetExperienceForm() {
        _hasNoExperience.value = false
        _experienceList.value = listOf(ExperienceEntry())
    }

    fun updateSelectedSkills(skills: List<JobSkill>) {
        _selectedSkills.value = skills
    }

    fun removeSkill(skill: JobSkill) {
        _selectedSkills.value = _selectedSkills.value - skill
    }

    fun clearSkills() {
        _selectedSkills.value = emptyList()
    }

    fun nextStep() {
        _step.value += 1
    }

    fun previousStep() {
        _step.value -= 1
    }

    fun updateCertificate(index: Int, updatedCertificate: JobCertificateInfo) {
        _certificates.value = _certificates.value.toMutableList().apply {
            this[index] = updatedCertificate
        }
    }

    fun addCertificate() {
        _certificates.value = _certificates.value + JobCertificateInfo()
    }

    fun removeCertificate(index: Int) {
        _certificates.value = _certificates.value.toMutableList().apply {
            removeAt(index)
        }
    }

    fun updateLanguage(index: Int, language: JobLanguageOption) {
        _userPrefsLanguages.value = _userPrefsLanguages.value.toMutableList().apply {
            this[index] = this[index].copy(language = language)
        }
    }

    fun updateProficiency(index: Int, proficiency: String) {
        _userPrefsLanguages.value = _userPrefsLanguages.value.toMutableList().apply {
            this[index] = this[index].copy(proficiency = proficiency)
        }
    }

    fun addLanguage() {
        _userPrefsLanguages.value = _userPrefsLanguages.value + JobProfileLanguage()
    }

    fun removeLanguage(index: Int) {
        _userPrefsLanguages.value = _userPrefsLanguages.value.toMutableList().apply {
            removeAt(index)
        }
    }

    // Update file details
    fun updateFile(fileName: String, fileSizeInBytes: Long, lastModified: Long) {
        _fileName.value = fileName
        _fileSizeInBytes.value = fileSizeInBytes
        _lastModified.value = lastModified
    }

    // Remove file details
    fun removeFile() {
        _fileName.value = ""
        _fileSizeInBytes.value = 0
        _lastModified.value = 0
    }

}
