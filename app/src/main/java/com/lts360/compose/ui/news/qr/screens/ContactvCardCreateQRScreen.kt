package com.lts360.compose.ui.news.qr.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.compose.ui.news.qr.viewmodels.ContactCreateQRViewModel
import com.lts360.compose.ui.theme.customColorScheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactVcardCreateQRScreen(onPopBackStack:()-> Unit) {

    val viewModel: ContactCreateQRViewModel = koinViewModel()
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val mobile by viewModel.mobile.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val fax by viewModel.fax.collectAsState()
    val email by viewModel.email.collectAsState()
    val company by viewModel.company.collectAsState()
    val job by viewModel.job.collectAsState()
    val street by viewModel.street.collectAsState()
    val city by viewModel.city.collectAsState()
    val zip by viewModel.zip.collectAsState()
    val state by viewModel.state.collectAsState()
    val country by viewModel.country.collectAsState()
    val website by viewModel.website.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contact vCard", // Set your title text here
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },

                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed {  }) {
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
                // Contact Details Input Fields
                Column(modifier = Modifier.wrapContentSize()
                    .verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            Icons.Default.ContactPhone,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface))
                        Text("Add vCard to QR code", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    // First Name
                    Text("First Name", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = firstName,
                        onValueChange = { viewModel.updateFirstName(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (firstName.isEmpty()) {
                                Text("Enter first name", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Last Name
                    Text("Last Name", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = lastName,
                        onValueChange = { viewModel.updateLastName(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (lastName.isEmpty()) {
                                Text("Enter last name", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Mobile Phone
                    Text("Mobile", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = mobile,
                        onValueChange = { viewModel.updateMobile(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (mobile.isEmpty()) {
                                Text("Enter mobile number", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Phone
                    Text("Phone", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = phone,
                        onValueChange = { viewModel.updatePhone(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (phone.isEmpty()) {
                                Text("Enter phone number", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Fax
                    Text("Fax", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = fax,
                        onValueChange = { viewModel.updateFax(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (fax.isEmpty()) {
                                Text("Enter fax number", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Email
                    Text("Email", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = email,
                        onValueChange = { viewModel.updateEmail(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (email.isEmpty()) {
                                Text("Enter email address", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Company
                    Text("Company", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = company,
                        onValueChange = { viewModel.updateCompany(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (company.isEmpty()) {
                                Text("Enter company name", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Job Title
                    Text("Job Title", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = job,
                        onValueChange = { viewModel.updateJob(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (job.isEmpty()) {
                                Text("Enter job title", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Street Address
                    Text("Street Address", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = street,
                        onValueChange = { viewModel.updateStreet(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (street.isEmpty()) {
                                Text("Enter street address", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // City
                    Text("City", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = city,
                        onValueChange = { viewModel.updateCity(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (city.isEmpty()) {
                                Text("Enter city", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ZIP
                    Text("ZIP", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = zip,
                        onValueChange = { viewModel.updateZip(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (zip.isEmpty()) {
                                Text("Enter ZIP", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // State
                    Text("State", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = state,
                        onValueChange = { viewModel.updateState(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (state.isEmpty()) {
                                Text("Enter state", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Country
                    Text("Country", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = country,
                        onValueChange = { viewModel.updateCountry(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (country.isEmpty()) {
                                Text("Enter country", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Website
                    Text("Website", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = website,
                        onValueChange = { viewModel.updateWebsite(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (website.isEmpty()) {
                                Text("Enter website", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Button to generate the QR Code
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            viewModel.generateContactQrCode(
                                firstName, lastName, mobile, phone, fax, email, company,
                                job, street, city, zip, state, country, website,
                                {
                                    // Success callback
                                    Toast.makeText(context, "QR Code Generated!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                // Error callback
                                Toast.makeText(context, "Error generating QR Code.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Generate vCard QR Code")
                    }
                }


            }
        }
    )
}
