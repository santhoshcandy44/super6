package com.super6.pot.ui.auth

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface

import com.google.firebase.auth.FirebaseAuth
import com.super6.pot.R
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.ui.account.InvalidSessionActivity
import com.super6.pot.ui.auth.navhost.AuthNavHost
import com.super6.pot.ui.auth.navhost.AuthScreen
import com.super6.pot.ui.main.MainActivity
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.ui.theme.AppTheme
import com.super6.pot.ui.utils.SafeDrawingBox
import com.super6.pot.utils.LogUtils.TAG
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isWelcome = intent.getBooleanExtra("is_welcome", false)
        val forceType = intent.getStringExtra("force_type")


        if(!isWelcome){
            if(UserSharedPreferencesManager.isInvalidSession){
                startActivity(
                    Intent(this, InvalidSessionActivity::class.java), ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                        .toBundle()
                )
                finishAffinity()
                return
            }
            if(forceType==null){
                when (tokenManager.getSignInMethod()) {


                    "legacy_email" -> {
                        val userId = UserSharedPreferencesManager.userId

                        Log.e(TAG,"userid: ${userId}")
                        val accessToken = tokenManager.getAccessToken()
                        val refreshToken = tokenManager.getRefreshToken()

                        if (accessToken.isNotEmpty() && refreshToken.isNotEmpty() && userId!=-1L) {
                            startActivity(
                                Intent(this, MainActivity::class.java), ActivityOptions.makeCustomAnimation(
                                    this,
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                                )
                                    .toBundle()
                            )
                            finishAffinity()
                        }

                    }

                    "google" -> {
                        val userId = UserSharedPreferencesManager.userId

                        val accessToken = tokenManager.getAccessToken()
                        val refreshToken = tokenManager.getRefreshToken()


                        // Get the instance of FirebaseAuth
                        val firebaseAuth = FirebaseAuth.getInstance()

                        // Check if the current user is not null

                        if (accessToken.isNotEmpty() && refreshToken.isNotEmpty() && firebaseAuth.currentUser != null && userId!=-1L) {
                            startActivity(
                                Intent(this, MainActivity::class.java), ActivityOptions.makeCustomAnimation(
                                    this,
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                                ).toBundle()
                            )
                            finishAffinity()
                        }

                    }

                    "guest" ->{

                        if(UserSharedPreferencesManager.userId!=-1L){
                            startActivity(
                                Intent(this, MainActivity::class.java), ActivityOptions.makeCustomAnimation(
                                    this,
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                                )
                                    .toBundle()
                            )
                            finishAffinity()
                        }

                    }

                }
            }
        }



        setContent {

            AppTheme {
                Surface {
                    SafeDrawingBox {
                        when(forceType){
                            "force_login"->{
                                AuthNavHost(AuthScreen.Login)
                            }
                            "force_register"->{
                                AuthNavHost(AuthScreen.SelectAccountType)
                            }
                            else->{
                                AuthNavHost()
                            }
                        }
                    }
                }
            }

        }

    }
}

