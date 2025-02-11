package com.super6.pot.compose.ui.services.manage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.super6.pot.compose.ui.services.manage.navhost.ManageServicesNavHost
import com.super6.pot.compose.ui.services.manage.navhost.ManageServicesRoutes
import com.super6.pot.compose.ui.theme.AppTheme
import com.super6.pot.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageServicesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent

        val isNavigateService = data.getBooleanExtra("navigate_service", false)
        val serviceId = data.getLongExtra("service_id", -1)
        val type = data.getStringExtra("type")

        setContent {

            AppTheme {
                Surface {
                    SafeDrawingBox {
                        if (isNavigateService && serviceId != -1L && type != null) {
                            ManageServicesNavHost(ManageServicesRoutes.ManagePublishedService){
                                this@ManageServicesActivity.finish()
                            }

                        } else {
                            ManageServicesNavHost{
                                this@ManageServicesActivity.finish()
                            }
                        }
                    }

                }
            }

        }

    }

}