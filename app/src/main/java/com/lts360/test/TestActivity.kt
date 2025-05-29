package com.lts360.test


import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lts360.R
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.app.notifications.NotificationIdManager
import com.lts360.app.services.FirebaseMessagingService
import com.lts360.compose.ui.bookmarks.BookmarksActivity
import com.lts360.compose.ui.chat.ChatPanel
import com.lts360.compose.ui.chat.panel.message.ChatOtherMessageItem
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.localjobs.manage.CreateLocalJobScreen
import com.lts360.compose.ui.localjobs.manage.ManagePublishedLocalJobApplicantsScreen
import com.lts360.compose.ui.main.prefs.BoardsSetupActivity
import com.lts360.compose.ui.profile.EditProfileFirstNameScreen
import com.lts360.compose.ui.profile.EditProfileLastNameScreen
import com.lts360.compose.ui.profile.EditProfileSettingsScreen
import com.lts360.compose.ui.settings.SettingsScreen
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.usedproducts.DetailedUsedProductListingScreen
import com.lts360.compose.ui.usedproducts.manage.ManageUsedProductListingScreen
import com.lts360.compose.utils.ExpandableText
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TestActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        /* setContent {
              AppTheme {
                  Surface {
                      SafeDrawingBox {
                          *//*   JobSearchScreenWithSmartDropdowns(onSearchClick = { role, location ->
                               println("Searching for $role jobs in $location")
                           }, onPopUp = {

                           })*//*


                    }
                }
            }
        }*/
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




