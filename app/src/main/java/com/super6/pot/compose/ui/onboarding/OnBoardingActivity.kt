package com.super6.pot.compose.ui.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.super6.pot.compose.ui.onboarding.navhost.OnBoardingNavHost
import com.super6.pot.compose.ui.onboarding.navhost.OnBoardingScreen
import com.super6.pot.compose.ui.theme.AppTheme
import com.super6.pot.compose.utils.SafeDrawingBox
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
                        if(type!=null && type=="guest"){
                            OnBoardingNavHost("guest",
                                OnBoardingScreen.LocationAccess)
                        }else{
                            OnBoardingNavHost()
                        }
                    }
                }
            }
        }

    }
}
