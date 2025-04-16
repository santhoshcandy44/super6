package com.lts360.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.api.utils.ResultError
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.libs.ui.ClockWiseCircularProgressBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantProfileScreen(
    applicantProfile: ApplicantProfile?,
    error: ResultError?,
    onEditProfile: () -> Unit,
    onEditEducation: () -> Unit,
    onEditExperience: () -> Unit,
    onEditSkill: () -> Unit,
    onEditCertificate: () -> Unit,
    onEditLanguage: () -> Unit,
    onEditResume: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {

    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(isRefreshing, pullToRefreshState, onRefresh = onRefresh)
    ) {
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.customColorScheme.jobUserProfileBackground),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            error?.let {
               item {
                   Box(
                       modifier = Modifier.fillParentMaxSize(),
                       contentAlignment = Alignment.Center
                   ) {
                       when (error) {
                           is ResultError.NoInternet -> {
                               NoInternetScreen {
                                   onRefresh()
                               }
                           }

                           else -> {
                               Column(modifier = Modifier.wrapContentSize()) {
                                   Image(
                                       painter = painterResource(R.drawable.something_went_wrong),
                                       contentDescription = "Image from drawable",
                                       modifier = Modifier
                                           .sizeIn(
                                               maxWidth = 200.dp,
                                               maxHeight = 200.dp
                                           )
                                   )

                                   Spacer(Modifier.height(16.dp))

                                   Text(text = "Oops, something went wrong")
                               }
                           }
                       }

                   }
               }
            } ?: run {
                applicantProfile?.let {

                    item {
                        LazyRow(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)){
                            val suggestions = it.getMissingFieldSuggestions()
                            items(suggestions){
                                SuggestionCard(it.title, it.message,it.contribution * 100)
                            }
                        }
                    }

                    item {
                        ProfileDisplayCard(
                            profileProgress = it.getProfileCompletion(),
                            professionalInfo = it.applicantProfessionalInfo,
                            onEditClicked = onEditProfile
                        )
                    }

                    item{
                        EducationDisplayCards(
                            educationList = it.applicantEducations,
                            onEditClicked = onEditEducation
                        )
                    }

                    item{
                        ExperienceDisplayCards(
                            experienceList = it.applicantExperiences,
                            onEditClicked = onEditExperience
                        )
                    }

                    item{
                        SkillDisplayChips(
                            skills = it.applicantSkills,
                            onEditClicked = onEditSkill
                        )
                    }

                    item {
                        CertificateDisplayCards(
                            certificateList = it.applicantCertificate,
                            onEditClicked = onEditCertificate
                        )
                    }

                    item {
                        ProfileLanguagesDisplay(
                            languages = it.applicantLanguages,
                            onEditClicked = onEditLanguage
                        )
                    }

                    item {
                        ResumeCardDisplay(
                            applicantResumeDocument = it.applicantResumeDocument,
                            onEditClicked = onEditResume
                        )
                    }
                }
            }
        }

        Indicator(pullToRefreshState, isRefreshing, modifier = Modifier.align(Alignment.TopCenter))
    }
}



@Composable
private fun ProfileDisplayCard(
    profileProgress: Float,
    professionalInfo: ApplicantProfessionalInfo,
    onEditClicked: () -> Unit
) {



    Card(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {



        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
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

                            professionalInfo.profilePic?.let {
                                AsyncImage(it,
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
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {


                            val progressColor = when {
                                profileProgress < 0.5f -> Color.Red         // Low = Red
                                profileProgress < 0.8f -> Color(0xFFFFA500) // Medium = Orange (Hex for orange)
                                else -> Color(0xFF4CAF50)            // High = Green
                            }


                            ClockWiseCircularProgressBar(
                                progress = profileProgress,
                                radius = 40.dp,
                                strokeWidth = 16.dp,
                                circleColor = Color.LightGray,
                                progressColor = progressColor,
                                isClockWise = true
                            )
                            val percentage = profileProgress * 100
                            val formattedString = if (percentage % 1f == 0f) {
                                String.format(Locale.getDefault(), "%.0f", percentage)
                            } else {
                                String.format(Locale.getDefault(), "%.1f", percentage)
                            }
                            Text("${formattedString}%", fontWeight = FontWeight.Bold)
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
    educationList: List<ApplicantEducationEntry>,
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
                            "Start: ${
                                SimpleDateFormat(
                                    "dd-MM-yyyy",
                                    Locale.getDefault()
                                ).format(Date(entry.startYear))
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (entry.currentlyStudying) {
                            Text(
                                "Currently Working",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "End: ${
                                    SimpleDateFormat(
                                        "dd-MM-yyyy",
                                        Locale.getDefault()
                                    ).format(Date(entry.endYear))
                                }",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

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
    experienceList: List<ApplicantExperienceEntry>,
    onEditClicked: () -> Unit
) {

    val hasNoExperience = experienceList.any { !it.experienced }

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


            if (hasNoExperience) {
                Text(
                    "No Experience (Fresher)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            } else {

                // Experience Cards
                experienceList.forEach { entry ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = entry.companyName, style = MaterialTheme.typography.titleLarge)
                        Text(text = entry.jobTitle, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = entry.employmentType,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(text = entry.location, style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "Start: ${
                                    SimpleDateFormat(
                                        "dd-MM-yyyy",
                                        Locale.getDefault()
                                    ).format(Date(entry.startDate))
                                }",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (entry.isCurrentJob) {
                                Text("Present", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text(
                                    "End: ${
                                        SimpleDateFormat(
                                            "dd-MM-yyyy",
                                            Locale.getDefault()
                                        ).format(Date(entry.endDate))
                                    }",
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
}


@Composable
private fun SkillDisplayChips(
    skills: List<ApplicantSkill>,
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
    certificateList: List<ApplicantCertificateInfo>,
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

            if(certificateList.isNotEmpty()){
                certificateList.forEach { certificate ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        // Image if available
                        certificate.image?.let { url ->

                            // Image Picker Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Certificate Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16 / 9f)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = "Issued By: ${certificate.issuedBy}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(thickness = 1.dp)
                }
            }

        }
    }
}

@Composable
private fun ProfileLanguagesDisplay(
    languages: List<ApplicantLanguage>,
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
                                text = languageEntry.proficiency?.name ?: "",
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
private fun ResumeCardDisplay(
    applicantResumeDocument: ApplicantResumeDocument?,
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
                    text = "Resume",
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = onEditClicked) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            applicantResumeDocument?.let {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ResumeCard(
                        it,
                        removeButtonEnabled = false
                    )
                }
            }

        }
    }
}



@Composable
private fun SuggestionCard(title: String, message: String, percentBoost: Float) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "+${String.format(Locale.getDefault(), "%.1f", percentBoost)}% profile completion",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Green
            )
        }
    }
}
