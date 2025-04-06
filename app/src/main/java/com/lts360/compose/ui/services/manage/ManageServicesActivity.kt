package com.lts360.compose.ui.services.manage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.lts360.compose.ui.services.manage.navhost.ManageServicesNavHost
import com.lts360.compose.ui.services.manage.navhost.ManageServicesRoutes
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageServicesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {

            AppTheme {
                Surface {
                    SafeDrawingBox {
                        ManageServicesNavHost{
                            this@ManageServicesActivity.finish()
                        }
                    }

                }
            }

        }

    }

}