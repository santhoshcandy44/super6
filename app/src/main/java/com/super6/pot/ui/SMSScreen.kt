package com.super6.pot.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.super6.pot.R
import com.super6.pot.ui.chat.E2EEEMessageHeader
import com.super6.pot.ui.chat.StateSyncingModifier
import com.super6.pot.ui.theme.customColorScheme
import com.super6.pot.ui.utils.ScrollBarConfig
import com.super6.pot.ui.utils.verticalScrollWithScrollbar
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SmsMessageData(
    val id: Long,
    val address: String,   // Sender's phone number
    val body: String,      // SMS message content
    val date: Long,        // Timestamp in milliseconds
    val isSentByUser: Boolean = false // Flag to indicate if the message was sent by the user
)


@HiltViewModel
class SmsViewModel @Inject constructor(@ApplicationContext val context:Context): ViewModel() {

    private val _activeSubscriptions = MutableStateFlow<List<SubscriptionInfo>>(emptyList())
    val activeSubscriptions = _activeSubscriptions.asStateFlow()

    private val _selectedSubscription = MutableStateFlow<SubscriptionInfo?>(null)
    val selectedSubscription = _selectedSubscription.asStateFlow()

    private val _groupedSmsMessages = MutableStateFlow<Map<String, List<SmsMessageData>>>(emptyMap())
    val groupedSmsMessages = _groupedSmsMessages.asStateFlow()

    init {
        fetchActiveSubscriptions()
    }

    fun fetchAndGroupSmsMessages(context: Context) {
        viewModelScope.launch {
            val smsList = getAllSmsMessages(context)
            _groupedSmsMessages.value = smsList.groupBy { it.address }
        }
    }

    fun getLatestMessages(): List<SmsMessageData> {
        return _groupedSmsMessages.value.mapNotNull { entry ->
            entry.value.maxByOrNull { it.date } // Get the latest message per sender
        }
    }

    fun getMessagesForSender(address: String): List<SmsMessageData> {
        return _groupedSmsMessages.value[address]?.sortedByDescending { it.date } ?: emptyList()
    }


    private fun getAllSmsMessages(context: Context): List<SmsMessageData> {
        val smsList = mutableListOf<SmsMessageData>()
        val uri = Uri.parse("content://sms")
        val projection = arrayOf("_id", "address", "date", "body", "type")

        context.contentResolver.query(uri, projection, null, null, "date DESC")?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("_id")
            val addressIndex = cursor.getColumnIndexOrThrow("address")
            val dateIndex = cursor.getColumnIndexOrThrow("date")
            val bodyIndex = cursor.getColumnIndexOrThrow("body")
            val typeIndex = cursor.getColumnIndexOrThrow("type") // Add type column

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val address = cursor.getString(addressIndex) ?: "Unknown"
                val date = cursor.getLong(dateIndex)
                val body = cursor.getString(bodyIndex) ?: ""
                val type = cursor.getInt(typeIndex)

                // Determine if the message is sent by the user based on type
                val isSentByUser = type == 1  // 1 means sent by user, 2 means received

                smsList.add(SmsMessageData(id, address, body, date, isSentByUser))
            }
        }
        return smsList
    }


    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun fetchActiveSubscriptions() {


        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager


        val subscriptions = subscriptionManager?.activeSubscriptionInfoList ?: emptyList()

        _activeSubscriptions.value = subscriptions


        // Default to the first subscription if available
        if (subscriptions.isNotEmpty()) {
            _selectedSubscription.value = subscriptions.first()
        }
    }


    fun switchSubscription(subscriptionInfo: SubscriptionInfo) {
        _selectedSubscription.value = subscriptionInfo
    }



    @RequiresPermission(Manifest.permission.SEND_SMS)
    fun sendSmsMessage(context: Context, subscriptionId: Int, recipient: String, message: String) {
        try {
            // Get SmsManager instance for the specific subscription ID
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
                    ?.createForSubscriptionId(subscriptionId)
            } else {
                SmsManager.getDefault()
            }

            smsManager?.sendTextMessage(recipient, null, message, null, null)
            Log.d("SmsViewModel", "SMS sent successfully to $recipient")
        } catch (e: Exception) {
            Log.e("SmsViewModel", "Failed to send SMS: ${e.message}")
        }
    }



}





@Composable
fun PermissionWrapper(viewModel: SmsViewModel, onMessageClick: (String) -> Unit){

    val context = LocalContext.current

    var permissionState by remember { mutableStateOf( ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionState = granted
            if (granted) {
                viewModel.fetchAndGroupSmsMessages(context)
            }
        }
    )

    LaunchedEffect(Unit) {
        if(!permissionState){
            launcher.launch(Manifest.permission.READ_SMS)
        }else{
            viewModel.fetchAndGroupSmsMessages(context)
        }
    }

    if (permissionState) {
        SMSScreen(viewModel = viewModel, onMessageClick)
    } else {
        Text("Permission required to read SMS")
    }
}

@Composable
fun SMSScreen(viewModel: SmsViewModel, onMessageClick: (String) -> Unit) {
    val groupedSmsMessages by viewModel.groupedSmsMessages.collectAsState()

    var latestMessages by remember { mutableStateOf(emptyList<SmsMessageData>()) }

    LaunchedEffect(groupedSmsMessages){
        if(groupedSmsMessages.isNotEmpty()){
            latestMessages=viewModel.getLatestMessages()
        }
    }

    val context = LocalContext.current

    LazyColumn {
        items(latestMessages) { message ->
            SmsItem(
                message = message,
                onClick = { onMessageClick(message.address) }
            )
        }
    }

}


@Composable
fun SmsItem(message: SmsMessageData, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
            .background(Color(0xFFEFEFEF), shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "From: ${message.address}",
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Last message: ${formatDate(message.date)}",
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message.body,
            color = Color.DarkGray
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


fun formatMessageReceived(timestamp: Long): String {

    // Determine the grouping label
    val time = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())  // Adjust ZoneId if needed
        .toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("h:mm a"))  // Format as "h:mm a"

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSChatWindowScreen(viewModel: SmsViewModel, senderAddress: String) {
    val groupedSmsMessages by viewModel.groupedSmsMessages.collectAsState()
    var messages by remember { mutableStateOf(emptyList<SmsMessageData>()) }

    LaunchedEffect(groupedSmsMessages){
       if(groupedSmsMessages.isNotEmpty()){
           messages=viewModel.getMessagesForSender(senderAddress)
       }
    }

    val context = LocalContext.current

    LaunchedEffect(messages.size){
        Toast.makeText(context,"${messages.size}", Toast.LENGTH_SHORT)
            .show()
    }

    val activeSubscriptions by viewModel.activeSubscriptions.collectAsState()

    val selectedSubscription by viewModel.selectedSubscription.collectAsState()


    var value by remember { mutableStateOf("") }
    val textFieldState = remember {

        TextFieldState(
            initialText = value,
            // Initialize the cursor to be at the end of the field.
            initialSelection = TextRange(value.length)
        )
    }



    Scaffold(
        topBar = {

            TopAppBar(
                modifier = Modifier.shadow(2.dp),
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {

                    val headerProfileImageRequest = remember(Unit) {
                        ImageRequest.Builder(context)
                            .data(null)
                            .placeholder(
                                R.drawable.user_placeholder
                            ) // Your placeholder image
                            .error(R.drawable.user_placeholder)
                            .crossfade(true)
                            .build()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Profile Image Container
                        Column(
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(end = 8.dp)
                        ) {

                            AsyncImage(
                                headerProfileImageRequest,
                                contentDescription = "User Profile Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // User Name and Status Container
                        Column(
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            /*  Text(
                                  text = "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}", // Replace with actual user name
                                  style = MaterialTheme.typography.bodyMedium
                              )

                              if (onlineStatus.isNotEmpty()) {
                                  Text(
                                      text = onlineStatus, // Replace with actual status
                                      style = MaterialTheme.typography.bodyMedium
                                  )
                              }*/

                        }
                    }
                }
            )


        }
    ) { contentPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {

            if (!true) {

                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            } else {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {

                        LazyColumn(
                            state = rememberLazyListState(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            reverseLayout = true
                        ) {

                            items(messages) { message ->
                                ChatBubble(message)
                            }

                            item(key = "profile-header") {
/*
                                ProfileHeader(userProfileInfo)
*/
                            }

                            item(key = "e2ee-message") {
                                E2EEEMessageHeader()
                            }
                        }

                        /*
                                                if (isGoToBottom) {
                                                    Box(
                                                        modifier = Modifier
                                                            .wrapContentSize()
                                                            .align(Alignment.BottomEnd)
                                                            .padding(horizontal = 16.dp, vertical = 8.dp)

                                                    ) {
                                                        FloatingActionButton(
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    lazyListState.animateScrollToItem(0)
                                                                }
                                                            },
                                                            modifier = Modifier.size(32.dp)

                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.KeyboardDoubleArrowDown,
                                                                contentDescription = "Scroll to Bottom",
                                                                tint = Color.White
                                                            )
                                                        }

                                                    }
                                                }
                        */



                    }




                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        RCSMessageTextField(
                            value,
                            selectedSubscription,
                            textFieldState,
                            {
                                value = it

                            },
                            {

                            },
                            Modifier
                                .heightIn(min = 48.dp)
                                .weight(1f)
                        )


                        if (value.trim().isNotEmpty()) {
                            // Send Button
                            IconButton(
                                onClick = {

/*
                                    if (value.trim().isEmpty()) {
                                        return@IconButton
                                    }

                                    selectedMessage?.let { nonNullSelectedMessage ->
                                        viewModel.sendMessage(
                                            value.trim(),
                                            nonNullSelectedMessage.senderMessageId,
                                            nonNullSelectedMessage.id
                                        ) {
                                        }
                                    } ?: run {
                                        viewModel.sendMessage(value.trim()) {


                                        }
                                    }

                                    textFieldState.clearText()
                                    showReplyContent = false
                                    viewModel.setSelectedMessage(null)*/
                                },
                                /*
                                                                modifier = Modifier.padding(start = 8.dp)
                                */
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_send), // Replace with actual drawable
                                    contentDescription = "Send",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
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
fun ChatBubble(message: SmsMessageData) {

    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f, false),
                contentAlignment = if (message.isSentByUser) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Text(
                    text = message.body,
                    modifier = Modifier
                        .background(
                            Color(0xFFECECEA),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomEnd = 16.dp,
                                bottomStart = 0.dp
                            )
                        )
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Timestamp
            Text(
                text = formatMessageReceived(message.date),
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),

                )
        }
    }

}



@Composable
fun RCSMessageTextField(
    value: String,
    activeSubscription:SubscriptionInfo?,
    state: TextFieldState,
    onValueChange: (String) -> Unit,
    onChooseAttachmentClicked: () -> Unit,
    modifier: Modifier = Modifier
    // and other arguments you want to delegate
) {


    val scrollState = rememberScrollState()

    // This is effectively a rememberUpdatedState, but it combines the updated state (text) with
    // some state that is preserved across updates (selection).
    var valueWithSelection by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        )
    }
    valueWithSelection = valueWithSelection.copy(text = value)


    // EditText for message input
    BasicTextField(
        state = state,
//                    onValueChange = { viewModel.onMessageValueChange(it) },
        textStyle = TextStyle.Default.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        ),

        modifier = modifier.then(
            StateSyncingModifier(
                state = state,
                value = valueWithSelection,
                onValueChanged = {
                    // Don't fire the callback if only the selection/cursor changed.
                    if (it.text != valueWithSelection.text) {
                        onValueChange(it.text)
                    }
                    valueWithSelection = it
                },
                writeSelectionFromTextFieldValue = false
            )

                .background(
                    MaterialTheme.customColorScheme.searchBarColor,
                    RoundedCornerShape(20.dp)
                )
                .heightIn(min = 40.dp)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ),
        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
        scrollState = scrollState,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        decorator = {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeSubscription?.let {
                    if (state.text.isEmpty()) {


                     SimCardSubscription(SimCardInfo(
                         it.simSlotIndex,
                         it.carrierName.toString(),
                         it.number,
                         it.subscriptionId,
                     ), true) {

                     }

                        Spacer(Modifier.width(4.dp))

                        /*         IconButton(

                                     onClick = {}, modifier = Modifier
                                         .size(32.dp)
                                         .minimumInteractiveComponentSize()
                                 ) {
                                     Icon(
                                         imageVector = Icons.Filled.PhotoCamera,
                                         contentDescription = "Add Camera Photos",
                                     )
                                 }*/
                    }
                }
                    ?: run{
                        Text("is null")
                    }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScrollWithScrollbar(
                            scrollState,
                            scrollbarConfig = ScrollBarConfig()
                        ),
                    contentAlignment = Alignment.CenterStart

                ) {
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                "Type message...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        it()
                    }

                }


                if (state.text.isEmpty()) {
                    IconButton(
                        onClick = onChooseAttachmentClicked, modifier =
                        Modifier
                            .size(32.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = "Add Attachments",
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    /*         IconButton(

                                 onClick = {}, modifier = Modifier
                                     .size(32.dp)
                                     .minimumInteractiveComponentSize()
                             ) {
                                 Icon(
                                     imageVector = Icons.Filled.PhotoCamera,
                                     contentDescription = "Add Camera Photos",
                                 )
                             }*/
                }

            }

        })


}


@Composable
fun SimCardSubscription(
    simCardInfo: SimCardInfo,
    isSelected: Boolean,
    onCardClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
            .padding(8.dp)
            .clickable { onCardClick(simCardInfo.subscriptionId) },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White)
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Index Number
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .size(40.dp)
                    .clip(
                        SimCardShape(cutSize = 16.dp.toPx())
                    )
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = simCardInfo.index.toString(),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = simCardInfo.carrierName,
                fontWeight = FontWeight.Bold,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
        }
    }
}


data class SimCardInfo(
    val index: Int,
    val carrierName: String,
    val phoneNumber: String?,
    val subscriptionId: Int
)


class SimCardShape(private val cutSize: Float) : Shape {

    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f) // Top-left corner
            lineTo(size.width - cutSize, 0f) // Top edge
            lineTo(size.width, cutSize) // Cut edge
            lineTo(size.width, size.height) // Right edge
            lineTo(0f, size.height) // Bottom edge
            close() // Close the shape
        }
        return Outline.Generic(path)
    }
}

@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }
