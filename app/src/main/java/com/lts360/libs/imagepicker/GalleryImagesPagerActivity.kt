package com.lts360.libs.imagepicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import com.lts360.libs.imagepicker.routes.GalleyImagesPagerRoutes
import com.lts360.libs.imagepicker.ui.LoadImageGalleryWithPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GalleryImagesPagerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.apply {
            WindowInsetsControllerCompat(this, decorView).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }

        val isSingle = intent.getBooleanExtra("is_single", true)
        val maxItems = intent.getIntExtra("max_items", 1)
        setContent {
            GalleryImagesPager(maxItems, isSingle)
        }
    }

    @Composable
    fun GalleryImagesPager(maxItems: Int, isSingle: Boolean) {
        AppTheme {
            Surface {

                SafeDrawingBox(
                    statusBarColor = Color.Black,
                    navigationBarColor = Color.Black
                ) {
                    val scope = rememberCoroutineScope()
                    val snackbarHostState = remember { SnackbarHostState() }

                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Black
                    ) { contentPadding ->


                        LoadImageGalleryWithPermissions(modifier = Modifier.padding(contentPadding)) {

                            val navController = rememberNavController()

                            val viewModel: ImagePickerViewModel = hiltViewModel()


                            val mediaItems by viewModel.mediaItems.collectAsState()

                            val selectedMediaItems =
                                mediaItems.filter { it.isSelected }
                                    .map { it.uri }




                            Column(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                ) {
                                    NavHost(
                                        navController = navController,
                                        startDestination = GalleyImagesPagerRoutes.GalleyImagesPager
                                    ) {

                                        slideComposable<GalleyImagesPagerRoutes.GalleyImagesPager> {

                                            if (isSingle) {
                                                GalleySingleImagePickerScreen(
                                                    {
                                                        setResult(
                                                            RESULT_OK, Intent()
                                                                .apply {
                                                                    putExtra("data", it.toString())
                                                                })
                                                        finish()
                                                    },
                                                    {
                                                        navController.navigate(
                                                            GalleyImagesPagerRoutes.SelectedAlbumImages(
                                                                it
                                                            )
                                                        )
                                                    },
                                                    viewModel
                                                )
                                            } else {
                                                GalleyMultipleImagesPickerScreen(
                                                    { imageMediaData ->

                                                        if (imageMediaData.isSelected || selectedMediaItems.size < maxItems) {
                                                            viewModel.updateImageMediaIsSelected(
                                                                imageMediaData.id
                                                            )
                                                        } else {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    "You can select up to $maxItems",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                    },
                                                    {
                                                        navController.navigate(
                                                            GalleyImagesPagerRoutes.SelectedAlbumImages(
                                                                it
                                                            )
                                                        )
                                                    },
                                                    viewModel
                                                )
                                            }
                                        }

                                        slideComposable<GalleyImagesPagerRoutes.SelectedAlbumImages> { backStackEntry ->

                                            val args =
                                                backStackEntry.toRoute<GalleyImagesPagerRoutes.SelectedAlbumImages>()

                                            val groupedByFolderMediaItems =
                                                viewModel.groupMediaFolders(mediaItems)
                                            val album = groupedByFolderMediaItems[args.album]

                                            album?.let { nonNullAlbum ->

                                                if (isSingle) {
                                                    ShowAlbumPhotosScreen(
                                                        args.album,
                                                        viewModel.groupMediaDate(
                                                            nonNullAlbum
                                                        ),
                                                        {
                                                            navController.popBackStack()
                                                        }) {
                                                        setResult(
                                                            RESULT_OK, Intent()
                                                                .apply {
                                                                    putExtra("data", it.toString())
                                                                })
                                                        finish()
                                                    }
                                                } else {

                                                    GalleyMultipleImagesPickerShowAlbumPhotosScreen(
                                                        args.album,
                                                        viewModel.groupMediaDate(
                                                            nonNullAlbum
                                                        ),
                                                        { imageMediaData ->
                                                            if (imageMediaData.isSelected || selectedMediaItems.size < maxItems) {
                                                                viewModel.updateImageMediaIsSelected(
                                                                    imageMediaData.id
                                                                )
                                                            } else {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        "You can select up to $maxItems",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        {
                                                            navController.popBackStack()
                                                        })
                                                }


                                            }


                                        }
                                    }
                                }

                                selectedMediaItems.takeIf { it.isNotEmpty() }?.let {
                                    OutlinedButton(
                                        onClick = {
                                            setResult(
                                                RESULT_OK, Intent()
                                                    .apply {
                                                        putParcelableArrayListExtra(
                                                            "data",
                                                            it as ArrayList<Uri>
                                                        )
                                                    })
                                            finish()
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        // "Add" Text
                                        Text("Add", color = Color.White)

                                        // Spacer for spacing between the text and the count box
                                        Spacer(Modifier.width(8.dp))

                                        // Rounded box for showing the count of selected items
                                        Text(
                                            text = it.size.toString(),  // Show the count
                                            color = Color.White
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

}