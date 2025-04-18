package com.lts360.compose.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.compose.ui.theme.viewmodels.ThemeMode
import com.lts360.compose.ui.theme.viewmodels.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(viewModel: ThemeViewModel = hiltViewModel(),
    onPopBackStack:()-> Unit) {

    val themeMode by viewModel.themeMode.collectAsState()

    var useSystemDefault by remember(themeMode) { mutableStateOf(themeMode == -1) }
    val isDarkMode by remember(themeMode) { mutableStateOf(themeMode == 1) }


    Scaffold( topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed {
                        onPopBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }

                },
                title = {
                Text(
                    text = "Theme Settings",
                    style = MaterialTheme.typography.titleMedium
                )
            })
        },

        content = { paddingValues ->

            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp), // Remove rounded corners
                elevation = CardDefaults.cardElevation(2.dp)
            ){
                Column(modifier = Modifier
                    .fillMaxWidth()
                ) {

                    // System Default Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween
                    ) {

                        Text("Use System Default", fontSize = 16.sp)

                        Switch(
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, // Thumb color when ON
                                checkedTrackColor = Color(0xFF00C04D), // Track background when ON
                                uncheckedThumbColor = Color.White, // Thumb color when OFF
                                uncheckedTrackColor = Color(0xFFBBC8D4), // Track background when OFF
                                checkedBorderColor = Color.Transparent,
                                uncheckedBorderColor = Color.Transparent
                            ),

                            checked = useSystemDefault,
                            onCheckedChange = { isChecked ->
                                useSystemDefault = isChecked
                                viewModel.setThemeMode(
                                    if (isChecked) ThemeMode.SystemDefault else if (isDarkMode) ThemeMode.Dark
                                    else ThemeMode.Light
                                )
                            }
                        )

                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)

                        Switch(
                            enabled = !useSystemDefault,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, // Thumb color when ON
                                checkedTrackColor = Color(0xFF00C04D), // Track background when ON
                                uncheckedThumbColor = Color.White, // Thumb color when OFF
                                uncheckedTrackColor = Color(0xFFBBC8D4), // Track background when OFF
                                checkedBorderColor = Color.Transparent,
                                uncheckedBorderColor = Color.Transparent
                            ),

                            checked = isDarkMode,
                            onCheckedChange = { isChecked ->
                                val newMode = if (isChecked) ThemeMode.Dark else ThemeMode.Light
                                viewModel.setThemeMode(newMode)
                            }
                        )
                    }

                }
            }


        })
}
