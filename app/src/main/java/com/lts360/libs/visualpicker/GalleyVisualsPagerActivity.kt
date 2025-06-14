package com.lts360.libs.visualpicker

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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import com.lts360.libs.imagepicker.routes.GalleyImagesPagerRoutes
import com.lts360.libs.visualpicker.ui.LoadVisualGalleryWithPermissions
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class GalleyVisualsPagerActivity : ComponentActivity() {

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

                        LoadVisualGalleryWithPermissions(modifier = Modifier.padding(contentPadding)) {

                            val viewModel: VisualMediaPickerViewModel = koinViewModel()
                            val mediaItems by viewModel.mediaItems.collectAsState()
                            val selectedMediaItems = mediaItems.filter { it.isSelected }.map { it.uri }

                            val backStack = rememberNavBackStack(GalleyImagesPagerRoutes.GalleyImagesPager)

                            Column(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                                    NavDisplay(
                                        backStack = backStack,
                                        entryDecorators = listOf(
                                            rememberSceneSetupNavEntryDecorator(),
                                            rememberSavedStateNavEntryDecorator(),
                                            rememberViewModelStoreNavEntryDecorator()
                                        ),
                                        entryProvider = entryProvider {
                                            entry<GalleyImagesPagerRoutes.GalleyImagesPager> {
                                                if (isSingle) {
                                                    GallerySingleVisualPickerScreen(
                                                        onImagePicked = {
                                                            setResult(RESULT_OK, Intent().apply {
                                                                putExtra("data", it.toString())
                                                            })
                                                            finish()
                                                        },
                                                        onNavigateUpAlbum = {
                                                            backStack.add(GalleyImagesPagerRoutes.SelectedAlbumImages(it))
                                                        },
                                                        viewModel = viewModel
                                                    )
                                                } else {
                                                    GalleryMultipleVisualsPickerScreen(
                                                        onImagePicked = { imageMediaData ->
                                                            if (imageMediaData.isSelected || selectedMediaItems.size < maxItems) {
                                                                viewModel.updateImageMediaIsSelected(imageMediaData.id)
                                                            } else {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        "You can select up to $maxItems",
                                                                        duration = SnackbarDuration.Short
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        onNavigateUpAlbum = {
                                                            backStack.add(GalleyImagesPagerRoutes.SelectedAlbumImages(it))
                                                        },
                                                        viewModel = viewModel
                                                    )
                                                }
                                            }

                                            entry<GalleyImagesPagerRoutes.SelectedAlbumImages> { entry ->

                                                val groupedByFolder = viewModel.groupMediaFolders(mediaItems)
                                                val album = groupedByFolder[entry.album]

                                                album?.let { nonNullAlbum ->
                                                    if (isSingle) {
                                                        ShowAlbumVisualsPickerScreen(
                                                            album = entry.album,
                                                            items = viewModel.groupMediaDate(nonNullAlbum),
                                                            onPopStack = {
                                                                backStack.removeLastOrNull()
                                                            },
                                                            onImagePicked = {
                                                                setResult(RESULT_OK, Intent().apply {
                                                                    putExtra("data", it.toString())
                                                                })
                                                                finish()
                                                            }
                                                        )
                                                    } else {
                                                        GalleryMultipleVisualsPickerShowAlbumVisualsScreen(
                                                            album = entry.album,
                                                            items = viewModel.groupMediaDate(nonNullAlbum),
                                                            onImagePicked = { imageMediaData ->
                                                                if (imageMediaData.isSelected || selectedMediaItems.size < maxItems) {
                                                                    viewModel.updateImageMediaIsSelected(imageMediaData.id)
                                                                } else {
                                                                    scope.launch {
                                                                        snackbarHostState.showSnackbar(
                                                                            "You can select up to $maxItems",
                                                                            duration = SnackbarDuration.Short
                                                                        )
                                                                    }
                                                                }
                                                            },
                                                            onPopStack = {
                                                                backStack.removeLastOrNull()
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                selectedMediaItems.takeIf { it.isNotEmpty() }?.let {
                                    OutlinedButton(
                                        onClick = {
                                            setResult(
                                                RESULT_OK, Intent().apply {
                                                    putParcelableArrayListExtra("data", it as ArrayList<Uri>)
                                                })
                                            finish()
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("Add", color = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = it.size.toString(), color = Color.White)
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