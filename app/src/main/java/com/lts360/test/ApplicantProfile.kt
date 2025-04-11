package com.lts360.test

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.libs.ui.ClockWiseCircularProgressBar
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobProfessionalInfo(
    @SerialName("profile_pic")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantProfileScreen() {

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullToRefresh(false, pullToRefreshState) { /*onRefresh*/ }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.customColorScheme.jobUserProfileBackground)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {

                JobProfileDisplayCard(
                    0.7f,
                    JobProfessionalInfo(
                        firstName = "Saravana",
                        lastName = "Kumar",
                        gender = "Male",
                        email = "santhoshcandy44@gmail.com",
                        intro = "As a dedicated Kotlin seeker, I am passionate about mastering Kotlin, a modern, expressive, and powerful programming language. With a focus on building efficient, scalable, and maintainable solutions, I am particularly drawn to its applications in Android development and cross-platform projects. My journey in Kotlin is driven by a desire to harness its full potential to create high-quality software that delivers seamless user experiences."
                    ), {

                    })

                val sampleEducationList = listOf(
                    EducationEntry("MIT", "Computer Science", "2015", "2019", "A"),
                    EducationEntry("Stanford", "AI Research", "2020", "2022", "A+"),
                    EducationEntry("Harvard", "Business", "2018", "2020", "B+")
                )

                EducationDisplayCards(sampleEducationList, {})

                val sampleExperienceList = listOf(
                    ExperienceEntry(
                        companyName = "Google",
                        jobTitle = "Software Engineer",
                        employmentType = "Full-Time",
                        location = "Mountain View, CA",
                        startDate = "Jan 2020",
                        endDate = "Dec 2023",
                        isCurrentJob = false
                    ),
                    ExperienceEntry(
                        companyName = "Meta",
                        jobTitle = "Senior Developer",
                        employmentType = "Contract",
                        location = "Remote",
                        startDate = "Jan 2024",
                        endDate = "",
                        isCurrentJob = true
                    )
                )

                ExperienceDisplayCards(sampleExperienceList, {})

                val sampleSkills = listOf(
                    JobSkill("Kotlin"),
                    JobSkill("Jetpack Compose"),
                    JobSkill("Firebase"),
                    JobSkill("SQL")
                )

                JobSkillDisplayChips(sampleSkills, {})

                val sampleCertificates = listOf(
                    JobCertificateInfo(
                        "Google",
                        "https://cdn.create.microsoft.com/catalog-assets/en-us/5824ad94-f1f1-4ef0-8f4a-312e98b556a3/thumbnails/1034/olive-branch-certificate-of-accomplishment-gray-organic-simple-1-1-a60a7e665ae6.webp"
                    ),
                    JobCertificateInfo(
                        "Coursera",
                        "https://marketplace.canva.com/EAFy42rCTA0/1/0/1600w/canva-blue-minimalist-certificate-of-achievement-_asVJz8YgJE.jpg"
                    ),
                    JobCertificateInfo(
                        "Udemy",
                        "https://d1csarkz8obe9u.cloudfront.net/posterpreviews/professional-certificate-design-template-882d9df12b1505d88cfb20c9b7ed22bb_screen.jpg?ts=1689415226"
                    )
                )

                CertificateDisplayCards(sampleCertificates, {})

                val languageList = listOf(
                    JobProfileLanguage(language = JobLanguageOption("English", "en"), proficiency = "Fluent"),
                    JobProfileLanguage(language = JobLanguageOption("Spanish", "es"), proficiency = "Intermediate"),
                    JobProfileLanguage(language = JobLanguageOption("French", "fr"), proficiency = "Basic")
                )

                JobProfileLanguagesDisplay(languageList, {})

                ResumeCardDisplay({})
            }

            Indicator(pullToRefreshState, false, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}


@Composable
private fun JobProfileDisplayCard(
    profileProgress:Float,
    professionalInfo: JobProfessionalInfo,
    onEditClicked: () -> Unit) {


    Card(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {

        Column(modifier = Modifier
                .fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Professional Information",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.Start
            ) {

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Profile Picture",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Profile Picture Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {

                            Image(
                                painter = painterResource(R.drawable.user_placeholder),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {


                            val progressColor = when {
                                profileProgress < 0.5f -> Color.Red         // Low = Red
                                profileProgress < 0.8f -> Color(0xFFFFA500) // Medium = Orange (Hex for orange)
                                else -> Color(0xFF4CAF50)            // High = Green
                            }


                            ClockWiseCircularProgressBar(
                                progress = 0.7f,
                                radius = 40.dp,
                                strokeWidth = 16.dp,
                                circleColor = Color.LightGray,
                                progressColor = progressColor,
                                isClockWise = true
                            )

                            Text("${profileProgress*100}%", fontWeight = FontWeight.Bold)
                        }

                    }
                }


                // Name Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.titleMedium
                    )
                    CustomTextField(
                        value = professionalInfo.firstName,
                        onValueChange = { },
                        label = "First Name",
                        readOnly = true
                    )
                    CustomTextField(
                        value = professionalInfo.lastName,
                        onValueChange = { },
                        label = "Last Name",
                        readOnly = true
                    )
                }

                // Gender Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.titleMedium
                    )
                    GenderSelector(professionalInfo.gender, isGenderSelectEnabled = false) {}
                }

                // Email Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.titleMedium
                    )
                    CustomTextField(
                        value = professionalInfo.email,
                        onValueChange = { },
                        label = "Email",
                        readOnly = true
                    )
                }

                // Intro Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Introduction",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        professionalInfo.intro,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
    }

}

@Composable
private fun EducationDisplayCards(
    educationList: List<EducationEntry>,
    onEditClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Edit button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Education Information",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Education Cards
            educationList.forEach { entry ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = entry.institution, style = MaterialTheme.typography.titleMedium)
                    Text(text = entry.fieldOfStudy, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Start: ${entry.startYear}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "End: ${entry.endYear}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Grade: ${entry.grade}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp)
            }
        }
    }
}


@Composable
private fun ExperienceDisplayCards(
    experienceList: List<ExperienceEntry>,
    onEditClicked: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Header with Edit button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experience Information",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Experience Cards
            experienceList.forEach { entry ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = entry.companyName, style = MaterialTheme.typography.titleLarge)
                    Text(text = entry.jobTitle, style = MaterialTheme.typography.bodyMedium)
                    Text(text = entry.employmentType, style = MaterialTheme.typography.bodySmall)
                    Text(text = entry.location, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Start: ${entry.startDate}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (entry.isCurrentJob) {
                            Text("Present", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text(
                                "End: ${entry.endDate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp)
            }
        }
    }
}


@Composable
private fun JobSkillDisplayChips(
    skills: List<JobSkill>,
    onEditClicked: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            // Header with Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skills",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (skills.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skills.forEach { skill ->
                        SkillChip(
                            skill = skill.skill,
                            enableRemoveButton = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateDisplayCards(
    certificateList: List<JobCertificateInfo>,
    onEditClicked: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Certificates",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Certificate Cards
            certificateList.forEach { certificate ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Image if available
                        certificate.image?.let { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Certificate Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Issued By
                        Text(
                            text = "Issued By: ${certificate.issuedBy}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun JobProfileLanguagesDisplay(
    languages: List<JobProfileLanguage>,
    onEditClicked: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Languages",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (languages.isEmpty()) {
                Text(
                    text = "No languages added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Show each language with proficiency in a single row
                languages.forEach { languageEntry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = languageEntry.language?.name ?: "",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = languageEntry.proficiency,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumeCardDisplay(onEditClicked: () -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resume",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResumeCard(
                    fileName = "My_Resume.pdf",
                    fileSizeInBytes = 1024 * 1024,
                    lastModified = System.currentTimeMillis(),
                    removeButtonEnabled = false
                )
            }
        }
    }
}


