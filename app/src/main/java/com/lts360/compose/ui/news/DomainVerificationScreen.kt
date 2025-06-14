package com.lts360.compose.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.compose.ui.theme.customColorScheme
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainVerificationScreen(viewModel: DomainVerificationViewModel = koinViewModel()) {

    val domain by viewModel.domain.collectAsState()
    val domainError by viewModel.domainError.collectAsState()

    val verificationCode by viewModel.verificationCode.collectAsState()
    val verificationResult by viewModel.verificationResult.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize(),

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
                        text = " Manage News Source",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) { contentPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {

            Text("Enter Your Domain")

            Spacer(modifier = Modifier.height(4.dp))

            verificationResult?.takeIf { it.isValid }?.let { result ->
                Text("Domain Verified", color = Color(
                    0xFF1DBC60
                ))
                Text(result.domain, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
            }

            verificationResult?.takeIf { !it.isValid && it.errorMessage!=null }?.let { result ->
                Text("Domain Not Verified", color = Color.Red.copy(alpha = 0.6f))
                Text(result.domain, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Red, shape = RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor =  Color.Red.copy(alpha = 0.6f)) // Light Green Background
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(result.errorMessage?:"Something wrong", color = Color.White,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

            }


            // Domain Input
            BasicTextField(
                domain,
                readOnly = verificationResult?.takeIf { it.isValid }?.let { true } ?: run { false },
                onValueChange = {
                    viewModel.onDomainValueChange(it)
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.customColorScheme.searchBarColor,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
                    .height(28.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri),
            ) { innerTextField ->

                Row(verticalAlignment = Alignment.CenterVertically) {

                    innerTextField() // Displays the actual text input field

                }
            }

            domainError?.let {
                ErrorText(it)
            }

            Spacer(modifier = Modifier.height(8.dp))


            // Show verification result
            verificationResult?.takeIf { it.isValid }?.let { result ->


                if(result.status=="Pending"){
                    Text("Add Feed Url")

                    Spacer(modifier = Modifier.height(4.dp))

                    // Domain Input
                    BasicTextField(
                        domain,
                        onValueChange = {
                            viewModel.onDomainValueChange(it)
                        },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(28.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri),
                    ) { innerTextField ->

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            innerTextField() // Displays the actual text input field
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0177fb),
                            contentColor = MaterialTheme.colorScheme.onSurface,

                            ),

                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request Review")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                }else if(result.status=="In-Review"){
                    ReviewStatusUI(result.domain)
                    Spacer(modifier = Modifier.height(4.dp))
                } else if(result.status == "Active"){
                    ApprovedStatusUI(result.domain)
                    Spacer(modifier = Modifier.height(4.dp))
                } else if(result.status=="Rejected"){
                    RejectedStatusUI(result.domain, result.rejectedReason)
                    Spacer(modifier = Modifier.height(4.dp))
                }


                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = MaterialTheme.colorScheme.onSurface,

                        ),

                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete")
                }


            } ?: run {


                if (verificationCode.isNullOrBlank()) {
                    // Generate Unique Code
                    Button(
                        onClick = { viewModel.generateVerificationCode(domain) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFF2A1F1C
                            ),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ), modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Verification Code")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                }

                verificationCode?.let {
                    MetaTagDomainVerificationCodeContainer(it)
                    Spacer(modifier = Modifier.height(8.dp))
                }


                // Verify Domain Button
                Button(
                    onClick = { viewModel.verifyDomain(domain) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(
                            0xFF1DBC60
                        ),
                        contentColor = MaterialTheme.colorScheme.onSurface,

                        ),

                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Domain")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }


        }

    }
}


@Composable
fun ReviewStatusUI(source:String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        .border(1.dp, Color.Yellow, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)) // Light Yellow Background
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Review Icon",
                tint = Color(0xFFFFA000), // Amber color
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("$source source is in review", fontWeight = FontWeight.Bold, color = Color.Black)
                Text("We are verifying your source. This may take a few days.", color = Color.Gray)
            }
        }
    }
}

@Composable
fun ApprovedStatusUI(source: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, Color.Green, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Light Green Background
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Approved Icon",
                tint = Color(0xFF2E7D32), // Green color
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Your ${source} is approved", fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Your source has been successfully verified & approved.", color = Color.Gray)
            }
        }
    }
}


@Composable
fun RejectedStatusUI(source:String, reason:String? ) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color.Red, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)) // Light Red Background
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Rejected Icon",
                tint = Color(0xFFD32F2F), // Red color
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Your $source is rejected", fontWeight = FontWeight.Bold, color = Color.Black)
                reason?.let {
                    Text(it, color = Color.Gray)
                }
            }



        }
    }
}



@Composable
fun MetaTagDomainVerificationCodeContainer(code: String) {

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithCache {
                onDrawWithContent {

                    // draw behind the content the vertical line on the left
                    drawLine(
                        color = Color.Yellow,
                        start = Offset.Zero,
                        end = Offset(0f, this.size.height),
                        strokeWidth = 1f
                    )

                    // draw the content
                    drawContent()
                }
            }
            .padding(8.dp)
    ) {
        // Copy Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString("<meta name=\"lts360-source-verification\" content=\"$code\" />"))
                }, modifier = Modifier.size(16.dp)) {
                    Icon(
                        Icons.Default.CopyAll, contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Instruction Text
        Text(
            "Meta Tag Verification",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(4.dp))

        // Instruction Text
        Text(
            "Add this meta tag verify your source.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium

        )

        Spacer(Modifier.height(4.dp))
        // Verification Code Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161b55), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Text(
                "<meta name=\"lts360-source-verification\" content=\"$code\" />",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}




