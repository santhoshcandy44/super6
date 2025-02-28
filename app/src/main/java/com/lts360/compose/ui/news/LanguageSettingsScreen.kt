package com.lts360.compose.ui.news

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(viewModel: LanguageViewModel = hiltViewModel()) {

/*
    val context = LocalContext.current
*/

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val languages = listOf(
        "en" to Pair("English", "English"),
        "hi" to Pair("हिन्दी", "Hindi"),
        "ta" to Pair("தமிழ்", "Tamil"),
        "te" to Pair("తెలుగు", "Telugu"),
        "bn" to Pair("বাংলা", "Bengali"),
        "mr" to Pair("मराठी", "Marathi"),
        "gu" to Pair("ગુજરાતી", "Gujarati"),
        "kn" to Pair("ಕನ್ನಡ", "Kannada")
    )

    val selectedLangItem = languages.find { it.first == selectedLanguage }


    Scaffold(

        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(
                        text = "News Language Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),

            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            // Show "Selected Language" label + selected language at top
            selectedLangItem?.let { (code, language) ->
                item {
                    Text(
                        text = "Selected Language",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(
                                0xFFFF5733
                            ),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(code)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Image(
                                imageVector = Icons.Default.Check, contentDescription = null,
                                colorFilter =  ColorFilter.tint(
                                    Color.White
                                )
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(language.first)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(language.second, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }


            item {
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }


            items(languages) { (code, language) ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(code)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Image(
                            imageVector = Icons.Default.Check, contentDescription = null,
                            colorFilter = if (code == selectedLanguage) ColorFilter.tint(
                                MaterialTheme.colorScheme.onSurface
                            )
                            else ColorFilter.tint(
                                Color.Transparent
                            ),
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(language.first)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(language.second, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class LanguageViewModel @Inject constructor(private val languageSettingsDataStore: LanguageDataStore) :
    ViewModel() {

    // Collecting the selected language from DataStore
    val selectedLanguage = languageSettingsDataStore.languageFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "en" // Default language
    )

    // Function to change the language
    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            languageSettingsDataStore.saveNewsLanguage(langCode)
        }
    }
}


// Extension function to create DataStore
private val Context.languageSettingsDataStore by preferencesDataStore(name = "language_settings")

@Singleton
class LanguageDataStore @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.languageSettingsDataStore

    // Flow to read the selected language
    val languageFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SELECTED_LANGUAGE] ?: "en"
    }

    // Function to save the selected language
    suspend fun saveNewsLanguage(languageCode: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_LANGUAGE] = languageCode
        }
    }

    private object PreferencesKeys {
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_news_language")
    }
}

