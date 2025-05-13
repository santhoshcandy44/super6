package com.lts360.test


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lts360.compose.noRippleClickable
import com.lts360.compose.ui.auth.CustomNumberInput
import com.lts360.compose.ui.auth.CustomPinInput
import com.lts360.compose.ui.theme.customColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryBottomSheetScreen() {

    val countries = listOf(
        Country("India", "\uD83C\uDDEE\uD83C\uDDF3", "+91")
    )

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    val coroutineScope = rememberCoroutineScope()
    var selectedCountry by remember {
        mutableStateOf<Country>(countries.find { it.code == "+91" }
            ?: throw IllegalStateException("Default country is not valid"))
    }

    var phoneNumber by remember { mutableStateOf("") }

    BottomSheetScaffold(
        sheetContent = {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxHeight()
            ) {

                item {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text("Choose country", style = MaterialTheme.typography.titleLarge)


                        IconButton(
                            {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                }
                            }

                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                            )
                        }

                    }
                }

                items(countries) { country ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCountry = country
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.hide()
                                }
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
        },
        sheetPeekHeight = 0.dp,
        scaffoldState = bottomSheetScaffoldState,
        sheetDragHandle = null,
        sheetShape = RectangleShape,
        topBar = {
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp)

            ) {
                Text(
                    "Log in to Super6",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Login or Create account by validating your phone number.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Enter Phone Number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))


                PhoneNumberInput(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = {
                        phoneNumber = it
                    },
                    selectedCountry = selectedCountry,
                    onChooseCountry = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
                )


                Spacer(modifier = Modifier.height(24.dp))

                Button({}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Text("Continue")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // agreement
                Text(buildAnnotatedString {
                    append("By checking agree to ")

                    withLink(
                        LinkAnnotation.Url(
                            url = "https://saket.me/compose-custom-text-spans/",
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.customColorScheme.linkColor,
                                    textDecoration = TextDecoration.None
                                )
                            )

                        ) {


                        }
                    ) {
                        append("Terms and Conditions")
                    }

                    append("& ")


                    withLink(
                        LinkAnnotation.Url(
                            url = "https://saket.me/compose-custom-text-spans/",
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.customColorScheme.linkColor,
                                    textDecoration = TextDecoration.None
                                )
                            )

                        ) {


                        }
                    ) {
                        append("Privacy Policy")
                    }
                    append(".")


                }, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Text("Or")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Email Log In", modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {

                    })


            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Blue)
                    .wrapContentHeight()

            ) {


                CustomNumberInput(onDigitClick = { digit ->
                    if (phoneNumber.length < 10 && digit.all { it.isDigit() }) {
                        phoneNumber += digit
                    }
                }, onBackspaceClick = {
                    phoneNumber = phoneNumber.dropLast(1)

                }, onForwardClick = {


                }, onBackwardClick = {

                })

            }


        }
    }
}


@Composable
fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onChooseCountry: () -> Unit,
    selectedCountry: Country,
    modifier: Modifier = Modifier,
    readOnly: Boolean=false
) {
    BasicTextField(
        value = phoneNumber,
        onValueChange = { phoneNumberText ->
            if (phoneNumberText.length <= 10 && phoneNumberText.all { it.isDigit() }) {
                onPhoneNumberChange(phoneNumberText)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.customColorScheme.searchBarColor,
                RoundedCornerShape(8.dp)
            )
            .heightIn(min = 40.dp)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        textStyle =LocalTextStyle.current.copy(
            fontSize = 18.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            color = MaterialTheme.colorScheme.onSurface,
            ),
        singleLine = true,
        readOnly = readOnly,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .noRippleClickable(onClick = onChooseCountry),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCountry.flag,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 24.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = selectedCountry.code,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 24.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(contentAlignment = Alignment.CenterStart) {
                    if (phoneNumber.isEmpty()) {
                        Text(
                            "Phone Number",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    it()
                }
            }
        }
    )
}


data class Country(val name: String, val flag: String, val code: String)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinVerificationAppOpenScreen() {

    val coroutineScope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var currentLoadingIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isLoading) {
        while (isLoading) {
            currentLoadingIndex = (currentLoadingIndex + 1) % 4
            delay(100L)
        }
        currentLoadingIndex = 0
    }


    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(pin) {
        isLoading = false
        var isWrong = (pin == "1220")
        if (pin.length == 4 && !isWrong) {
            coroutineScope.launch {
                shakeOffset.animateTo(
                    targetValue = 32f,
                    animationSpec = repeatable(
                        iterations = 3,
                        animation = tween(durationMillis = 100, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                shakeOffset.snapTo(0f)
                isWrong = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)

        ) {
            Text(
                "Enter Super6 PIN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Login or Create account by validating your phone number.",
                style = MaterialTheme.typography.bodyMedium
            )


            Spacer(modifier = Modifier.height(24.dp))



            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) } // Apply shake offset

            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 2.dp,
                                color = Color.LightGray,
                                shape = CircleShape
                            )
                            .background(
                                color = if (isLoading && currentLoadingIndex == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    if (index < pin.length) Color.LightGray else Color.Transparent
                                },
                                shape = CircleShape
                            )
                    )
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            Text("Invalid Pin", color = Color.Red)


        }


        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Forgot Pin")

            Box(
                modifier = Modifier
                    .fillMaxWidth() // Fill the width
                    .wrapContentHeight() // Wrap content height


            ) {


                CustomPinInput(onDigitClick = { digit ->
                    if (pin.length < 4 && digit.all { it.isDigit() }) {
                        pin += digit
                    }
                }, onBackspaceClick = {
                    pin = pin.dropLast(1)

                }, onForwardClick = {


                }, onBackwardClick = {

                })

            }
        }

    }
}
