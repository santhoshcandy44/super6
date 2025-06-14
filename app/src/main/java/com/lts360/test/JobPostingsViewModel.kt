package com.lts360.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.utils.ResultError
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsRepository
import com.lts360.test.job.JobsPageSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class JobPostingsViewModel (
    val savedStateHandle: SavedStateHandle,
    tokenManager: TokenManager,
    private val guestUserLocationDao: UserLocationDao,
    networkConnectivityManager: NetworkConnectivityManager,
    regionalSettingsRepository: RegionalSettingsRepository

) : ViewModel() {

    val submittedQuery = savedStateHandle.get<String?>("submittedQuery")
    val onlySearchBar = savedStateHandle.get<Boolean>("onlySearchBar") == true
    private val key = savedStateHandle.get<Int>("key") ?: 0

    val userId = UserSharedPreferencesManager.userId

    val countryCode = regionalSettingsRepository.getCountryFromPreferences()?.code
        ?: Locale.getDefault().country.uppercase()

    private val _selectedJobPosting = MutableStateFlow<JobPosting?>(null)
    val selectedJobPosting = _selectedJobPosting.asStateFlow()


    private val _showFilters = MutableStateFlow(false)
    val showFilters = _showFilters.asStateFlow()

    private val _filterCategory = MutableStateFlow(FilterCategory.WORK_MODE)
    val filterCategory = _filterCategory.asStateFlow()

    private val _filters = MutableStateFlow(JobFilters())
    val filters = _filters.asStateFlow()

    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()


    private var _pageSource = JobsPageSource(pageSize = 1)
    val pageSource: JobsPageSource get() = _pageSource

    private val _lastLoadedItemPosition = MutableStateFlow(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()

    val connectivityManager = networkConnectivityManager

    val isGuest = tokenManager.isGuest()

    private var loadingItemsJob: Job? = null

    init {

        if (isGuest) {
            viewModelScope.launch {

                launch {
                    val userLocation = guestUserLocationDao.getLocation(userId)
                    pageSource.guestNextPage(
                        userId,
                        submittedQuery,
                        userLocation?.latitude,
                        userLocation?.longitude
                    )
                }.join()

            }
        } else {
            loadingItemsJob = viewModelScope.launch {
                launch {
                    pageSource.nextPage(userId, submittedQuery)
                }.join()
            }
        }

    }

    fun updateSelectedJobPosting(selectedJobPosting: JobPosting) {
        _selectedJobPosting.value = selectedJobPosting
    }

    fun updateLastLoadedItemPosition(newPosition: Int) {
        viewModelScope.launch {
            _lastLoadedItemPosition.value = newPosition
        }
    }


    fun updateJobFiltersAndRefresh(userId: Long, submittedQuery: String, filters: JobFilters) {
        viewModelScope.launch {
            updateJobFilters(filters)
            refresh(userId, submittedQuery, filters)
        }
    }

    fun updateJobFilters(filters: JobFilters){
        viewModelScope.launch {
            _filters.value = filters
        }
    }

    fun updateShowFilters(isShowFilters: Boolean) {
        viewModelScope.launch {
            _showFilters.value = isShowFilters
        }
    }

    fun updateShowFilters(filterCategory: FilterCategory) {
        viewModelScope.launch {
            _filterCategory.value = filterCategory
        }
    }


    fun nextPage(
        userId: Long, query: String?,
        filters: JobFilters? = null
    ) {
        viewModelScope.launch {
            if (isGuest) {

                val userLocation = guestUserLocationDao.getLocation(userId)
                if (userLocation != null) {
                    pageSource.guestNextPage(
                        userId, query, userLocation.latitude,
                        userLocation.longitude
                    )
                } else {
                    pageSource.guestNextPage(userId, query)
                }

            } else {
                loadingItemsJob?.cancel()
                loadingItemsJob = launch {
                    pageSource.nextPage(userId, query, filters = filters)
                }
            }
        }
    }

    fun refresh(userId: Long, query: String?, filters: JobFilters?) {
        viewModelScope.launch {
            if (isGuest) {
                val location = guestUserLocationDao.getLocation(userId)
                pageSource.guestRefresh(
                    userId,
                    query,
                    location?.latitude,
                    location?.longitude
                )
            } else {
                loadingItemsJob?.cancel()
                loadingItemsJob = launch {
                    pageSource.refresh(userId, query, filters)
                }
            }
        }
    }


    fun retry(userId: Long, query: String?, filters: JobFilters?) {
        viewModelScope.launch {
            if (isGuest) {
                val location = guestUserLocationDao.getLocation(userId)
                pageSource.guestRetry(
                    userId,
                    query,
                    location?.latitude,
                    location?.longitude
                )
            } else {
                pageSource.retry(userId, query, filters)
            }
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
    val recruiter: Recruiter,

    @SerialName("initial_check_at")
    var initialCheckAt: String?,

    @SerialName("total_relevance")
    var totalRelevance: String?
)

fun JobPosting.formattedPostedBy(): String {
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


enum class Role {
    RECRUITER,
    HIRING_MANAGER,
    TALENT_ACQUISITION,
    HR
}

@Serializable
enum class WorkMode(val displayName: String, val value: String) {

    @SerialName("remote")
    REMOTE("Remote", "remote"),

    @SerialName("office")
    OFFICE("Office", "office"),

    @SerialName("hybrid")
    HYBRID("Hybrid", "hybrid"),

    @SerialName("flexible")
    FLEXIBLE("Flexible", "flexible");
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

