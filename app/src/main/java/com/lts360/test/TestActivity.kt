package com.lts360.test


import android.app.Activity
import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.compose.ui.auth.repos.AuthRepository
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.libs.imagepicker.utils.redirectToAppSettings
import com.lts360.libs.visualpicker.GalleryVisualPagerActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*      var items by remember { mutableStateOf(emptyList<NewsArticle>()) }

                   val context = LocalContext.current

                   LaunchedEffect(Unit) {

                       val data = context.assets.open("recent_articles.json")
                           .bufferedReader()
                           .use {
                               it.readText()
                           }

                       val serializer = Json { ignoreUnknownKeys }
                       items = serializer.decodeFromString(data) as List<NewsArticle>
                   }*/


@Serializable
data class NewsArticle(
    val title: String,
    val description: String,
    val url: String,
    val thumbnail: String,
    @SerialName("published_date") val publishedDate: String
)


@AndroidEntryPoint
class TestActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {


            AppTheme {

                Surface {




                    val context = LocalContext.current
                    val launcher = rememberLauncherForActivityResult(
                        GalleryVisualPagerActivityResultContracts
                            .PickMultipleVisuals()
                    ) { uris ->

                    }

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clickable {
                                launcher.launch(Unit)
                            }
                    ) {


                        /*
                                                DonutChart(
                                                    listOf(10, 30, 40, 15, 5),
                                                    listOf(
                                                        Color(0xFF7B4F98),  // Purple
                                                        Color(0xFF34A853),  // Green
                                                        Color(0xFFFBBC05),  // Yellow
                                                        Color(0xFF4285F4),  // Blue
                                                        Color(0xFFDB4437)   // Red
                                                    ),
                                                    size = 500.dp,
                                                    modifier = Modifier.align(Alignment.Center)
                                                        .background(Color.Yellow)
                                                )*/

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

