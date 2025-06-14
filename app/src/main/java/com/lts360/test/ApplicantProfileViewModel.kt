package com.lts360.test

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.app.AppClient
import com.lts360.api.app.ApplicantProfileApiService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.components.utils.InputStreamRequestBody
import com.lts360.components.utils.getFileNameForUri
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Date


@Serializable
data class ApplicantProfile(
    @SerialName("applicant_professional_info")
    val applicantProfessionalInfo: ApplicantProfessionalInfo,
    @SerialName("applicant_education")
    val applicantEducations: List<ApplicantEducationEntry>,
    @SerialName("applicant_experience")
    val applicantExperiences: List<ApplicantExperienceEntry>,
    @SerialName("applicant_skill")
    val applicantSkills: List<ApplicantSkill>,
    @SerialName("applicant_language")
    val applicantLanguages: List<ApplicantLanguage>,
    @SerialName("applicant_certificate")
    val applicantCertificate: List<ApplicantCertificateInfo>,
    @SerialName("applicant_resume")
    val applicantResumeDocument: ApplicantResumeDocument?,
    @SerialName("next_complete_step")
    val nextCompleteStep: Int
)

fun ApplicantProfile.getProfileCompletion(): Float {
    var totalFields = 0
    var completedFields = 0

    totalFields += 6
    with(applicantProfessionalInfo) {
        if (firstName.isNotBlank()) completedFields++
        if (lastName.isNotBlank()) completedFields++
        if (gender.isNotBlank()) completedFields++
        if (email.isNotBlank()) completedFields++
        if (intro.isNotBlank()) completedFields++
        if (!profilePic.isNullOrBlank()) completedFields++
    }

    totalFields++
    if (applicantEducations.isNotEmpty()) completedFields++

    totalFields++
    if (applicantExperiences.isNotEmpty()) completedFields++

    totalFields++
    if (applicantSkills.isNotEmpty()) completedFields++

    totalFields++
    if (applicantLanguages.isNotEmpty()) completedFields++

    totalFields++
    if (applicantCertificate.isNotEmpty()) completedFields++

    totalFields++
    if (applicantResumeDocument != null) completedFields++


    return completedFields.toFloat() / totalFields
}

data class ApplicantProfileSuggestion(
    val title: String,
    val message: String,
    val fieldKey: String,
    val contribution: Float // from 0.0f to 1.0f
)


fun ApplicantProfile.getMissingFieldSuggestions(): List<ApplicantProfileSuggestion> {
    val suggestions = mutableListOf<ApplicantProfileSuggestion>()
    val totalFields = 12 // 6 from professional info + 6 sections
    val fieldWeight = 1f / totalFields

    with(applicantProfessionalInfo) {
        if (firstName.isBlank()) suggestions.add(
            ApplicantProfileSuggestion(
                title = "Add your first name",
                message = "Tell us who you are by filling in your first name.",
                fieldKey = "firstName",
                contribution = fieldWeight
            )
        )
        if (lastName.isBlank()) suggestions.add(
            ApplicantProfileSuggestion(
                title = "Add your last name",
                message = "Complete your full name to personalize your profile.",
                fieldKey = "lastName",
                contribution = fieldWeight
            )
        )
        if (gender.isBlank()) suggestions.add(
            ApplicantProfileSuggestion(
                title = "Select your gender",
                message = "Help recruiters know you better.",
                fieldKey = "gender",
                contribution = fieldWeight
            )
        )
        if (email.isBlank()) suggestions.add(
            ApplicantProfileSuggestion(
                title = "Add your email",
                message = "Important for job notifications and contact.",
                fieldKey = "email",
                contribution = fieldWeight
            )
        )
        if (intro.isBlank()) suggestions.add(
            ApplicantProfileSuggestion(
                title = "Write a short intro",
                message = "Let people know more about you in your own words.",
                fieldKey = "intro",
                contribution = fieldWeight
            )
        )
        if (profilePic.isNullOrBlank()) suggestions.add(
            ApplicantProfileSuggestion(
                title = "Upload a profile picture",
                message = "Profiles with pictures get more attention.",
                fieldKey = "profilePic",
                contribution = fieldWeight
            )
        )
    }

    if (applicantEducations.isEmpty()) suggestions.add(
        ApplicantProfileSuggestion(
            title = "Add your education",
            message = "Show your academic background to employers.",
            fieldKey = "education",
            contribution = fieldWeight
        )
    )

    if (applicantExperiences.isEmpty()) suggestions.add(
        ApplicantProfileSuggestion(
            title = "Add your work experience",
            message = "Highlight your career journey.",
            fieldKey = "experience",
            contribution = fieldWeight
        )
    )

    if (applicantSkills.isEmpty()) suggestions.add(
        ApplicantProfileSuggestion(
            title = "Add your skills",
            message = "Tell us what you're good at!",
            fieldKey = "skills",
            contribution = fieldWeight
        )
    )

    if (applicantLanguages.isEmpty()) suggestions.add(
        ApplicantProfileSuggestion(
            title = "Add languages you speak",
            message = "Stand out with your language proficiency.",
            fieldKey = "languages",
            contribution = fieldWeight
        )
    )

    if (applicantCertificate.isEmpty()) suggestions.add(
        ApplicantProfileSuggestion(
            title = "Add certificates",
            message = "Show your achievements and specializations.",
            fieldKey = "certificates",
            contribution = fieldWeight
        )
    )

    if (applicantResumeDocument == null) suggestions.add(
        ApplicantProfileSuggestion(
            title = "Upload your resume",
            message = "Required to apply for jobs.",
            fieldKey = "resume",
            contribution = fieldWeight
        )
    )

    return suggestions
}




@Serializable
data class ApplicantProfessionalInfo(
    @SerialName("profile_pic_url")
    val profilePic: String? = null,

    @SerialName("first_name")
    val firstName: String = "",

    @SerialName("last_name")
    val lastName: String = "",

    @SerialName("gender")
    val gender: String = "",

    @SerialName("email")
    val email: String = "",

    @SerialName("intro")
    val intro: String = ""
)

data class ApplicantProfessionalInfoError(
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val intro: String? = null
)


data class ApplicantEducationEntryError(
    val institution: String? = null,
    val fieldOfStudy: String? = null,
    val startYear: String? = null,
    val endYear: String? = null,
    val grade: String? = null,
    val currentlyStudying: String? = null
)

@Serializable
data class ApplicantEducationEntry(
    @SerialName("institution")
    val institution: String = "",

    @SerialName("field_of_study")
    val fieldOfStudy: String = "",

    @SerialName("start_year")
    val startYear: Long = 0,

    @SerialName("end_year")
    val endYear: Long = 0,

    @SerialName("grade")
    val grade: Double = 0.0,

    @SerialName("currently_studying")
    val currentlyStudying: Boolean = false,

    @Transient
    var error: ApplicantEducationEntryError? = null
)


data class ApplicantExperienceEntryError(
    val companyName: String? = null,
    val jobTitle: String? = null,
    val employmentType: String? = null,
    val location: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)


@Serializable
data class ApplicantExperienceEntry(
    @SerialName("experienced")
    var experienced: Boolean = false,
    @SerialName("company_name")
    var companyName: String = "",

    @SerialName("job_title")
    var jobTitle: String = "",

    @SerialName("employment_type")
    var employmentType: String = "",

    @SerialName("location")
    var location: String = "",

    @SerialName("start_date")
    var startDate: Long = 0,

    @SerialName("end_date")
    var endDate: Long = 0,

    @SerialName("is_current_job")
    var isCurrentJob: Boolean = false,

    @Transient
    var error: ApplicantExperienceEntryError? = null
)

@Serializable
data class ApplicantSkill(

    @SerialName("skill")
    val skill: String = "",
    @SerialName("skill_code")
    val skillCode: String = "",
)

data class ApplicantCertificateError(
    val issuedBy: String? = null,
    val image: String? = null
)


@Serializable
data class ApplicantCertificateInfo(
    @SerialName("issued_by") val issuedBy: String = "",
    @SerialName("file_name") val fileName: String = "",
    @SerialName("file_size") val fileSize: Long = 0,
    @SerialName("type") val type: String = "",
    @SerialName("image") val image: String? = null,
    @Transient
    val error: ApplicantCertificateError? = null,
    @SerialName("id") val id: Int = -1
)


@Serializable
data class ApplicantResumeDocument(
    @SerialName("file_name")
    val fileName: String,

    @SerialName("file_size")
    val fileSize: Long,

    @SerialName("type")
    val type: String,

    @SerialName("resume")
    val resume: String,

    @SerialName("last_used")
    val lastUsed: Long? = null
)

@Serializable
data class ApplicantLanguage(
    val language: ApplicantLanguageOption? = null,
    val proficiency: ApplicantProficiencyOption? = null
)

@Serializable
data class ApplicantLanguageOption(
    val name: String,
    val code: String
)

@Serializable
data class ApplicantProficiencyOption(
    val name: String,
    val value: String
)


class ApplicantProfileViewModel  constructor(val context: Context) :
    ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val languages = listOf(
        ApplicantLanguageOption("Afrikaans", "af"),
        ApplicantLanguageOption("Amharic", "am"),
        ApplicantLanguageOption("Arabic", "ar"),
        ApplicantLanguageOption("Bengali", "bn"),
        ApplicantLanguageOption("Burmese", "my"),
        ApplicantLanguageOption("Croatian", "hr"),
        ApplicantLanguageOption("Czech", "cs"),
        ApplicantLanguageOption("Dutch", "nl"),
        ApplicantLanguageOption("English", "en"),
        ApplicantLanguageOption("Finnish", "fi"),
        ApplicantLanguageOption("French", "fr"),
        ApplicantLanguageOption("German", "de"),
        ApplicantLanguageOption("Greek", "el"),
        ApplicantLanguageOption("Gujarati", "gu"),
        ApplicantLanguageOption("Hebrew", "he"),
        ApplicantLanguageOption("Hindi", "hi"),
        ApplicantLanguageOption("Hungarian", "hu"),
        ApplicantLanguageOption("Italian", "it"),
        ApplicantLanguageOption("Japanese", "ja"),
        ApplicantLanguageOption("Javanese", "jv"),
        ApplicantLanguageOption("Kannada", "kn"),
        ApplicantLanguageOption("Korean", "ko"),
        ApplicantLanguageOption("Malay", "ms"),
        ApplicantLanguageOption("Malayalam", "ml"),
        ApplicantLanguageOption("Mandarin Chinese", "zh"),
        ApplicantLanguageOption("Marathi", "mr"),
        ApplicantLanguageOption("Nepali", "ne"),
        ApplicantLanguageOption("Norwegian", "no"),
        ApplicantLanguageOption("Oriya (Odia)", "or"),
        ApplicantLanguageOption("Persian (Farsi)", "fa"),
        ApplicantLanguageOption("Polish", "pl"),
        ApplicantLanguageOption("Portuguese", "pt"),
        ApplicantLanguageOption("Punjabi", "pa"),
        ApplicantLanguageOption("Romanian", "ro"),
        ApplicantLanguageOption("Russian", "ru"),
        ApplicantLanguageOption("Serbian", "sr"),
        ApplicantLanguageOption("Sinhala", "si"),
        ApplicantLanguageOption("Slovak", "sk"),
        ApplicantLanguageOption("Spanish", "es"),
        ApplicantLanguageOption("Swahili", "sw"),
        ApplicantLanguageOption("Swedish", "sv"),
        ApplicantLanguageOption("Tamil", "ta"),
        ApplicantLanguageOption("Telugu", "te"),
        ApplicantLanguageOption("Thai", "th"),
        ApplicantLanguageOption("Turkish", "tr"),
        ApplicantLanguageOption("Ukrainian", "uk"),
        ApplicantLanguageOption("Urdu", "ur"),
        ApplicantLanguageOption("Vietnamese", "vi"),
        ApplicantLanguageOption("Xhosa", "xh"),
        ApplicantLanguageOption("Zulu", "zu")
    )

    companion object {
        const val MAX_ONBOARD_CAREER_STEP = 7
    }

    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()

    private val _step = MutableStateFlow(0)
    val step = _step.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _completedStep = MutableStateFlow(0)
    val completedStep = _completedStep.asStateFlow()

    private val _isImagePickerLauncherLaunched = MutableStateFlow(false)
    val isImagePickerLauncherLaunched = _isImagePickerLauncherLaunched.asStateFlow()

    private val _isCameraPickerLauncherLaunched = MutableStateFlow(false)
    val isCameraPickerLauncherLaunched = _isCameraPickerLauncherLaunched.asStateFlow()

    private val _cameraPickerUri = MutableStateFlow<Uri?>(null)
    val cameraPickerUri = _cameraPickerUri.asStateFlow()

    private val _professionalInfo = MutableStateFlow(ApplicantProfessionalInfo())
    val professionalInfo = _professionalInfo.asStateFlow()

    private val _professionalInfoError = MutableStateFlow(ApplicantProfessionalInfoError())
    val professionalInfoError = _professionalInfoError.asStateFlow()

    private val _educationList = MutableStateFlow(listOf(ApplicantEducationEntry()))
    val educationList = _educationList.asStateFlow()

    private val _educationInfoError = MutableStateFlow<String?>(null)
    val educationInfoError = _educationInfoError.asStateFlow()

    private val _experienceList = MutableStateFlow(listOf(ApplicantExperienceEntry()))
    val experienceList = _experienceList.asStateFlow()

    private val _hasNoExperience = MutableStateFlow(false)
    val hasNoExperience = _hasNoExperience.asStateFlow()

    private val _selectedSkills = MutableStateFlow<List<ApplicantSkill>>(emptyList())
    val selectedSkills = _selectedSkills.asStateFlow()

    private val _skillInfoError = MutableStateFlow<String?>(null)
    val skillInfoError = _skillInfoError.asStateFlow()

    private val _certificates = MutableStateFlow(listOf(ApplicantCertificateInfo()))
    val certificates = _certificates.asStateFlow()

    private val _userPrefsLanguages = MutableStateFlow(listOf(ApplicantLanguage()))
    val userPrefsLanguages = _userPrefsLanguages.asStateFlow()

    private val _languageInfoError = MutableStateFlow<String?>(null)
    val languageInfoError = _languageInfoError.asStateFlow()


    private val _applicantResumeDocument = MutableStateFlow<ApplicantResumeDocument?>(null)
    val applicantResumeDocument = _applicantResumeDocument.asStateFlow()

    private val _resumeError = MutableStateFlow<String?>(null)
    val resumeError = _resumeError.asStateFlow()


    private val _applicantProfile = MutableStateFlow<ApplicantProfile?>(null)
    val applicantProfile = _applicantProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        fetchApplicantProfile()
    }

    fun fetchApplicantProfile(){
        onGetApplicantProfile(userId = userId, onSuccess = {
            _isLoading.value = false
        }, onError = {
            _isLoading.value = false
        })
    }

    fun onFirstNameChange(newValue: String) {
        _professionalInfo.value = _professionalInfo.value.copy(firstName = newValue)
    }

    fun onLastNameChange(newValue: String) {
        _professionalInfo.value = _professionalInfo.value.copy(lastName = newValue)
    }

    fun onGenderChange(newValue: String) {
        _professionalInfo.value = _professionalInfo.value.copy(gender = newValue)
    }

    fun onEmailChange(newValue: String) {
        _professionalInfo.value = _professionalInfo.value.copy(email = newValue)
    }

    fun onIntroChange(newValue: String) {
        _professionalInfo.value = _professionalInfo.value.copy(intro = newValue)
    }

    // Functions to update states
    fun onUpdateProfilePic(uri: Uri) {
        _professionalInfo.value = _professionalInfo.value.copy(profilePic = uri.toString())
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

    private fun onUpdateStep(step: Int) {
        val nextCompleteStep = (step).coerceAtMost(MAX_ONBOARD_CAREER_STEP)
        _step.value = nextCompleteStep
        onUpdateCompleteStep(nextCompleteStep)
    }

    private fun onUpdateCompleteStep(step: Int) {
        _completedStep.value = (step - 1).coerceAtLeast(0)
    }

    fun updateContent(content:String){
        _content.value = content
    }

    fun validatePersonalInfoForm(): Boolean {
        val info = _professionalInfo.value
        var isValid = true

        val updatedErrors = ApplicantProfessionalInfoError(
            firstName = if (info.firstName.isBlank()) {
                isValid = false
                "First name is required"
            } else null,

            lastName = if (info.lastName.isBlank()) {
                isValid = false
                "Last name is required"
            } else null,

            gender = if (info.gender.isBlank() || info.gender !in listOf(
                    "Male",
                    "Female",
                    "Other"
                )
            ) {
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

        _professionalInfoError.value = updatedErrors
        return isValid
    }

    fun validateEducationInfoForm(): Boolean {
        val info = _educationList.value
        var isValid = true

        if (info.size > 3) {
            isValid = false
            _educationInfoError.value = "You can add up to 3 education entries"
        }

        val updatedList = info.map { entry ->
            var hasError = false

            val startDate = if (entry.startYear > 0) Date(entry.startYear) else null
            val endDate =
                if (!entry.currentlyStudying && entry.endYear > 0) Date(entry.endYear) else null

            val error = ApplicantEducationEntryError(
                institution = if (entry.institution.isBlank()) {
                    hasError = true
                    "Institution is required"
                } else null,
                fieldOfStudy = if (entry.fieldOfStudy.isBlank()) {
                    hasError = true
                    "Field of Study is required"
                } else null,
                startYear = if (startDate == null) {
                    hasError = true
                    "Start date is required"
                } else null,
                endYear = when {
                    !entry.currentlyStudying && endDate == null -> {
                        hasError = true
                        "End date is required"
                    }

                    startDate != null && endDate != null && endDate.before(startDate) -> {
                        hasError = true
                        "End date must be after start date"
                    }

                    else -> null
                },
                grade = if (!entry.currentlyStudying && entry.grade <= 0.00) {
                    hasError = true
                    "Grade is required"
                } else null
            )

            if (hasError) isValid = false

            entry.copy(error = error)
        }

        _educationList.value = updatedList

        return isValid
    }

    fun validateExperienceInfoForm(): Boolean {
        val info = _experienceList.value
        var isValid = true

        val updatedList = info.map { entry ->


            var hasError = false

            val startDate = if (entry.startDate > 0) Date(entry.endDate) else null

            val endDate =
                if (!entry.isCurrentJob && entry.endDate > 0) Date(entry.endDate) else null

            val error = ApplicantExperienceEntryError(
                companyName = if (entry.companyName.isBlank() || entry.companyName.isEmpty()) {
                    hasError = true
                    "Company name is required"
                } else null,
                jobTitle = if (entry.jobTitle.isBlank()) {
                    hasError = true
                    "Job title is required"
                } else null,
                employmentType = if (entry.employmentType.isBlank()) {
                    hasError = true
                    "Employment type is required"
                } else null,
                location = if (entry.location.isBlank()) {
                    hasError = true
                    "Location is required"
                } else null,
                startDate = if (startDate == null) {
                    hasError = true
                    "Start date is required"
                } else null,
                endDate = when {
                    !entry.isCurrentJob && endDate == null -> {
                        hasError = true
                        "End date is required"
                    }

                    startDate != null && endDate != null && endDate.before(startDate) -> {
                        hasError = true
                        "End date must be after start date"
                    }

                    else -> null
                }
            )

            if (hasError) isValid = false

            entry.copy(error = error)
        }

        _experienceList.value = updatedList

        return isValid
    }

    fun validateSkillInfoForm(): Boolean {
        val skills = _selectedSkills.value
        var isValid = true

        if (skills.isEmpty()) {
            isValid = false
            _skillInfoError.value = "Please select at least one skill"
        } else {
            _skillInfoError.value = null
        }

        return isValid
    }

    fun validateCertificateInfoForm(): Boolean {
        val certificates = _certificates.value
        var isValid = true

        // Validate if at least one certificate exists
        if (certificates.isEmpty()) {
            _languageInfoError.value = "Please choose at least one certificate"
            return false
        } else {
            _languageInfoError.value = null
        }

        // Create a new list to hold updated certificates with errors
        val updatedList = certificates.map { cert ->
            var certError = ApplicantCertificateError()

            // Validate 'issuedBy'
            if (cert.issuedBy.isBlank()) {
                certError = certError.copy(issuedBy = "Issued by is required")
                isValid = false
            }

            // Validate 'image'
            if (cert.image.isNullOrBlank()) {
                certError = certError.copy(image = "Image is required")
                isValid = false
            }

            // Return a new certificate with the error information
            cert.copy(error = certError)
        }

        // Update the certificates list with errors
        _certificates.value = updatedList

        return isValid
    }

    fun validateLanguageInfoForm(): Boolean {
        val languages = _userPrefsLanguages.value
        var isValid = true

        // 1. Validate at least one skill is selected
        if (languages.isEmpty()) {
            isValid = false
            _languageInfoError.value = "Please select at least one language"
        } else {
            _languageInfoError.value = null
        }

        return isValid
    }

    fun validateResumeInfoForm(): Boolean {
        val resume = _applicantResumeDocument.value
        var isValid = true

        if (resume == null) {
            isValid = false
            _resumeError.value = "Please select valid resume"
        } else {
            _resumeError.value = null
        }

        return isValid
    }


    private fun onUpdateApplicantProfile(applicantProfile: ApplicantProfile) {
        _applicantProfile.value = applicantProfile

        onUpdateStep(applicantProfile.nextCompleteStep)


        _professionalInfo.value = applicantProfile.applicantProfessionalInfo
        _educationList.value = applicantProfile.applicantEducations.ifEmpty {
            listOf(ApplicantEducationEntry())
        }

        _hasNoExperience.value = _experienceList.value.any {
            !it.experienced
        }

        if(!_hasNoExperience.value){
            _experienceList.value = applicantProfile.applicantExperiences.ifEmpty {
                listOf(ApplicantExperienceEntry())
            }
        }


        _selectedSkills.value = applicantProfile.applicantSkills

        _certificates.value = applicantProfile.applicantCertificate.ifEmpty {
            listOf(ApplicantCertificateInfo())
        }
        _applicantResumeDocument.value = applicantProfile.applicantResumeDocument
        _userPrefsLanguages.value = applicantProfile.applicantLanguages.ifEmpty {
            listOf(ApplicantLanguage())
        }
    }

    fun isRefreshing(isRefreshing:Boolean){
        _isRefreshing.value= isRefreshing
    }

    fun onGetApplicantProfile(
        userId: Long,
        onSuccess: () -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            try {
                when (val result = getApplicantProfile(userId)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess()
                        _error.value?.also { _error.value = null }

                    }

                    is Result.Error -> {
                        _error.value = mapExceptionToError(result.error)
                        onError(result.error.message)
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message)
            }
        }
    }


    fun onUpdatePersonalInfoForm(
        professionalInfo: ApplicantProfessionalInfo,
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updatePersonalInfoForm(professionalInfo)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun onUpdateEducationInfoForm(
        applicantEducationEntries: List<ApplicantEducationEntry>,
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updateEducationInfoForm(applicantEducationEntries)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun onUpdateExperienceInfoForm(
        applicantExperienceEntries: List<ApplicantExperienceEntry>,
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updateExperienceInfoForm(applicantExperienceEntries)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun onUpdateNoExperienceInfoForm(
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updateNoExperienceInfoForm()) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }


    fun onUpdateSkillInfoForm(
        applicantSkillsEntries: List<ApplicantSkill>,
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updateSkillInfoForm(applicantSkillsEntries)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }


    fun onUpdateCertificateInfoForm(
        applicantCertificateInfoList: List<ApplicantCertificateInfo>,
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updateCertificateInfoForm(applicantCertificateInfoList)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }


    fun onUpdateLanguageInfoForm(
        applicantLanguageEntries: List<ApplicantLanguage>,
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updateLanguageInfoForm(applicantLanguageEntries)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }


    fun onUpdateResumeInfoForm(
        applicantResumeDocument: ApplicantResumeDocument,
        onSuccess: (String) -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            _isUpdating.value = true
            try {
                when (val result = updateResumeInfoForm(applicantResumeDocument)) {
                    is Result.Success -> {
                        val data = Json.decodeFromString<ApplicantProfile>(result.data.data)
                        onUpdateApplicantProfile(data)
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> onError(result.error.message)

                }
            } catch (e: Exception) {
                onError(e.message)
            } finally {
                _isUpdating.value = false
            }
        }
    }


    private suspend fun getApplicantProfile(userId: Long): Result<ResponseReply> {

        return try {

            AppClient.instance.create(ApplicantProfileApiService::class.java)
                .getApplicantProfile(userId)
                .let { response ->
                    if (response.isSuccessful) {
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
                }

        } catch (t: Throwable) {
            Result.Error(t)
        }
    }


    private suspend fun updatePersonalInfoForm(jobProfessionalInfo: ApplicantProfessionalInfo): Result<ResponseReply> {

        return try {
            val updateBody = Json.encodeToString(jobProfessionalInfo)
                .toRequestBody("application/json".toMediaType())

            val profilePicBody = jobProfessionalInfo.profilePic
                ?.takeIf { it.startsWith("content://") }
                ?.let {
                    val uri = it.toUri()
                    val displayName = getFileNameForUri(context, uri)
                        ?: throw NullPointerException("Display name is null")
                    val profilePicInputStreamRequestBody = InputStreamRequestBody(context, uri)
                    MultipartBody.Part.createFormData(
                        "profile_pic",
                        displayName,
                        profilePicInputStreamRequestBody
                    )
                }

            AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateProfessionalInfo(updateBody, profilePicBody)
                .let { response ->
                    if (response.isSuccessful) {
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
                }


        } catch (t: Throwable) {
            Result.Error(t)
        }
    }

    private suspend fun updateEducationInfoForm(applicantEducationEntries: List<ApplicantEducationEntry>): Result<ResponseReply> {

        return try {
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            val updateBody = json.encodeToString(applicantEducationEntries)
                .toRequestBody("application/json".toMediaType())

            val response = AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateEducationInfo(updateBody)

            if (response.isSuccessful) {
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

    private suspend fun updateExperienceInfoForm(applicantExperienceEntries: List<ApplicantExperienceEntry>): Result<ResponseReply> {

        return try {
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            val updateBody = json.encodeToString(applicantExperienceEntries)
                .toRequestBody("application/json".toMediaType())

            val response = AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateExperienceInfo(updateBody)

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


    private suspend fun updateNoExperienceInfoForm(): Result<ResponseReply> {

        return try {


            val response = AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateNoExperienceInfo()

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


    private suspend fun updateSkillInfoForm(applicantSkillsEntries: List<ApplicantSkill>): Result<ResponseReply> {

        return try {
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            val updateBody = json.encodeToString(applicantSkillsEntries)
                .toRequestBody("application/json".toMediaType())

            val response = AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateSkillInfo(updateBody)

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

    private suspend fun updateLanguageInfoForm(applicantLanguageEntries: List<ApplicantLanguage>): Result<ResponseReply> {

        return try {
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            val updateBody = json.encodeToString(applicantLanguageEntries)
                .toRequestBody("application/json".toMediaType())

            val response = AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateLanguageInfo(updateBody)

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

    private suspend fun updateResumeInfoForm(applicantResumeDocument: ApplicantResumeDocument): Result<ResponseReply> {

        return try {
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            val updateBody = json.encodeToString(applicantResumeDocument)
                .toRequestBody("application/json".toMediaType())

            val resumeBody = applicantResumeDocument.resume.let {

                val profilePicInputStreamRequestBody = InputStreamRequestBody(context, it.toUri())
                MultipartBody.Part.createFormData(
                    "resume",
                    applicantResumeDocument.fileName,
                    profilePicInputStreamRequestBody
                )
            }

            val response = AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateResumeInfo(updateBody, resumeBody)

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
            t.printStackTrace()
            Result.Error(t)
        }
    }


    private suspend fun updateCertificateInfoForm(
        applicantCertificateInfoList: List<ApplicantCertificateInfo>
    ): Result<ResponseReply> {
        return try {
            // Serialize the list of certificate info into JSON
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            val updateBody = json.encodeToString(applicantCertificateInfoList)
                .toRequestBody("application/json".toMediaType())

            // Create multipart body parts for all images
            val certificateParts = applicantCertificateInfoList.mapNotNull { info ->
                val uri =
                    info.image?.toUri().takeIf { info.image?.startsWith("content://") == true }
                if (uri != null) {
                    val inputStreamRequestBody = InputStreamRequestBody(context, uri)
                    MultipartBody.Part.createFormData(
                        "certificates-${info.id.takeIf { it != -1 } ?: "new"}", // This field name must match the backend
                        info.fileName,
                        inputStreamRequestBody
                    )
                } else {
                    null
                }
            }

            // Make the API call
            val response = AppClient.instance.create(ApplicantProfileApiService::class.java)
                .updateCertificateInfo(updateBody, certificateParts)

            // Handle response
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {
                    Result.Success(body)
                } else {
                    Result.Error(Exception("Failed, try again later..."))
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
            t.printStackTrace()
            Result.Error(t)
        }
    }


    fun updateEducationList(updatedList: List<ApplicantEducationEntry>) {
        _educationList.value = updatedList
    }

    fun updateExperienceList(updatedList: List<ApplicantExperienceEntry>) {
        _experienceList.value = updatedList
    }

    fun setHasNoExperience(value: Boolean) {
        _hasNoExperience.value = value
    }


    fun updateSelectedSkills(skills: List<ApplicantSkill>) {
        _selectedSkills.value = skills
    }

    fun removeSkill(skill: ApplicantSkill) {
        _selectedSkills.value -= skill
    }


    fun nextStep() {
        _step.value += 1
    }

    fun previousStep() {
        _step.value -= 1
    }

    fun updateCertificate(index: Int, updatedCertificate: ApplicantCertificateInfo) {
        _certificates.value = _certificates.value.toMutableList().apply {
            this[index] = updatedCertificate
        }
    }

    fun addCertificate() {
        _certificates.value += ApplicantCertificateInfo()
    }

    fun removeCertificate(index: Int) {
        _certificates.value = _certificates.value.toMutableList().apply {
            removeAt(index)
        }
    }

    fun updateLanguage(index: Int, language: ApplicantLanguageOption) {
        _userPrefsLanguages.value = _userPrefsLanguages.value.toMutableList().apply {
            this[index] = this[index].copy(language = language)
        }
    }

    fun updateProficiency(index: Int, proficiency: ApplicantProficiencyOption) {
        _userPrefsLanguages.value = _userPrefsLanguages.value.toMutableList().apply {
            this[index] = this[index].copy(proficiency = proficiency)
        }
    }

    fun addLanguage() {
        _userPrefsLanguages.value += ApplicantLanguage()
    }

    fun removeLanguage(index: Int) {
        _userPrefsLanguages.value = _userPrefsLanguages.value.toMutableList().apply {
            removeAt(index)
        }
    }

    // Update file details
    fun updateFile(applicantResumeDocument: ApplicantResumeDocument) {
        _applicantResumeDocument.value = applicantResumeDocument
    }

    // Remove file details
    fun removeFile() {
        _applicantResumeDocument.value = null
    }

}
