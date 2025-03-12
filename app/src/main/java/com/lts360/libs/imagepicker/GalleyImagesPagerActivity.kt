package com.lts360.libs.imagepicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.libs.imagepicker.routes.GalleyImagesPagerRoutes
import com.lts360.libs.imagepicker.ui.LoadImageGalleryWithPermissions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleyImagesPagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isSingle = intent.getBooleanExtra("is_single", true)
        val maxItems = intent.getIntExtra("max_items", 1)

        setContent {
            AppTheme {
                Surface {

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Black
                    ) { contentPadding ->


                        LoadImageGalleryWithPermissions(modifier = Modifier.padding(contentPadding)) {

                            val navController = rememberNavController()
                            NavHost(
                                navController = navController,
                                startDestination = GalleyImagesPagerRoutes.GalleyImagesPager(maxItems)
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
                                            }
                                        )
                                    } else {
                                        GalleyMultipleImagesPickerScreen(
                                            {
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
                                            {
                                                navController.navigate(
                                                    GalleyImagesPagerRoutes.SelectedAlbumImages(
                                                        it
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }

                                slideComposable<GalleyImagesPagerRoutes.SelectedAlbumImages> { backStackEntry ->
                                    val args =
                                        backStackEntry.toRoute<GalleyImagesPagerRoutes.SelectedAlbumImages>()
                                    val viewModel: ImagePickerViewModel = hiltViewModel(
                                        navController.getBackStackEntry<GalleyImagesPagerRoutes.GalleyImagesPager>()
                                    )

                                    val groupedByFolderMediaItems by viewModel.groupedByFolderMediaItems.collectAsState()

                                    val album = groupedByFolderMediaItems[args.album]
                                    album?.let { nonNullAlbum ->

                                        if (isSingle) {

                                            ShowAlbumPhotosScreen(args.album,
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
                                            val selectedGroupedByFolderMediaItems =
                                                groupedByFolderMediaItems.values.flatten()
                                                    .filter { it.isSelected }
                                                    .map { it.uri }

                                            GalleyMultipleImagesPickerShowAlbumPhotosScreen(
                                                viewModel,
                                                selectedGroupedByFolderMediaItems,
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
                                                            putParcelableArrayListExtra(
                                                                "data",
                                                                it as ArrayList<Uri>
                                                            )
                                                        })
                                                finish()
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
    }
}