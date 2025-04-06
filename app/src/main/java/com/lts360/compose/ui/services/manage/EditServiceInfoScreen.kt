package com.lts360.compose.ui.services.manage


import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.lts360.compose.transformations.PlaceholderTransformation
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceInfoScreen(
    onPopBackStack: () -> Unit,
    viewModel: PublishedServicesViewModel
) {


    val userId = viewModel.userId

    val isUpdating by viewModel.isUpdating.collectAsState()

    val selectedService by viewModel.selectedService.collectAsState()


    val serviceTitle by viewModel.serviceTitle.collectAsState()
    val serviceShortDescription by viewModel.shortDescription.collectAsState()
    val serviceLongDescription by viewModel.longDescription.collectAsState()
    val selectedIndustry by viewModel.selectedIndustry.collectAsState()
    /*
        val selectedCountry by viewModel.selectedCountry.collectAsState()
    */

    val serviceTitleError by viewModel.serviceTitleError.collectAsState()
    val serviceShortDescriptionError by viewModel.shortDescriptionError.collectAsState()
    val serviceLongDescriptionError by viewModel.longDescriptionError.collectAsState()
    val selectedIndustryError by viewModel.selectedIndustryError.collectAsState()
    /*
        val selectedCountryError by viewModel.selectedCountryError.collectAsState()
    */


    val context = LocalContext.current


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
                            text = "Edit Service Info",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            },
            modifier = Modifier
                .fillMaxSize()
        ) { contentPadding ->
            // Toolbar

            // Main content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {

                item {

                    // Text Field for Service Title
                    OutlinedTextField(
                        isError = serviceTitleError != null || serviceTitle.length > 100,
                        value = serviceTitle,
                        onValueChange = { viewModel.updateServiceTitle(it) },
                        label = { Text("Service Title") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        visualTransformation = if (serviceTitle.isEmpty())
                            PlaceholderTransformation("") else VisualTransformation.None
                    )

                    if (serviceTitle.length > 100) {
                        Text(
                            text = "Limit: ${serviceTitle.length}/${100}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 4.dp
                            ) // Adjust padding as needed
                        )
                    }


                    serviceTitleError?.let {
                        ErrorText(it)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text Field for Short Description
                    OutlinedTextField(
                        isError = serviceShortDescriptionError != null || serviceShortDescription.length > 250,
                        value = serviceShortDescription,
                        onValueChange = { viewModel.updateShortDescription(it) },
                        label = { Text("Service Short Description") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        maxLines = 5,
                        visualTransformation = if (serviceShortDescription.isEmpty())
                            PlaceholderTransformation("") else VisualTransformation.None
                    )

                    if (serviceShortDescription.length > 250) {
                        Text(
                            text = "Limit: ${serviceShortDescription.length}/${250}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 4.dp
                            ) // Adjust padding as needed
                        )
                    }


                    serviceShortDescriptionError?.let {
                        ErrorText(it)
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    ExposedDropdownIndustry(selectedIndustry, selectedIndustryError != null) {
                        viewModel.updateServiceIndustry(it.value)
                    }
                    selectedIndustryError?.let {
                        ErrorText(it)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text Field for Long Description
                    OutlinedTextField(
                        isError = serviceLongDescriptionError != null || serviceShortDescription.length > 5000,
                        value = serviceLongDescription,
                        onValueChange = { viewModel.updateLongDescription(it) },
                        label = { Text("Service Long Description") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        minLines = 6,
                        visualTransformation = if (serviceLongDescription.isEmpty())
                            PlaceholderTransformation("") else VisualTransformation.None
                    )

                    if (serviceLongDescription.length > 5000) {
                        Text(
                            text = "Limit: ${serviceLongDescription.length}/${5000}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 4.dp
                            ) // Adjust padding as needed
                        )
                    }

                    serviceLongDescriptionError?.let {
                        ErrorText(it)
                    }

                    /*
                     Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownCountry(selectedCountry,selectedCountryError!=null) {
                             viewModel.updateServiceCountry(it.value)
                         }*/

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            selectedService?.let { service ->

                                if (viewModel.validateServiceInfoAll()) {
                                    viewModel.onUpdateServiceInfo(
                                        userId,
                                        service.serviceId,
                                        serviceTitle,
                                        serviceShortDescription,
                                        serviceLongDescription,
                                        selectedIndustry, {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    ) {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        },

                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Update Service Info",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

        }

        if (isUpdating) {
            LoadingDialog()
        }
    }

}