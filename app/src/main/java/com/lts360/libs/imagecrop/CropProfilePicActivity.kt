package com.lts360.libs.imagecrop

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import com.lts360.BuildConfig
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class CropProfilePicActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.apply {
            WindowInsetsControllerCompat(this, decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val uri = intent.data
        if (uri == null) {
            setResult(RESULT_OK, null)
            finish()
            return
        }
        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox(statusBarColor = Color.Black, navigationBarColor = Color.Black){
                        CropScreen(uri, { bitmap ->
                            setResult(
                                RESULT_OK, Intent()
                                    .apply {
                                        data = bitmap?.let { nonNullBitmap ->
                                            saveBitmapToCache(
                                                this@CropProfilePicActivity,
                                                nonNullBitmap
                                            )?.let {
                                                FileProvider.getUriForFile(
                                                    this@CropProfilePicActivity,
                                                    "${BuildConfig.APPLICATION_ID}.provider",
                                                    it
                                                )
                                            }
                                        }
                                    })

                            finish()
                        })
                    }
                }
            }
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): File? {
        try {
            // Create a file in the cache directory
            val file = File(context.cacheDir, "cropped_image.jpg")

            // Create a FileOutputStream to write the bitmap to the file
            val outputStream = FileOutputStream(file)

            // Compress the bitmap to the file (use JPEG or PNG format)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            // Close the output stream
            outputStream.flush()
            outputStream.close()

            // Return the saved file
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

}


class CropProfilePicActivityContracts {


    class ImageCropper : ActivityResultContract<Uri, Uri?>() {

        override fun createIntent(context: Context, input: Uri): Intent {
            return Intent(context, CropProfilePicActivity::class.java)
                .apply {
                    data = input
                }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (resultCode == Activity.RESULT_OK) {
                intent?.data
            } else {
                null
            }
        }
    }
}
