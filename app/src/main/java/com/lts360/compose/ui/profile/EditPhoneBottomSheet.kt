package com.lts360.compose.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lts360.R
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.test.Country
import com.lts360.test.PhoneNumberInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPhoneBottomSheet(
    onVerifyClick: () -> Unit,
    onDismiss:()->Unit,
    modifier: Modifier = Modifier,
) {

    val sheetState = rememberModalBottomSheetState()

    val countries = listOf(
        Country("India", "\uD83C\uDDEE\uD83C\uDDF3", "+91")
    )


    var selectedCountry by remember {
        mutableStateOf<Country>(countries.find { it.code == "+91" }
            ?: throw IllegalStateException("Default country is not valid"))
    }

    var phoneNumber by remember { mutableStateOf("") }

    var showCountryPicker by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var route by remember { mutableStateOf("phone") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if(route=="verify_otp"){
                IconButton(onClick = {
                    route = "phone"
                }, modifier = Modifier.align(Alignment.Start)) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                }
            }

            Image(
                painterResource(R.drawable.ic_verify_phone),
                contentDescription = "Phone Icon",
                modifier = Modifier
                    .size(120.dp)
            )

            Spacer(Modifier.height(8.dp))

            when (route) {
                "phone" -> {

                    Text(
                        text = "Verify your phone number",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PhoneNumberInput(
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = {
                            phoneNumber = it
                        },
                        selectedCountry = selectedCountry,
                        onChooseCountry = {
                            if (isLoading) return@PhoneNumberInput
                            showCountryPicker = true
                        },
                        readOnly = isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            route = "verify_otp"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        if (isLoading) {
                            CircularProgressIndicatorLegacy(
                                modifier = Modifier
                                    .size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Send OTP", color = Color.White)
                        }
                    }
                }

                "verify_otp" -> {

                    Text(
                        text = "Enter OTP sent to $phoneNumber number",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var otp by remember { mutableStateOf("") }

                    TextField(
                        value = otp,
                        onValueChange = {

                            if (it.all { it.isDigit() } && it.length <= 6) {
                                otp = it
                            }

                        }, placeholder = {
                            Text("Enter OTP")
                        }, keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),

                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = MaterialTheme.customColorScheme.grayVariant,
                            focusedContainerColor = MaterialTheme.customColorScheme.grayVariant,
                            unfocusedContainerColor = MaterialTheme.customColorScheme.grayVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = 40.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onVerifyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        enabled = otp.length == 6,
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        if (isLoading) {
                            CircularProgressIndicatorLegacy(
                                modifier = Modifier
                                    .size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Verify OTP", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showCountryPicker) {
        CountrySelectionDialog(
            countries = countries, onCountrySelected = {
                selectedCountry = it
                showCountryPicker = false
            }, onDismiss = { showCountryPicker = false },
            enabled = !isLoading
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountrySelectionDialog(
    countries: List<Country>,
    onCountrySelected: (Country) -> Unit,
    onDismiss: () -> Unit,
    enabled: Boolean = true
) {
    BasicAlertDialog(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        onDismissRequest = onDismiss,
    ) {
        Surface {
            Column(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Choose Country", style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    items(countries) { country ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = enabled) {
                                    onCountrySelected(country)
                                },
                            headlineContent = {
                                Text(text = "${country.name} (${country.code})")
                            },
                            leadingContent = {
                                Text(
                                    text = country.flag,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}