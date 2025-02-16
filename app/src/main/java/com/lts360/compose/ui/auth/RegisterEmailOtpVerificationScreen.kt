package com.lts360.compose.ui.auth

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.lts360.R
import com.lts360.compose.ui.auth.viewmodels.RegisterEmailOTPVerificationViewModel
import com.lts360.compose.ui.onboarding.OnBoardingActivity


@Composable
fun RegisterEmailOtpVerificationScreen(
    onPopBackStack:()-> Unit,
    viewModel: RegisterEmailOTPVerificationViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val isLoading: Boolean by viewModel.loading.collectAsState()
    val email = viewModel.email

    OtpVerificationScreen(
        viewModel,
        email,
        isLoading,
        {
            viewModel.onResendOtp(email,{
                Toast.makeText(
                    context,
                    "OTP has sent to $email",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                Toast.makeText(
                    context,
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }

        },
        { otp ->
            viewModel.onVerifyEmail(
                otp = otp,
                firstName = viewModel.firstName,
                lastName = viewModel.lastName,
                email = viewModel.email,
                password = viewModel.password,
                accountType = viewModel.accountType,
                onSuccess = {
                    context.startActivity(
                        Intent(context, OnBoardingActivity::class.java),
                        ActivityOptions.makeCustomAnimation(
                            context,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        ).toBundle()
                    )
                    (context as Activity).finishAffinity()
                },
            ) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }

        },
        onPopBackStack
        )


}


