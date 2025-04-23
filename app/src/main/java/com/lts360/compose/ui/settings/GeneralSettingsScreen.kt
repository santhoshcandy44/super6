package com.lts360.compose.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    currentCountry: Country?,
    currentLanguage: Language?,
    onNavigateToTheme: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToCountry: () -> Unit,
    onPopBackStack: () -> Unit
) {
    val settingsItems = remember(currentLanguage, currentCountry) {
        listOf(
            SettingsItem.Header("General Settings"),
            SettingsItem.Option(
                title = "Theme",
                description = "Dark, Light or System default",
                icon = Icons.Filled.Palette,
                iconTint = Color(0xFF6A1B9A),
                cardColor = Color(0xFFEDE7F6),
                onClick = onNavigateToTheme
            ),
            /*      SettingsItem.Header("Regional"),
                     SettingsItem.Option(
                         title = "Language",
                         description = "Current: ${if (currentCountry?.name != null) {
                             currentLanguage?.name ?: "Language Action Required"
                         } else "Country Action Required"
                         }",
                         icon = Icons.Filled.Language,
                         iconTint = Color(0xFFE65100),
                         cardColor = Color(0xFFFFF3E0),
                         onClick = if(currentCountry?.name!=null){
                             onNavigateToLanguage
                         }else{
                             {}
                         }
                     ),
      */
            SettingsItem.Option(
                title = "Country",
                description = "Current: ${currentCountry?.name ?: "Country Action Required"}",
                icon = Icons.Filled.Flag,
                iconTint = Color(0xFF2E7D32),
                cardColor = Color(0xFFE8F5E9),
                onClick = if (currentCountry?.name != null) {
                    onNavigateToCountry
                } else {
                    {}
                }
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(
                        dropUnlessResumed {
                            onPopBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }

    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(settingsItems) { item ->
                when (item) {
                    is SettingsItem.Header -> {
                        Text(
                            text = item.title,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    is SettingsItem.Option -> {
                        SettingsCard(
                            title = item.title,
                            description = item.description,
                            icon = item.icon,
                            iconTint = item.iconTint,
                            cardColor = item.cardColor,
                            onClick = item.onClick
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SettingsCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    cardColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        onClick = dropUnlessResumed {
            onClick()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

        }
    }
}
