package com.lts360.test


import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.lts360.App
import com.lts360.R
import com.lts360.compose.ui.auth.LoginScreen
import com.lts360.compose.ui.auth.RegisterScreen
import com.lts360.compose.ui.main.HomeScreen
import com.lts360.compose.ui.main.SearchBar
import com.lts360.compose.ui.profile.EditProfileSettingsScreen
import com.lts360.compose.ui.services.DetailedServiceScreen
import com.lts360.compose.ui.services.manage.CreateServiceScreen
import com.lts360.compose.ui.services.manage.EditServiceImagesScreen
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.usedproducts.DetailedUsedProductListingScreen
import com.lts360.compose.ui.usedproducts.manage.CreateUsedProductListingScreen
import com.lts360.compose.ui.usedproducts.manage.ManagePublishedUsedProductListingScreen
import com.lts360.compose.ui.usedproducts.manage.ManageUsedProductListingScreen
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin



@AndroidEntryPoint
class TestActivity : ComponentActivity() {



    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {
                        JobNavHost()
                    }
                }
            }
        }

    }


    @Composable
    fun BoxScope.CanvasTest() {

        val context = LocalContext.current

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {

                drawArc(
                    Color.LightGray,
                    sweepAngle = 180f,
                    startAngle = 180f,
                    useCenter = false,

                    style = Stroke(width = 16.dp.toPx()),
                    topLeft = Offset(0f, 0f)  // Position of the arc (adjust as needed)


                )

                val text = "90"
                val textPaint = Paint().apply {
                    color = context.getColor(R.color.colorAccent)
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }


                // Angle in degrees
                val angle = 240f

// Convert degrees to radians
                val radians = angle * (PI / 180f)

// Define the center point of the canvas
                val centerX = size.width / 2
                val centerY = size.height / 2

// Define the radius from the center
                val radius = 200f

// Calculate the new X and Y positions after rotation
                val pivotX = centerX + radius * cos(radians) // X coordinate after rotation
                val pivotY = centerY + radius * sin(radians) // Y coordinate after rotation


                // Rotate the canvas before drawing the text
                rotate(degrees = 0f, pivot = Offset(pivotX.toFloat(), pivotY.toFloat())) {
                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        pivotX.toFloat(),
                        pivotY.toFloat(),
                        textPaint
                    )
                }

                drawArc(
                    Color(0xFF9c46ff),
                    sweepAngle = 90f,
                    startAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 1.dp.toPx()),
                    topLeft = Offset(
                        0f - 16.dp.toPx(),
                        0f - 16.dp.toPx(),
                    )  // Position of the arc (adjust as needed)

                )

                val angleInDegrees = 45f  // You can change this value to the desired angle

                val angleInRadians = Math.toRadians(angleInDegrees.toDouble()).toFloat()

                val length = 200f  // Length of the line
                val endX = (size.width / 2) + length * cos(angleInRadians.toDouble()).toFloat()
                val endY = (size.height / 2) + length * sin(angleInRadians.toDouble()).toFloat()

                drawLine(
                    color = Color.Red,
                    start = Offset(
                        size.width / 2,
                        size.height / 2
                    ), // Start at the center of the canvas
                    end = Offset(endX, endY), // End at the top of the canvas (adjust as needed)
                    strokeWidth = 4f // Adjust the line thickness if desired
                )

            }
        }


    }

    @Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun DefaultPreview() {

        AppTheme {
            Surface {
            }
        }

    }
}

