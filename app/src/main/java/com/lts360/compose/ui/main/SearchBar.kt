package com.lts360.compose.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lts360.compose.ui.theme.customColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onFocus: (Boolean) -> Unit,
    focusRequesterEnabled: Boolean = true,
    isBackButtonEnabled: Boolean = true,
) {

    // Conditionally create and use FocusRequester
    val focusRequester = remember { FocusRequester() }


    // If focusRequester is enabled, request focus once component is rendered
    if (focusRequesterEnabled) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }


    var recentQuery by remember(query) { mutableStateOf(query) }

    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isBackButtonEnabled) {
            IconButton(modifier = Modifier.padding(0.dp),
                onClick = {
                    onBackButtonClicked()
                    coroutineScope.launch {
                        delay(100) // Optional: Delay to ensure state is collected
                        focusManager.clearFocus() // Clear focus after state update
                    }
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        BasicTextField(
            value = recentQuery,
            onValueChange = { textState ->
                recentQuery = textState
                onQueryChange(recentQuery)
            },
            textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    coroutineScope.launch {
                        onSearch()
                        delay(100) // Optional: Delay to ensure state is collected
                        focusManager.clearFocus() // Clear focus after state update
                    }
                }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.customColorScheme.searchBarColor, CircleShape)
                .padding(8.dp)
                .height(28.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
//                    onFocus(focusState.isFocused)
                },

            ) { innerTextField ->
            Row(modifier, verticalAlignment = Alignment.CenterVertically) {

                IconButton(onClick = {
                    coroutineScope.launch {
                        delay(100) // Optional: Delay to ensure state is collected
                        focusManager.clearFocus() // Clear focus after state update
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {

                    if (recentQuery.text.trim().isEmpty()) {
                        Text(
                            text = "Search",
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        )
                    }


                    innerTextField()
                }


                if (recentQuery.text.trim().isNotEmpty()) {

                    IconButton(onClick = {
                        onClearClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                }


            }
        }

    }
}
