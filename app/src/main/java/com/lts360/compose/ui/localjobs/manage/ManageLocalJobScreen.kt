package com.lts360.compose.ui.localjobs.manage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import com.lts360.compose.ui.localjobs.manage.viewmodels.PublishedLocalJobViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lts360.compose.ui.common.ProfileNotCompletedPromptSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLocalJobScreen(
    navController: NavController,
    viewModel: PublishedLocalJobViewModel,
    onAddNewLocalJobClick: () -> Unit,
    onNavigateManagePublishedLocalJob: () -> Unit,
    onNavigatePublishedLocalJobViewApplicants: () -> Unit,
    onNavigateProfileSettings:()->Unit,
    onPopBackStack: () -> Unit
) {

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val isLocalJobCreated = savedStateHandle?.get<Boolean>("is_local_job_created")


    val isProfileCompleted by viewModel.isProfileCompletedFlow.collectAsState(initial = false)
    val unCompletedProfileFieldsFlow by viewModel.unCompletedProfileFieldsFlow.collectAsState(initial = listOf("EMAIL","PHONE"))

    var isShowingProfileNotCompletedBottomSheet by remember { mutableStateOf(false) }

    Scaffold(topBar = {
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
                    text = "Manage Local Job",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        )
    }) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {

            Text(
                text = "Create Local Job",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick =
                    if (isProfileCompleted) {
                        dropUnlessResumed {
                            onAddNewLocalJobClick()
                        }
                    } else {
                        {
                            isShowingProfileNotCompletedBottomSheet = true
                        }
                    },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RectangleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Local Job",
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
                Text(text = "Add New")
            }


            Spacer(modifier = Modifier.height(8.dp))


            PublishedLocalJobScreen(
                isLocalJobCreated = isLocalJobCreated,
                onRemoveLocalJobCreatedSavedState = {
                    isLocalJobCreated?.let {
                        savedStateHandle.remove<String>("is_local_job_created")
                    }
                },
                onNavigateUpManagePublishedLocalJob = onNavigateManagePublishedLocalJob,
                onNavigateUpPublishedLocalJobViewApplicants = onNavigatePublishedLocalJobViewApplicants,
                viewModel = viewModel
            )

            if (isShowingProfileNotCompletedBottomSheet) {
                ProfileNotCompletedPromptSheet(
                    unCompletedProfileFields = unCompletedProfileFieldsFlow,
                    onProfileCompleteClick = onNavigateProfileSettings,
                    onDismiss = {
                    isShowingProfileNotCompletedBottomSheet = false
                })
            }

        }
    }

}