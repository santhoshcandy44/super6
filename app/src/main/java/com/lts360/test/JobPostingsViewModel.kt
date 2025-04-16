package com.lts360.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.app.AppClient
import com.lts360.api.app.JobPostingsApiService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.compose.ui.main.viewmodels.SecondsPageSource
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.test.job.JobsPageSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class JobPostingsViewModel @Inject constructor() : ViewModel(){

    val userId = UserSharedPreferencesManager.userId

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()


    private val _jobPostings = MutableStateFlow<List<JobPosting>>(emptyList())
    val jobPostings = _jobPostings.asStateFlow()

    private val _selectedJobPosting = MutableStateFlow<JobPosting?>(null)
    val selectedJobPosting = _selectedJobPosting.asStateFlow()

    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()


    private var _pageSource = JobsPageSource(pageSize = 1)
    val pageSource: JobsPageSource get() = _pageSource

    private val _lastLoadedItemPosition = MutableStateFlow(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()

    init {
        onGetJobPostings(onSuccess = {
            _isLoading.value = false
        }, onError = {
            _isLoading.value = false
        })
    }

    fun updateSelectedJobPosting(selectedJobPosting:JobPosting){
        _selectedJobPosting.value = selectedJobPosting
    }

    fun updateLastLoadedItemPosition(newPosition: Int) {
        viewModelScope.launch {
            _lastLoadedItemPosition.value = newPosition
        }
    }

    fun onGetJobPostings(
        onSuccess: () -> Unit = {},
        onError: (message: String?) -> Unit = {}
    ) {

        viewModelScope.launch {
            try {
                when (val result = getJobPostings()) {
                    is Result.Success -> {

                        val json = Json {
                            ignoreUnknownKeys = true
                        }
                        val data = json.decodeFromString<List<JobPosting>>(result.data.data)
                        _jobPostings.value = data
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

    private suspend fun getJobPostings(): Result<ResponseReply> {

        return try {

            AppClient.instance.create(JobPostingsApiService::class.java)
                .gteJobPostings()
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

}


@Serializable
data class JobPosting(
    @SerialName("id")
    val id: Long = 0L,

    @SerialName("title")
    val title: String = "",

    @SerialName("work_mode")
    val workMode: WorkMode = WorkMode.OFFICE,

    @SerialName("location")
    val location: String = "",

    @SerialName("company_id")
    val companyId: Long,


    @SerialName("description")
    val description: String = "",

    @SerialName("education")
    val education: String = "",

    @SerialName("experience_type")
    val experienceType: ExperienceType = ExperienceType.FRESHER,

    @SerialName("experience_range_min")
    val experienceRangeMin: Int = 0,

    @SerialName("experience_range_max")
    val experienceRangeMax: Int = 0,

    @SerialName("experience_fixed")
    val experienceFixed: Int = 0,

    @SerialName("salary_min")
    val salaryMin: Double = 0.0,
    @SerialName("salary_max")
    val salaryMax: Double = 0.0,

    @SerialName("salary_min_formatted")
    val salaryMinFormatted: String? = null,

    @SerialName("salary_max_formatted")
    val salaryMaxFormatted: String? = null,

    @SerialName("salary_not_disclosed")
    val salaryNotDisclosed: Boolean = false,

    @SerialName("must_have_skills")
    val mustHaveSkills: List<String> = emptyList(),

    @SerialName("good_to_have_skills")
    val goodToHaveSkills: List<String> = emptyList(),

    @SerialName("industry_type")
    val industryType: String = "",

    @SerialName("department")
    val department: String = "",

    @SerialName("role")
    val role: String = "",

    @SerialName("employment_type")
    val employmentType: EmploymentType = EmploymentType.FULL_TIME,

    @SerialName("vacancies")
    val vacancies: Int = 1,

    @SerialName("highlights")
    val highlights: List<HighLightType> = emptyList(),

    @SerialName("posted_at")
    val postedAt: String = "",

    @SerialName("posted_by")
    val postedBy: Long,

    @SerialName("organization_id")
    val organizationId: Long? = null,

    @SerialName("expiry_date")
    val expiryDate: String = "",

    @SerialName("slug")
    val slug: String = "",

    @SerialName("organization")
    val organization: Organization,
    @SerialName("recruiter")
    val recruiter:Recruiter
)

fun JobPosting.formattedPostedBy():String{
    val zonedDateTime = ZonedDateTime.parse(postedAt)
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
    return zonedDateTime.format(formatter)
}


@Serializable
data class Organization(
    @SerialName("id")
    val id: Long,

    @SerialName("name")
    val name: String,

    @SerialName("logo")
    val logo: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("website")
    val website: String? = null,

    @SerialName("country")
    val country: String? = null,

    @SerialName("state")
    val state: String? = null,

    @SerialName("city")
    val city: String? = null,

    @SerialName("postal_code")
    val postalCode: String? = null
)
@Serializable
data class Recruiter(
    val id: Int,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    val email: String,
    val role: Role,
    val company: String,
    val phone: String,

    @SerialName("profile_picture")
    val profilePicture: String,

    val bio: String,

    @SerialName("years_of_experience")
    val yearsOfExperience: Int,

    @SerialName("is_verified")
    val isVerified: Boolean
)


enum class Role{
    RECRUITER,
    HIRING_MANAGER,
    TALENT_ACQUISITION,
    HR
}
enum class WorkMode {
    @SerialName("remote")
    REMOTE,

    @SerialName("office")
    OFFICE,

    @SerialName("hybrid")
    HYBRID
}

enum class ExperienceType {
    @SerialName("fresher")
    FRESHER,

    @SerialName("min_max")
    MIN_MAX,

    @SerialName("fixed")
    FIXED
}

enum class EmploymentType {
    @SerialName("full_time")
    FULL_TIME,

    @SerialName("part_time")
    PART_TIME,

    @SerialName("intern")
    INTERNSHIP,

    @SerialName("contract")
    CONTRACT
}

enum class HighLightType {
    @SerialName("free_food")
    FREE_FOOD,

    @SerialName("free_room")
    FREE_ROOM,

    @SerialName("transport")
    TRANSPORT,

    @SerialName("bonus")
    BONUS,

    @SerialName("health")
    HEALTH,

    @SerialName("training")
    TRAINING,

    @SerialName("flexible")
    FLEXIBLE
}

val HighLightType.emoji: String
    get() = when (this) {
        HighLightType.FREE_FOOD -> "🍕"
        HighLightType.FREE_ROOM -> "🏠"
        HighLightType.TRANSPORT -> "🚌"
        HighLightType.BONUS -> "💰"
        HighLightType.HEALTH -> "🏥"
        HighLightType.TRAINING -> "🎓"
        HighLightType.FLEXIBLE -> "⏰"
    }

val HighLightType.label: String
    get() = when (this) {
        HighLightType.FREE_FOOD -> "Free Food"
        HighLightType.FREE_ROOM -> "Free Room"
        HighLightType.TRANSPORT -> "Transport"
        HighLightType.BONUS -> "Bonus"
        HighLightType.HEALTH -> "Health"
        HighLightType.TRAINING -> "Training"
        HighLightType.FLEXIBLE -> "Flexible"
    }

val HighLightType.description: String
    get() = when (this) {
        HighLightType.FREE_FOOD -> "Daily meals provided"
        HighLightType.FREE_ROOM -> "Company housing"
        HighLightType.TRANSPORT -> "Commute coverage"
        HighLightType.BONUS -> "Yearly rewards"
        HighLightType.HEALTH -> "Full medical plan"
        HighLightType.TRAINING -> "Skill development"
        HighLightType.FLEXIBLE -> "Choose your schedule"
    }

