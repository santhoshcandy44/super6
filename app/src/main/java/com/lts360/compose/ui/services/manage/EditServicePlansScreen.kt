package com.lts360.compose.ui.services.manage


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.lts360.api.models.service.EditablePlan
import com.lts360.api.models.service.EditablePlanFeature
import com.lts360.api.models.service.Plan
import com.lts360.api.models.service.PlanFeature
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.services.ValidatedPlan
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.lts360.compose.ui.theme.icons
import java.math.BigDecimal


@Composable
fun EditServicePlanScreen(
    onPopBackStack: () -> Unit,
    viewModel: PublishedServicesViewModel
) {

    val selectedService by viewModel.selectedService.collectAsState()

    val userId = viewModel.userId
    val validatedPlans by viewModel.editablePlans.collectAsState()
    val plansError by viewModel.plansError.collectAsState()

    val isUpdating by viewModel.isUpdating.collectAsState()

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        EditServicePlanContent(
            plansError = plansError,
            plans = validatedPlans,
            onAddNewPlan = {
                if (validatedPlans.size >= 3) {
                    Toast.makeText(context, "Maximum 3 plans can be added", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.addPlan()

                }
            },
            onUpdatePlan = { index, updatedPlan ->
                viewModel.updatePlan(index, updatedPlan)
            },
            onRemovePlan = {
                viewModel.removePlan(it)
            },
            onUpdatePlans = { editablePlans ->

                if (viewModel.validatePlans()) {
                    viewModel.onUpdateServicePlans(
                        userId, selectedService!!.serviceId,
                        Gson().toJson(editablePlans.map { editablePlan ->
                            Plan(
                                editablePlan.planId,
                                editablePlan.planName.trim(),
                                editablePlan.planDescription.trim(),
                                editablePlan.planPrice,
                                editablePlan.planPriceUnit,
                                editablePlan.planFeatures.map {
                                    PlanFeature(
                                        it.featureName.trim(),
                                        it.featureValue.toString().trim()
                                    )
                                },
                                editablePlan.planDeliveryTime,
                                editablePlan.planDurationUnit
                            )
                        }), {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    ) {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT)
                            .show()
                    }

                }

            },
            onPopBackStack = onPopBackStack
        )



        if (isUpdating) {
            LoadingDialog()
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditServicePlanContent(
    plansError: String?,
    plans: List<ValidatedPlan>,
    onAddNewPlan: () -> Unit,
    onRemovePlan: (EditablePlan) -> Unit,
    onUpdatePlan: (Int, ValidatedPlan) -> Unit,
    onUpdatePlans: (List<EditablePlan>) -> Unit,
    onPopBackStack: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onPopBackStack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Manage Plans & Features",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
            )
        },
        content = { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {

                    Text(
                        text = "Edit Service Plans and Features ",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Plans", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(plans, key = { index, _ -> index }) { index, validatedPlan ->
                    PlanItem(
                        plan = validatedPlan.editablePlan,
                        updatePlan = { updatedPlan ->
                            onUpdatePlan(index, ValidatedPlan(true, updatedPlan))
                        },
                        onPlanRemove = { onRemovePlan(it) },
                        onValidate = !validatedPlan.isValid
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedButton(
                        onClick = {
                            onAddNewPlan()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(MaterialTheme.icons.add),
                            contentDescription = "Add Plan",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Add Plans")
                    }

                    plansError?.let {
                        ErrorText(it)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onUpdatePlans(plans.map {
                                it.editablePlan
                            })
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Update Plans")
                    }
                }
            }

        }
    )
}

@Composable
fun AddFeature(
    featureName: String,
    onFeatureNameChange: (String) -> Unit,
    featureValue: String,
    onFeatureValueChange: (String) -> Unit,
    onRemoveFeature: () -> Unit,
    onValidate: Boolean = false,
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            OutlinedTextField(
                isError = if (onValidate || featureName.length > 40) featureName.trim()
                    .isEmpty() || featureName.length > 40 else false,
                value = featureName,
                onValueChange = onFeatureNameChange,
                label = { Text("Feature") })



            if (featureName.length > 40) {
                Text(
                    text = "Limit: ${featureName.length}/${40}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ) // Adjust padding as needed
                )
            }

            if (onValidate && featureName.trim().isEmpty()) {
                ErrorText("Feature name cannot be empty")
            }


        }


        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            OutlinedTextField(
                isError = if (onValidate || featureValue.length > 10) featureValue.trim()
                    .isEmpty() || featureValue.length > 10 else false,
                value = featureValue,
                onValueChange = onFeatureValueChange,
                label = { Text("Value") }
            )


            if (featureValue.length > 10) {
                Text(
                    text = "Limit: ${featureValue.length}/${10}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ) // Adjust padding as needed
                )
            }



            if (onValidate && (featureValue.trim().isEmpty())) {
                ErrorText("Feature value cannot be empty")
            }
        }

        IconButton(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = CircleShape
                )
                .size(40.dp),
            onClick = { onRemoveFeature() }
        ) {
            Text(text = "-")
        }
    }
}


@Composable
fun ErrorText(message: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = message,
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(
            horizontal = 16.dp
        ) // Adjust padding as needed
    )
}

@Composable
fun PlanItem(
    plan: EditablePlan,
    updatePlan: (EditablePlan) -> Unit,
    onPlanRemove: (EditablePlan) -> Unit,
    onValidate: Boolean = false, // Validation callback to parent
) {

    val context = LocalContext.current

    var planName = plan.planName.ifEmpty { "" }
    var planDescription = plan.planDescription.ifEmpty { "" }
    var planPrice = if (plan.planPrice == BigDecimal.ZERO) "" else plan.planPrice.toString()
    var planPriceUnit = plan.planPriceUnit.ifEmpty { "INR" }
    var planDeliveryTime = if (plan.planDeliveryTime == -1) "" else plan.planDeliveryTime.toString()
    val planFeatures = plan.planFeatures.toMutableList()
    var planDurationUnit = plan.planDurationUnit.ifEmpty { "D" }


    fun updatePlan() {
        val updatedPlan = plan.copy(
            planName = planName,
            planDescription = planDescription,
            planPrice = planPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            planPriceUnit = planPriceUnit,
            planDurationUnit = planDurationUnit,
            planDeliveryTime = planDeliveryTime.toIntOrNull() ?: -1,
            planFeatures = planFeatures
        )
        updatePlan(updatedPlan)
    }


    fun addFeature() {

        if (planFeatures.size >= 10) {
            Toast.makeText(context, "Maximum 10 features can be added", Toast.LENGTH_SHORT)
                .show()
        } else {
            planFeatures.add(EditablePlanFeature(featureName = "", featureValue = null))
            updatePlan()
        }
    }

    fun removeFeature(planFeature: EditablePlanFeature) {
        planFeatures.remove(planFeature)
        updatePlan()
    }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                isError = if (onValidate || planName.length > 20) planName.trim()
                    .isEmpty() || planName.length > 20 else false,
                value = planName,
                onValueChange = { newValue ->
                    planName = newValue
                    updatePlan()
                },
                label = { Text(text = "Plan Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            IconButton(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = CircleShape
                    )
                    .size(40.dp),
                onClick = { onPlanRemove(plan) }
            ) {
                Text(text = "-")
            }
        }

        if (planName.length > 20) {
            Text(
                text = "Limit: ${planName.length}/${20}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ) // Adjust padding as needed
            )
        }

        if (onValidate && planName.trim().isEmpty()) {
            ErrorText("Plan name cannot be empty")
        }
        OutlinedTextField(
            isError = if (onValidate || planDescription.length > 200) planDescription.trim()
                .isEmpty() || planDescription.length > 200 else false,
            value = planDescription,
            onValueChange = { newValue ->
                planDescription = newValue
                updatePlan()
            },
            label = { Text(text = "Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        if (planDescription.length > 200) {
            Text(
                text = "Limit: ${planDescription.length}/${200}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ) // Adjust padding as needed
            )
        }



        if (onValidate && planDescription.trim().isEmpty()) {
            ErrorText("Plan description cannot be empty")
        }
        PriceAndUnitSpinner(
            price = planPrice,
            onPriceChange = { newValue ->
                planPrice = newValue
                updatePlan()
            },
            selectedUnit = planPriceUnit,
            onUnitChange = {
                planPriceUnit = it
                updatePlan()
            },
            units = listOf("INR", "USD"),
            onValidate = onValidate
        )

        DeliveryTimeAndUnitSpinner(
            deliveryTime = planDeliveryTime,
            onDeliveryTimeChange = { newValue ->
                planDeliveryTime = newValue
                updatePlan()
            },
            selectedUnit = planDurationUnit,
            onUnitChange = {
                planDurationUnit = it
                updatePlan()

            },
            units = listOf("HR", "D", "W", "M"),
            onValidate = onValidate
        )


        Text(
            text = "Features",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        planFeatures.forEachIndexed { index, feature ->
            AddFeature(
                featureName = feature.featureName,
                onFeatureNameChange = { newValue ->
                    planFeatures[index] = feature.copy(featureName = newValue)
                    updatePlan()
                },
                featureValue = feature.featureValue?.toString() ?: "",
                onFeatureValueChange = { newValue ->
                    planFeatures[index] = feature.copy(featureValue = newValue)
                    updatePlan()
                },
                onRemoveFeature = {
                    removeFeature(feature)
                },
                onValidate
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (onValidate && planFeatures.isEmpty()) {
            ErrorText("AtLeast 1 feature must be added")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {

            IconButton(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = CircleShape
                    )
                    .size(40.dp),
                onClick = { addFeature() }
            ) {
                Text(text = "+")
            }

            Text(text = "Add Feature")
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAndUnitSpinner(
    price: String,
    onPriceChange: (String) -> Unit,
    selectedUnit: String,
    onUnitChange: (String) -> Unit,
    units: List<String>,
    onValidate: Boolean = false,
) {


    var expanded by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                isError = if (onValidate) price.isEmpty() else false,
                value = price,
                onValueChange = onPriceChange,
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )
            if (onValidate && price.isEmpty()) {
                ErrorText("Plan price cannot be empty")
            }

        }


        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp))
                .weight(1f)
        ) {
            OutlinedTextField(
                value = selectedUnit,
                onValueChange = { },
                readOnly = true,
                label = { Text("Currency") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(text = unit) },
                        onClick = {
                            onUnitChange(unit)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTimeAndUnitSpinner(
    deliveryTime: String,
    onDeliveryTimeChange: (String) -> Unit,
    selectedUnit: String,
    onUnitChange: (String) -> Unit,
    units: List<String>,
    onValidate: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                isError = if (onValidate) deliveryTime.isEmpty() else false,
                value = deliveryTime,
                onValueChange = onDeliveryTimeChange,
                label = { Text("Delivery Time") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )
            if (onValidate && deliveryTime.isEmpty()) {
                ErrorText("Plan delivery time cannot be empty")
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp))
                .weight(1f)
        ) {
            OutlinedTextField(
                value = selectedUnit,
                onValueChange = { },
                readOnly = true,
                label = { Text("Duration") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(text = unit) },
                        onClick = {
                            onUnitChange(unit)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
