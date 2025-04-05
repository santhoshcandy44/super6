package com.lts360.compose.ui.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.lts360.compose.ui.main.viewmodels.PageSource
import com.lts360.compose.ui.onboarding.navhost.OnBoardingNavHost
import com.lts360.compose.ui.onboarding.navhost.OnBoardingScreen
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val  type= intent.getStringExtra("type")
        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {
                        if(type=="guest"){
                            OnBoardingNavHost("guest", OnBoardingScreen.LocationAccess)
                        }else{
                            OnBoardingNavHost()
                        }
                    }
                }
            }
        }

    }
}
