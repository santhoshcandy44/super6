package com.lts360.compose.ui.services.manage


import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePublishedServicesScreen(
    onNavigateUpManageServiceInfo: () -> Unit,
    onNavigateUpManageServiceThumbnail: () -> Unit,
    onNavigateUpManageServiceImages: () -> Unit,
    onNavigateUpManageServicePlans: () -> Unit,
    onNavigateUpManageServiceLocation: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: PublishedServicesViewModel
) {

    val userId = viewModel.userId

    val editableService by viewModel.selectedService.collectAsState()

    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()

    val sheetState = rememberModalBottomSheetState()

    var bottomSheetState by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState)
            sheetState.expand()
        else
            sheetState.hide()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
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
                            text = "Manage Published Service",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        ) { contentPadding ->


            ManagePublishedServicesOptions(
                onManageServiceInfoClick = {

                    editableService?.let {
                        viewModel.loadManageServiceInfoDetails(it)
                        onNavigateUpManageServiceInfo()
                    }

                },
                onManageServiceThumbnailClick = {
                    editableService?.let {
                        onNavigateUpManageServiceThumbnail()
                    }

                },
                onManageServiceImagesClick = {
                    editableService?.let {
                        viewModel.loadManageImages(it)
                        onNavigateUpManageServiceImages()

                    }

                },
                onManageServicePlansClick = {
                    editableService?.let {
                        viewModel.loadManageServicePlans(it)
                        onNavigateUpManageServicePlans()
                    }

                },
                onManageServiceLocationClick = {
                    editableService?.let {
                        viewModel.loadManageLocationDetails(it)
                        onNavigateUpManageServiceLocation()
                    }

                },
                onDeleteServiceClick = {
                    bottomSheetState = true
                },
                modifier = Modifier.padding(contentPadding)
            )

            if (bottomSheetState) {

                ModalBottomSheet(
                    modifier = Modifier
                        .padding(16.dp)
                        .safeDrawingPadding(),
                    dragHandle = null,
                    onDismissRequest = {
                        bottomSheetState = false
                    },
                    sheetState = sheetState,
                    shape = RoundedCornerShape(16.dp)
                ) {


                    DeleteInfoBottomSheet(
                        "Are you sure you want to delete this service? This action cannot be undone.",
                        {

                            editableService?.let { editableServiceNonNull ->
                                viewModel.onDeleteService(userId, editableService!!.serviceId, {
                                    viewModel.removeSelectedService(editableServiceNonNull.serviceId)
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    onPopBackStack()

                                }) {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            bottomSheetState = false

                        }) {
                        bottomSheetState = false
                    }

                }
            }
        }

        if (isLoading) {
            LoadingDialog()
        }
    }

}


@Composable
private fun ManagePublishedServicesOptions(
    onManageServiceInfoClick: () -> Unit,
    onManageServiceThumbnailClick: () -> Unit,

    onManageServiceImagesClick: () -> Unit,
    onManageServicePlansClick: () -> Unit,
    onManageServiceLocationClick: () -> Unit,
    onDeleteServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {


        ServiceOptionCard(
            title = "Service Info",
            description = "Title, Short Description, Long Description and more.",
            icon = R.drawable.ic_manage_service_info,
            cardBackgroundColor = MaterialTheme.colorScheme.secondary, // Replace with your color

            onClick = onManageServiceInfoClick
        )
        Spacer(modifier = Modifier.height(8.dp))


        ServiceOptionCard(
            title = "Service Thumbnail",
            description = "Manage thumbnail of the associated service.",
            icon = R.drawable.ic_thumbnail,
            cardBackgroundColor = MaterialTheme.colorScheme.secondary,
            onClick = onManageServiceThumbnailClick
        )

        Spacer(modifier = Modifier.height(8.dp))


        ServiceOptionCard(
            title = "Service Images",
            description = "Manage all images of the associated service.",
            icon = R.drawable.ic_manage_service_images,
            cardBackgroundColor = MaterialTheme.colorScheme.secondary,

            onClick = onManageServiceImagesClick
        )
        Spacer(modifier = Modifier.height(8.dp))

        ServiceOptionCard(
            title = "Service Plans and Features",
            description = "Manage service plans and features.",
            icon = R.drawable.ic_manage_service_plan_and_features,
            cardBackgroundColor = MaterialTheme.colorScheme.secondary,

            onClick = onManageServicePlansClick
        )
        Spacer(modifier = Modifier.height(8.dp))

        ServiceOptionCard(
            title = "Service Location Info",
            description = "Manage service location info.",
            icon = R.drawable.ic_light_location,
            cardBackgroundColor = MaterialTheme.colorScheme.secondary,

            onClick = onManageServiceLocationClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Delete button
        Button(
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = onDeleteServiceClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Delete Service",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ServiceOptionCard(
    title: String,
    description: String,
    @DrawableRes icon: Int,
    cardBackgroundColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = dropUnlessResumed {
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
fun DeleteInfoBottomSheet(
    message: String,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(24.dp)
    ) {

        Text(
            text = message,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = onProceed,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }


            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(8.dp)

            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
