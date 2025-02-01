package com.super6.pot.ui.chat

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.super6.pot.ui.enterFullScreenMode
import com.super6.pot.ui.exitFullScreenMode
import com.super6.pot.ui.findActivity
import com.super6.pot.ui.theme.AppTheme


class ImagesSliderActivity : ComponentActivity(){



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        // Change the status bar color (example with cyan color)

        windowInsetsController.isAppearanceLightStatusBars = false // Light icons on dark background
        window.apply {
            statusBarColor=Color.Transparent.toArgb()
        }

        enterFullScreenMode(this)

        val data = intent.data
        val imageWidth = intent.getIntExtra("imageWidth",0)
        val imageHeight = intent.getIntExtra("imageHeight",0)

        setContent {
            AppTheme {
                data?.let {
                    ImagesSliderScreen(it, imageWidth, imageHeight,{
                        this@ImagesSliderActivity.finish()
                    })
                }
            }
        }
    }

}





@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesSliderScreen(
    imageUri: Uri,
    imageWidth:Int,
    imageHeight:Int,
    onPopBackStack:()-> Unit,
    modifier: Modifier = Modifier
) {
    // Get context and initialize ExoPlayer
    val context = LocalContext.current


    var controlsVisible by remember { mutableStateOf(true) }

    // Function to hide controls immediately
    fun hideControls() {
        enterFullScreenMode(context.findActivity())
        controlsVisible = false  // Hide controls
    }

    // Handler and runnable to hide controls after 10 seconds of inactivity
    val handler = remember { Handler(Looper.getMainLooper()) }
    val runnable = remember {
        Runnable {
            hideControls()
        }
    }

    // Function to show controls and reset the inactivity timer
    fun showControls() {
        controlsVisible = true  // Show controls immediately
        handler.removeCallbacks(runnable) // Reset previous timer
        handler.postDelayed(runnable, 5000) // Hide controls after 10 seconds of inactivity
    }



    LaunchedEffect(Unit) {
        showControls()  // Initially show controls
    }

    var scale by rememberSaveable { mutableFloatStateOf(1f) }

    // Save x and y components of the offset separately
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }

    // Composable layout
    Box(
        modifier = modifier
            .clip(RectangleShape) // Clip the box content
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit){
                detectTapGestures(
                    onTap = {
                        // If controls are visible, hide them
                        if (controlsVisible) {
                            hideControls()  // Hide controls immediately
                        } else {
                            exitFullScreenMode(context.findActivity())
                            showControls()  // Show controls and reset the timer
                        }
                    }
                )
            }
            .pointerInput(Unit) {

                detectTransformGestures { _, pan, zoom, _ ->
                    // Handle zoom
                    scale *= zoom
                    // Handle pan (drag)
                    offsetX += pan.x
                    offsetY += pan.y
                }


            },
        contentAlignment = Alignment.Center
    ) {


        if(imageWidth>0 && imageHeight>0){




            AsyncImage(

                model = imageUri,
                contentDescription = "Image",

                modifier = Modifier
                    .align(Alignment.Center) // keep the image centralized into the Box
                    .aspectRatio(imageWidth.toFloat()/imageHeight.toFloat())

                    .graphicsLayer(
                        // Zooming with bounds
                        scaleX = maxOf(.5f, minOf(10f, scale)),
                        scaleY = maxOf(.5f, minOf(10f, scale)),
                        // Apply dragging offsets
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                )


        }



        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .systemBarsPadding()
        ) {

            // Animated TopAppBar
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 100) // Set your desired fade-in duration here
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 500) // Set your desired fade-out duration here
                )
            ) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onPopBackStack) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Text("Photo", style = MaterialTheme.typography.titleMedium)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White)
                )
            }

        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .systemBarsPadding()) {

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 100) // Set your desired fade-in duration here
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 500) // Set your desired fade-out duration here
                )
            ) {

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                    }

                    //Controls (Play/Pause, Forward, Backward)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {



                    }
                }

            }

        }
    }
}
