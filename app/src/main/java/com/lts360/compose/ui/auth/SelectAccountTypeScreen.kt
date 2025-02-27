package com.lts360.compose.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import com.lts360.R
import com.lts360.compose.ui.auth.viewmodels.SwitchAccountTypeViewModel
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettings
import com.lts360.compose.ui.main.viewmodels.AccountAndProfileSettingsViewModel
import com.lts360.compose.ui.theme.icons
import com.lts360.compose.utils.NavigatorSubmitButton



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchAccountTypeScreen(
    navController: NavController,
    onNavigateUp: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: SwitchAccountTypeViewModel = hiltViewModel(),

    ) {
    val backStackEntry = remember {
        navController.getBackStackEntry<AccountAndProfileSettings>()
    }

    val sharedViewModel: AccountAndProfileSettingsViewModel = hiltViewModel(backStackEntry)

    val userId = viewModel.userId

    val accountType = viewModel.accountType

    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()

    Surface {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Scaffold(

                topBar = {

                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = dropUnlessResumed { onPopBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back Icon"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Manage Account Type",
                                style = MaterialTheme.typography.titleMedium
                            )
                        })
                }

            ) { innerPadding ->


                Surface(modifier = Modifier.padding(innerPadding)) {
                    SelectAccountTypeScreenContent(accountType, isLoading,"switch_account") {
                        selectedAccountType->

                        viewModel.onSwitchAccountType(userId, selectedAccountType, {
                            updatedAccountType,message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                .show()
                            sharedViewModel.setAccountType(updatedAccountType)
                            onNavigateUp()
                        }) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

            }

            if (isLoading) {
                LoadingDialog()
            }

        }
    }


}


@Composable
fun SelectAccountTypeScreen(
    onNavigateUp: (AccountType) -> Unit,
) {

    Surface( modifier = Modifier
            .fillMaxSize()){
        SelectAccountTypeScreenContent {
            onNavigateUp(it)
        }
    }

}


@Composable
private fun SelectAccountTypeScreenContent(
    defaultSelectedAccountType: AccountType = AccountType.Personal,
    isLoading: Boolean = false,
    type:String?=null,
    onContinueClick: (AccountType) -> Unit) {
    // Define the account types
    val accountTypes = listOf(
        AccountTypeChooser(
            "Personal",
            MaterialTheme.icons.accountTypePersonal,
            "Looking for a personalized experience.", AccountType.Personal
        ),
        AccountTypeChooser(
            "Business",
            MaterialTheme.icons.accountTypeBusiness,
            "Personalized experience & Active business tools for purposes.", AccountType.Business
        )
    )


    // Use `rememberSaveable` to retain the state across navigation
    var selectedAccountType by rememberSaveable { mutableStateOf(defaultSelectedAccountType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose Account Type",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We are ready setup your account accordingly.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display account type cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(accountTypes) { accountType ->
                AccountTypeCard(
                    accountType = accountType,
                    isSelected = selectedAccountType == accountType.type,
                    onClick = {
                        selectedAccountType = accountType.type
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        if(type=="switch_account"){
            NavigatorSubmitButton(isLoading, onNextButtonClicked = {
                onContinueClick(selectedAccountType)
            }, content = {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, // Change this to any color you prefer
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                } else {
                    Text(
                        "Next",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            },Modifier.fillMaxWidth())

        }else{
            ContinueButton(isLoading) {
                onContinueClick(selectedAccountType)
            }
        }

    }


}

@Composable
fun AccountTypeCard(
    accountType: AccountTypeChooser,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected)
        colorResource(id = R.color.colorPrimary) else
        Color.LightGray // Change color when selected

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            // Account Type Image
            Image(
                painter = painterResource(id = accountType.imageRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Account Type Info

            Column(
                modifier = Modifier.weight(1f) // This allows the Column to take available space
            ) {
                Text(text = accountType.name, style = MaterialTheme.typography.titleMedium)
                Text(text = accountType.benefit, style = MaterialTheme.typography.bodyMedium)
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape) // Make it round
                        .size(40.dp) // Set the size of the icon
                        .background(Color.Green) // Background color
                        .padding(8.dp) // Padding inside the circle
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White, // Icon color
                        modifier = Modifier.fillMaxSize() // Make the icon fill the box
                    )
                }
            } else {

                Box(
                    modifier = Modifier
                        .clip(CircleShape) // Make it round
                        .size(40.dp) // Set the size of the icon
                        .background(Color.LightGray) // Background color
                        .padding(8.dp) // Padding inside the circle
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White, // Icon color
                        modifier = Modifier.fillMaxSize() // Make the icon fill the box
                    )
                }
            }

        }
    }
}


@Composable
fun ContinueButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    // Create a rectangular button with rounded corners
    Button(
        enabled = !isLoading,
        onClick = dropUnlessResumed { onClick()  },
        modifier = Modifier
            .fillMaxWidth() // Make it fill the width

    ) {
        Text(
            text = "Continue",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp) // Horizontal padding for the text
        )
    }
}


enum class AccountType {
    Personal,
    Business
}


data class AccountTypeChooser(
    val name: String,
    val imageRes: Int,
    val benefit: String,
    val type: AccountType,
)

