package com.lts360.compose.ui.usedproducts.manage

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.api.models.service.EditableUsedProductListing
import com.lts360.compose.ui.common.ProfileNotCompletedPromptSheet
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsedProductListingScreen(
    onAddNewUsedProductListingClick: () -> Unit,
    onNavigateUpManagePublishedUsedProductListing: (EditableUsedProductListing) -> Unit,
    onNavigateProfileSettings:()-> Unit,
    onPopBackStack: () -> Unit,
    viewModel: PublishedUsedProductsListingViewModel
    ) {

    val isUsedProductListingCreated = false

    var isShowingProfileNotCompletedBottomSheet by remember { mutableStateOf(false) }

    val isProfileCompleted by viewModel.isProfileCompletedFlow.collectAsState(initial = false)
    val unCompletedProfileFieldsFlow by viewModel.unCompletedProfileFieldsFlow.collectAsState(
        initial = listOf("EMAIL", "PHONE")
    )

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
                    text = "Manage Second Hands",
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
                text = "Create Second Hands",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick =
                    if (isProfileCompleted) {
                        dropUnlessResumed {
                            onAddNewUsedProductListingClick()
                        }
                    } else {
                        {
                            isShowingProfileNotCompletedBottomSheet = true
                        }
                    },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RectangleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Seconds Hands",
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
                Text(text = "Add New")
            }

            Spacer(modifier = Modifier.height(8.dp))

            PublishedUsedProductListingScreen(
                isUsedProductListingCreated,
                {
                    isUsedProductListingCreated?.let {

                    }
                },
                onNavigateUpManagePublishedUsedProductListing,
                viewModel

            )
        }


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