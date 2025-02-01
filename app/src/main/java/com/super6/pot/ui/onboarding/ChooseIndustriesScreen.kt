package com.super6.pot.ui.onboarding


import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.super6.pot.api.models.service.Industry
import com.super6.pot.ui.ShimmerBox
import com.super6.pot.ui.auth.LoadingDialog
import com.super6.pot.ui.onboarding.viewmodels.ChooseIndustriesViewModel
import com.super6.pot.ui.onboarding.viewmodels.GuestChooseIndustriesViewModel
import com.super6.pot.ui.theme.customColorScheme
import com.super6.pot.ui.managers.NetworkConnectivityManager
import com.super6.pot.api.Utils.ResultError


@Composable
fun GuestChooseIndustrySheet(
    viewModel: GuestChooseIndustriesViewModel = hiltViewModel(),
    onDismissed: () -> Unit,
    onIndustriesUpdated: () -> Unit,
) {

    val userId = viewModel.userId

    val context = LocalContext.current


    val industryItems by viewModel.itemList.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    val connectivityManager = viewModel.connectivityManager


    val anyItemSelected = industryItems.any { it.isSelected }


    val onRetry = {

        if (connectivityManager.isConnectedInternet) {

            viewModel.onGetGuestIndustries(
                userId,
                onSuccess = {

                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }

        } else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT)
                .show()
        }
    }


    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.onGetGuestIndustries(
                    userId,
                    isLoading = false,
                    isRefreshing = true,
                    onSuccess = {}
                ) { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {

            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                viewModel.setRefreshing(false)
            }
        }
    }

    val onRefresh = {
        viewModel.setRefreshing(true)
        connectivityManager.checkForSeconds(Handler(Looper.getMainLooper()), statusCallback, 4000)
    }




    Box(modifier = Modifier.fillMaxWidth()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Choose Industries", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(8.dp))
                ChooseIndustryContent(
                    industryItems = industryItems,
                    onIndustrySelectionChanged = { viewModel.onIndustrySelectionChanged(it) },
                    onRetry = { onRetry() },
                    onRefresh = { onRefresh() },
                    onUpdateIndustriesClicked = {

                        if (viewModel.validateIndustries()) {
                            viewModel.onUpdateIndustries(
                                industryItems,
                                onSuccess = {

                                    onIndustriesUpdated()

                                    Toast.makeText(
                                        context,
                                        it,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                },
                                onError = { errorMessage ->

                                    Toast.makeText(
                                        context,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {

                            Toast.makeText(
                                context,
                                "At least 1 industry selected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    isLoading = isLoading,
                    error = error,
                    isRefreshing = isRefreshing,
                    isUpdating = isUpdating,
                    anyItemSelected = anyItemSelected
                )


                OutlinedButton(
                    {
                        onDismissed()
                    },
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't show again")
                }
            }

        }


    }


}


@Composable
fun ChooseIndustrySheet(
    onDismissed: () -> Unit,
    onNavigateUpChooseIndustries: () -> Unit,
) {


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Choose Industries", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text("It looks like you haven’t selected any industries yet. Choose a few to get a personalized experience!")
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                {
                    onNavigateUpChooseIndustries()
                },
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose industries")
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                {
                    onDismissed()
                },
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't show again")
            }
        }

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestChooseIndustryScreen(
    onMainActivityNavigateUp: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: GuestChooseIndustriesViewModel = hiltViewModel()
    ) {

    val userId = viewModel.userId

    val context = LocalContext.current


    val industryItems by viewModel.itemList.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    val connectivityManager = viewModel.connectivityManager


    val anyItemSelected = industryItems.any { it.isSelected }


    val onRetry = {

        if (connectivityManager.isConnectedInternet) {

            viewModel.onGetGuestIndustries(
                userId,
                onSuccess = {

                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }

        } else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT)
                .show()
        }
    }


    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.onGetGuestIndustries(
                    userId,
                    isLoading = false,
                    isRefreshing = true,
                    onSuccess = {}
                ) { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                viewModel.setRefreshing(false)
            }
        }
    }

    val onRefresh = {
        viewModel.setRefreshing(true)
        connectivityManager.checkForSeconds(Handler(Looper.getMainLooper()), statusCallback, 4000)
    }




    Box {
        Scaffold(
            topBar =
            {
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
                            text = "Choose Industries",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )

            }) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                ChooseIndustryContent(
                    industryItems = industryItems,
                    onIndustrySelectionChanged = { viewModel.onIndustrySelectionChanged(it) },
                    onRetry = { onRetry() },
                    onRefresh = { onRefresh() },
                    onUpdateIndustriesClicked = {

                        if (viewModel.validateIndustries()) {
                            viewModel.onUpdateIndustries(
                                industryItems,
                                onSuccess = {

                                    onMainActivityNavigateUp()

                                    Toast.makeText(
                                        context,
                                        it,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                },
                                onError = { errorMessage ->

                                    Toast.makeText(
                                        context,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {

                            Toast.makeText(
                                context,
                                "At least 1 industry selected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    isLoading = isLoading,
                    error = error,
                    isRefreshing = isRefreshing,
                    isUpdating = isUpdating,
                    anyItemSelected = anyItemSelected
                )

            }
        }

        if (isUpdating) {
            LoadingDialog()
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseIndustryScreen(
    onMainActivityNavigateUp: () -> Unit,
    onPopBackStack:() -> Unit,
    viewModel: ChooseIndustriesViewModel = hiltViewModel(),

    ) {

    val type = viewModel.type

    val userId = viewModel.userId

    val context = LocalContext.current

    val industryItems = viewModel.itemList

    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()

    val connectivityManager = viewModel.connectivityManager


    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.onGetIndustries(
                    userId,
                    isLoading = false,
                    isRefreshing = true,
                    onSuccess = {}
                ) { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                viewModel.setRefreshing(false)
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    val onRefresh = {
        viewModel.setRefreshing(true)
        connectivityManager.checkForSeconds(Handler(Looper.getMainLooper()), statusCallback, 4000)
    }


    // Function to check if all fields are filled
    val anyItemSelected = industryItems.any { it.isSelected }



    Box {
        Scaffold(
            topBar =
            {
                if (type != null && (type == "on_board" || type == "update_industries")) {
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
                                text = if (type == "on_board") "Choose Industries" else "Manage Industries",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )

                }
            }) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                ChooseIndustryContent(
                    industryItems = industryItems,
                    onIndustrySelectionChanged = { viewModel.onIndustrySelectionChanged(it) },
                    onRetry = { onRefresh() },
                    onRefresh = { onRefresh() },
                    onUpdateIndustriesClicked = {

                        if (viewModel.validateIndustries()) {
                            viewModel.onUpdateIndustries(
                                userId,
                                industryItems,
                                onSuccess = {


                                    if (type != null && (type == "on_board")) {
                                        onMainActivityNavigateUp()
                                    }

                                    Toast.makeText(
                                        context,
                                        it,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                },
                                onError = { errorMessage ->

                                    Toast.makeText(
                                        context,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {

                            Toast.makeText(
                                context,
                                "At least 1 industry selected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    isLoading = isLoading,
                    error = error,
                    isRefreshing = isRefreshing,
                    isUpdating = isUpdating,
                    anyItemSelected = anyItemSelected
                )

            }
        }

        if (isUpdating) {
            LoadingDialog()
        }
    }


}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChooseIndustryContent(
    industryItems: List<Industry>,
    onIndustrySelectionChanged: (Industry) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateIndustriesClicked: () -> Unit,
    isLoading: Boolean,
    error: ResultError?,
    isRefreshing: Boolean,
    isUpdating: Boolean,
    anyItemSelected: Boolean,
) {

    val scrollState = rememberScrollState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onRefresh() },
    ) {

        Box(
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            if (isLoading) {


                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    ShimmerBox {
                        Text(
                            text = "Pick Your Interests",
                            color = Color.Transparent,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBox {
                        Text(
                            text = "Select your industries to personalize your experience.",
                            color = Color.Transparent,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()

                    ) {
                        repeat(3) {
                            ShimmerInterestItem()
                        }
                    }

                }


            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Text(
                        text = "Pick Your Interests",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select your industries to personalize your experience.",
                        style = MaterialTheme.typography.bodyMedium)


                    Spacer(modifier = Modifier.height(8.dp))


                    if (error is ResultError.NoInternet) {



                        Row(horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()) {

                            Text(
                                "Internet connection failed",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Retry",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    onRetry()
                                }
                            )
                        }

                    }
                    else if (error != null && industryItems.isEmpty()) {
                        Text("Something went wrong",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium)
                    } else {


                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()

                        ) {
                            industryItems.map { industry ->

                                // Using a unique key, such as the industry ID
                                key(industry.industryId) {
                                    InterestItem(industry) { selectedItem ->
                                        onIndustrySelectionChanged(selectedItem)
                                    }
                                }
                            }

                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(

                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = {
                                onUpdateIndustriesClicked()
                            },
                            enabled = anyItemSelected && !isUpdating,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {

                            if (isUpdating && anyItemSelected) {

                                CircularProgressIndicator(
                                    color = Color.White, // Change this to any color you prefer
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                )

                            } else {
                                Text(
                                    "Continue",
                                    color = if (anyItemSelected) Color.White else Color.LightGray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        }

                    }

                }

            }
        }

    }

}


@Composable
fun ShimmerInterestItem() {

    ShimmerBox(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(CircleShape) // Ensure the shape is applied here for the ripple effect to respect it
            .border(1.dp, MaterialTheme.customColorScheme.shimmerContainer, CircleShape)

    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            text = "Industry title",
            color = Color.Transparent,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}


@Composable
fun InterestItem(
    industry: Industry,
    onClick: (Industry) -> Unit,
) {
    // Determine the background color based on selection
    val backgroundColor = if (industry.isSelected) {
        MaterialTheme.colorScheme.primary // Selected color (purple_500)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest // Default color (white)
    }

    // Determine the background color based on selection
    val borderColor = if (industry.isSelected) {
        MaterialTheme.colorScheme.primary // Selected color (purple_500)
    } else {
        Color.LightGray // Default color (gray)
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(CircleShape) // Ensure the shape is applied here for the ripple effect to respect it
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, // Required for indication
                indication = ripple(bounded = true), // Default ripple effect
            ) {
                onClick(industry) // Call onClick with the updated industry
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = industry.industryName,
            color = if (industry.isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


