package com.lts360.test

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSearchScreenWithSmartDropdowns(
    onSearchClick: (role: String, location: String) -> Unit,
    onPopUp:()-> Unit
) {

    val allJobRoles = listOf(
        "Software Engineer",
        "Product Manager",
        "Data Scientist",
        "UX Designer",
        "DevOps Engineer",
        "Mobile Developer",
        "QA Engineer"
    )

    val allLocations = listOf(
        "New York, NY",
        "San Francisco, CA",
        "Austin, TX",
        "Seattle, WA",
        "Chicago, IL",
        "Boston, MA",
        "Remote"
    )

    var selectedRole by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("") }


    var role by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var roleExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    val filteredRoles = remember(role, allJobRoles) {
        allJobRoles.filter {
            it.contains(role, ignoreCase = true)
        }
    }

    val filteredLocations = remember(location, allLocations) {
        allLocations.filter {
            it.contains(location, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                IconButton(onPopUp) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                }
            })
        }
    ) { contentPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            Text(
                text = "Find Your Job",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = roleExpanded && role.isNotEmpty(),
                onExpandedChange = { roleExpanded = it }
            ) {
                CustomTextField(
                    value = role,
                    onValueChange = {
                        selectedRole = ""
                        role = it
                        roleExpanded = true
                    },
                    label = "Job Role",
                    minHeight = 32.dp,
                    leadingIcon = { Icon(Icons.Default.Work, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .onFocusChanged { focusState ->
                            roleExpanded = focusState.isFocused
                        },
                )

                if (filteredRoles.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = roleExpanded && role.isNotEmpty(),
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        filteredRoles.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    role = selectionOption
                                    selectedRole = selectionOption
                                    roleExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }

            // Location Dropdown
            ExposedDropdownMenuBox(
                expanded = locationExpanded && location.isNotEmpty(),
                onExpandedChange = { locationExpanded = it }
            ) {
                CustomTextField(
                    value = location,
                    onValueChange = {
                        selectedLocation = ""
                        location = it
                        locationExpanded = true

                    },
                    label = "Location",
                    minHeight = 32.dp,
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .onFocusChanged { focusState ->
                            locationExpanded = focusState.isFocused
                        },
                )

                if (filteredLocations.isNotEmpty()) {


                    ExposedDropdownMenu(
                        expanded = locationExpanded && location.isNotEmpty(),
                        onDismissRequest = { locationExpanded = false },
                    ) {
                        filteredLocations.forEach { selectionOption ->

                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    location = selectionOption
                                    selectedLocation = selectionOption
                                    locationExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.weight(1f))

            // Apply Button at Bottom
            Button(
                onClick = { onSearchClick(role, location) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = selectedRole.isNotBlank() && selectedLocation.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Apply Now")
            }
        }
    }

}

