package com.lts360.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedJobPosting(jobPosting: JobPosting, onPopBackStack: ()-> Unit) {
    Scaffold { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                TopAppBar(title = {}, navigationIcon = {
                    IconButton(onPopBackStack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                })
            }
            item {
                Column(modifier = Modifier.padding(16.dp)) {

                    PosterInfo(jobPosting.recruiter)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = jobPosting.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = jobPosting.organization.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Enhanced chip row with better icon handling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip(
                            icon = Icons.Outlined.LocationOn,
                            text = jobPosting.location
                        )

                        InfoChip(
                            icon = when (jobPosting.workMode) {
                                WorkMode.REMOTE -> Icons.Outlined.Wifi
                                WorkMode.HYBRID -> Icons.Outlined.Hub
                                WorkMode.OFFICE -> Icons.Outlined.Business
                                WorkMode.FLEXIBLE -> Icons.Outlined.Commit
                            },
                            text = when (jobPosting.workMode) {
                                WorkMode.REMOTE -> "Remote"
                                WorkMode.HYBRID -> "Hybrid"
                                WorkMode.OFFICE -> "Office"
                                WorkMode.FLEXIBLE -> "Flexible"
                            }
                        )
                    }


                    // Modern divider
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Job Details Section
                    SectionHeader(
                        title = "Job Details",
                        icon = Icons.Outlined.Info,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JobDetailsSection(jobPosting)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Job Description Section
                    SectionHeader(
                        title = "Job Description",
                        icon = Icons.Outlined.Description,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HtmlDescriptionText(
                        html = jobPosting.description,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Skills Section
                    SectionHeader(
                        title = "Key Skills",
                        icon = Icons.Outlined.Code,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        jobPosting.mustHaveSkills.forEach { skill ->
                            SkillChip(skill, true)
                        }
                        jobPosting.goodToHaveSkills.forEach { skill ->
                            SkillChip(skill, false)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionHeader(
                        title = "Highlights",
                        icon = Icons.Outlined.Star,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)

                    ) {
                        jobPosting.highlights.forEach { highlight ->
                            HighlightCard(highlight = highlight)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { },
                        shape = CircleShape
                    ) {
                        Text("Apply Job")
                    }
                }
            }
        }
    }

}


@Composable
private fun PosterInfo(
    recruiter: Recruiter
) {

    val context = LocalContext.current

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp)
            ) {
                AsyncImage(
                    ImageRequest.Builder(context)
                        .data(recruiter.profilePicture)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .build(), // Replace with your image resource
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )


            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "${recruiter.firstName} ${recruiter.lastName}",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium

                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when(recruiter.role){
                            Role.RECRUITER -> "Recruiter"
                            Role.HIRING_MANAGER -> "Hiring Manager"
                            Role.TALENT_ACQUISITION -> "Talent Acquisition"
                            Role.HR ->  "Hr"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if(recruiter.isVerified){
                        Image(
                            painter = painterResource(id = R.drawable.ic_verified_service), // Replace with your drawable
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(start = 4.dp)
                        )
                    }

                }
            }
        }
    }

}


@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HtmlDescriptionText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val annotatedString = remember(html) {
        try {
            AnnotatedString.Builder().apply {
                append(
                    HtmlCompat.fromHtml(
                        html,
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    ).trimEnd()
                )
            }.toAnnotatedString()
        } catch (e: Exception) {
            AnnotatedString(html)
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style
    )
}


@Composable
private fun JobDetailsSection(jobPosting: JobPosting) {

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            // Experience
            DetailItem(
                label = "Industry",
                value = jobPosting.industryType,
                icon = Icons.Outlined.Workspaces
            )

            // Experience
            DetailItem(
                label = "Experience",
                value = when (jobPosting.experienceType) {
                    ExperienceType.FIXED -> "${jobPosting.experienceFixed} years"
                    ExperienceType.MIN_MAX -> "${jobPosting.experienceRangeMin}-${jobPosting.experienceRangeMax} years"
                    ExperienceType.FRESHER -> "Fresher"
                },
                icon = Icons.Outlined.WorkOutline
            )

            // Salary
            val salaryText = when {
                jobPosting.salaryNotDisclosed -> "Not Disclosed"
                jobPosting.salaryMinFormatted != null && jobPosting.salaryMaxFormatted != null ->
                    "${jobPosting.salaryMinFormatted} - ${jobPosting.salaryMaxFormatted}"

                else -> null
            }
            salaryText?.let {
                DetailItem(
                    label = "Salary",
                    value = it,
                    icon = Icons.Outlined.AttachMoney
                )
            }

            // Employment Type
            DetailItem(
                label = "Employment Type",
                value = when (jobPosting.employmentType) {
                    EmploymentType.FULL_TIME -> "Full Time"
                    EmploymentType.PART_TIME -> "Part Time"
                    EmploymentType.INTERNSHIP -> "Internship"
                    EmploymentType.CONTRACT -> "Contract"
                },
                icon = Icons.Outlined.Schedule
            )

            // Education
            DetailItem(
                label = "Education",
                value = jobPosting.education,
                icon = Icons.Outlined.School
            )

            // Vacancies
            DetailItem(
                label = "Vacancies",
                value = jobPosting.vacancies.toString(),
                icon = Icons.Outlined.PeopleAlt
            )
        }
    }
}


@Composable
private fun DetailItem(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Image(
                    imageVector = it,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        )
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(top = 4.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SkillChip(skill: String, isMustHave: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isMustHave) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = skill,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isMustHave) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}


@Composable
private fun HighlightCard(
    highlight: HighLightType,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .wrapContentWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = highlight.emoji,
                style = MaterialTheme.typography.headlineSmall
            )


            Column {
                Text(
                    text = highlight.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = highlight.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

