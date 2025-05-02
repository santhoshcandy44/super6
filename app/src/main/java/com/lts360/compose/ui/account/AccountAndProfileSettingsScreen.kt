package com.lts360.compose.ui.account

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.main.viewmodels.AccountAndProfileSettingsViewModel
import com.lts360.compose.ui.theme.icons


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAndProfileSettingsScreen(
    onChangePasswordNavigateUp: () -> Unit,
    onSelectAccountType: (AccountType) -> Unit,
    onPopStack: () -> Unit,
    viewModel: AccountAndProfileSettingsViewModel = hiltViewModel()) {

    val accountType by viewModel.accountType.collectAsState()

    var bottomSheetState by rememberSaveable { mutableStateOf(false) }


    val sheetState = rememberModalBottomSheetState()

    val userId = viewModel.userId

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = dropUnlessResumed { onPopStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Account & Profile Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                    })
            }
        ) { contentPadding ->
            Surface(
                modifier = Modifier.padding(
                    contentPadding
                )
            ) {

                ProfileManagement(
                    accountType,
                    onChangePasswordNavigateUp,
                    onSelectAccountType
                ) {
                    bottomSheetState = true
                }

                if (bottomSheetState) {
                    ModalBottomSheet(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(16.dp)
                            .safeDrawingPadding(),
                        onDismissRequest = {
                            bottomSheetState = false
                        },
                        sheetState = sheetState,
                        dragHandle = null,
                        shape = RoundedCornerShape(16.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {


                            Box(
                                modifier = Modifier.
                                 align(Alignment.CenterHorizontally)
                                .background(Color(0XFFFDF4F5), CircleShape),

                            ){
                                Image(
                                    painter = painterResource(MaterialTheme.icons.exit),
                                    contentDescription = null,
                                    colorFilter =  ColorFilter.tint(Color.Red) ,
                                    modifier = Modifier.padding(8.dp)
                                        .size(60.dp)

                                )
                            }


                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Are you sure want to log out your account ?"
                            )

                            Spacer(modifier = Modifier.height(32.dp))


                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red, // Set background color to red
                                    contentColor = Color.White // Set text color to black
                                ),
                                onClick = {
                                    bottomSheetState = false
                                    viewModel.onLogout(userId, {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        viewModel.logOutAccount(context)
                                    }) {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                },
                            ) {
                                // Text
                                Text(
                                    text = "Log out",
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    color = Color.White
                                )
                            }


                            Spacer(modifier = Modifier.height(4.dp))


                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(4.dp),
                                onClick = {
                                    bottomSheetState = false
                                },
                            ) {
                                // Text
                                Text(
                                    text = "Dismiss",
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }


                        }

                    }

                }


            }

        }

        if (isLoading) {
            LoadingDialog()
        }
    }
}


@Composable
fun ProfileManagement(
    accountType: AccountType,
    onChangePasswordNavigateUp: () -> Unit,
    onSelectAccountType: (AccountType) -> Unit,
    onClickLogOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Text(text = "Manage Account", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            ProfileCard(
                iconRes = R.drawable.ic_security,
                title = "Password",
                description = "Manage Account Password",
                cardBackgroundColor = MaterialTheme.colorScheme.secondary // Replace with your color
            ) {
                onChangePasswordNavigateUp()
            }

            Spacer(modifier = Modifier.height(4.dp))


            if (accountType == AccountType.Business) {
                // Password Card

                val annotatedString = buildAnnotatedString {

                    append("Switch to ")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Personal Account ")
                    }
                }


                AccountTypeCard(
                    iconRes = R.drawable.ic_switch_account_type,
                    title = annotatedString,
                    description = "Switch to your personal account to manage your individual preferences and settings.",
                    cardBackgroundColor = MaterialTheme.colorScheme.secondary // Replace with your color
                ) {

                    onSelectAccountType(accountType)
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
            if (accountType == AccountType.Personal) {
                val annotatedString = buildAnnotatedString {

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Switch to ")
                    }

                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold, fontSize = 18.sp
                        )
                    ) {
                        append("Business Account ")
                    }
                }


                // Password Card
                AccountTypeCard(
                    iconRes = R.drawable.ic_switch_account_type,
                    title = annotatedString,
                    description = "Switching to your business account will provide access to tools for business growth.",
                    cardBackgroundColor = MaterialTheme.colorScheme.secondary // Replace with your color
                ) {
                    onSelectAccountType(accountType)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                onClick = {
                    onClickLogOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Log Out",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                    }
                }
            }


        }

    }
}


@Composable
fun ProfileCard(
    iconRes: Int,
    title: String,
    description: String,
    cardBackgroundColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = dropUnlessResumed { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
fun AccountTypeCard(
    iconRes: Int,
    title: AnnotatedString,
    description: String,
    cardBackgroundColor: Color,
    onClick: () -> Unit,
) {
    Card(

        onClick = dropUnlessResumed { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}