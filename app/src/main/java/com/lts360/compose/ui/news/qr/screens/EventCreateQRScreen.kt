package com.lts360.compose.ui.news.qr.screens
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lts360.compose.ui.news.qr.viewmodels.EventCreateQRViewModel
import com.lts360.compose.ui.theme.customColorScheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreateQRScreen(onPopBackStack:()-> Unit) {

    val viewModel: EventCreateQRViewModel = koinViewModel()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val title by viewModel.title.collectAsState()
    val organizer by viewModel.organizer.collectAsState()
    val location by viewModel.location.collectAsState()
    val description by viewModel.description.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeEventBitmap.collectAsState()

    val context = LocalContext.current



    var datePickerDialogState by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var dateType by remember { mutableStateOf("") }

    // Function to show the date picker
    val onDateSelected: (String) -> Unit = { selectedDate ->
        if(dateType=="Start"){
            viewModel.updateStartDate(selectedDate)
        }
        if(dateType=="End"){
            viewModel.updateEndDate(selectedDate)
        }
    }

    // Launch Date Picker dialog when the field is clicked
    if (datePickerDialogState) {
        DatePickerDialog(
            onDismissRequest = { datePickerDialogState = false },
            confirmButton = {
                TextButton(onClick = {
                    // Fetch the date and close dialog
                    val selectedDate = DatePickerDefaults.dateFormatter().formatDate(datePickerState.selectedDateMillis, Locale.getDefault())
                    selectedDate?.let {
                        onDateSelected(selectedDate)
                        datePickerDialogState = false
                    }

                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerDialogState = false }) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            content = {


                // Use the DatePicker composable to display the calendar view
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth(),

                    dateFormatter = DatePickerDefaults.dateFormatter(),
                    colors = DatePickerDefaults.colors(),
                    showModeToggle=false,
                    title = null,
                    headline = null
                )
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Event QR Code", // Set your title text here
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },

                navigationIcon = {
                    IconButton(onClick = {
                        onPopBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.wrapContentSize()) {

                    // Start Date field
                    Text("Start Date", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        readOnly = true,
                        value = startDate,
                        onValueChange = { viewModel.updateStartDate(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                            .clickable {
                                dateType="Start"
                                // Open Date Picker Dialog
                                datePickerDialogState = true
                            }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically){
                            Box(Modifier.fillMaxWidth()
                                .weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (startDate.isEmpty()) {
                                    Text("Enter start date", style = MaterialTheme.typography.bodySmall)
                                }
                                it()
                            }
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pick start date",
                                modifier = Modifier.size(24.dp)
                                    .clickable {
                                        dateType="Start"
                                        // Open Date Picker Dialog
                                        datePickerDialogState = true
                                    }
                            )
                        }

                    }

                    Spacer(Modifier.height(8.dp))

                    // End Date field
                    Text("End Date", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        readOnly = true,
                        value = endDate,
                        onValueChange = { viewModel.updateEndDate(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                            .clickable {
                                dateType="End"
                                // Open Date Picker Dialog
                                datePickerDialogState = true
                            }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically){
                            Box(Modifier.fillMaxWidth()
                                .weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (endDate.isEmpty()) {
                                    Text("Enter end date", style = MaterialTheme.typography.bodySmall)
                                }
                                it()
                            }
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pick end date",
                                modifier = Modifier.size(24.dp)
                                    .clickable {
                                        dateType="End"
                                        // Open Date Picker Dialog
                                        datePickerDialogState = true
                                    }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Title field
                    Text("Event Title", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        value = title,
                        onValueChange = { viewModel.updateTitle(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (title.isEmpty()) {
                                Text("Enter event title", style = MaterialTheme.typography.bodySmall)
                            }
                            it()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Organizer field
                    Text("Organizer", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        value = organizer,
                        onValueChange = { viewModel.updateOrganizer(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (organizer.isEmpty()) {
                                Text("Enter organizer name", style = MaterialTheme.typography.bodySmall)
                            }
                            it()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Location field
                    Text("Location", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        value = location,
                        onValueChange = { viewModel.updateLocation(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (location.isEmpty()) {
                                Text("Enter event location", style = MaterialTheme.typography.bodySmall)
                            }
                            it()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Description field
                    Text("Description", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        value = description,
                        onValueChange = { viewModel.updateDescription(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (description.isEmpty()) {
                                Text("Enter event description", style = MaterialTheme.typography.bodySmall)
                            }
                            it()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Latitude field
                    Text("Latitude", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        value = latitude,
                        onValueChange = { viewModel.updateLatitude(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (latitude.isEmpty()) {
                                Text("Enter latitude", style = MaterialTheme.typography.bodySmall)
                            }
                            it()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Longitude field
                    Text("Longitude", style = MaterialTheme.typography.bodyMedium)
                    BasicTextField(
                        value = longitude,
                        onValueChange = { viewModel.updateLongitude(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.customColorScheme.searchBarColor, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .height(32.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (longitude.isEmpty()) {
                                Text("Enter longitude", style = MaterialTheme.typography.bodySmall)
                            }
                            it()
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Button to generate the QR code for the event
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.generateQrCodeForEvent(
                                startDate, endDate, title, organizer, location, description,
                                latitude, longitude,
                                {
                                    // Success callback
                                    Toast.makeText(context, "QR Code Generated!", Toast.LENGTH_SHORT).show()
                                }
                            ){
                                // Error callback
                                Toast.makeText(context, "Error generating QR Code.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Generate Event QR Code")
                    }
                }

             
            }
        }
    )
}
