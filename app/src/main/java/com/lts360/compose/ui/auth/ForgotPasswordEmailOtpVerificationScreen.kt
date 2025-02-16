package com.lts360.compose.ui.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.lts360.compose.ui.auth.viewmodels.ForgotPasswordEmailOTPVerificationViewModel


@Composable
fun ForgotPasswordEmailOtpVerification(
    onNavigateUp: (String,String) -> Unit,
    onPopBackStack:()-> Unit,
    viewModel: ForgotPasswordEmailOTPVerificationViewModel = hiltViewModel(),

    ) {

    val context = LocalContext.current
    val isLoading by viewModel.loading.collectAsState()
    val email = viewModel.email



    OtpVerificationScreen(
        viewModel,
        email,
        isLoading,
        {
         viewModel.onResendOtp(email, {
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
            viewModel.onVerifyEmailForgotPassword(
                otp = otp, onSuccess = { a,b->
                    onNavigateUp(a,b)
                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }, onPopBackStack)
}


@Composable
fun ForgotPasswordEmailOtpVerificationProtected(
    onNavigateUp: (String,String) -> Unit,
    onPopBackStack:()-> Unit,
    viewModel: ForgotPasswordEmailOTPVerificationViewModel = hiltViewModel(),

    ) {

    val context = LocalContext.current
    val isLoading by viewModel.loading.collectAsState()
    val userId = viewModel.userId
    val email = viewModel.email



    OtpVerificationScreen(
        viewModel,
        email,
        isLoading,
        {
            viewModel.onProtectedResendOtp(
                userId,
                email, {
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
            viewModel.onProtectedVerifyEmailForgotPassword(
                userId,
                otp = otp, onSuccess = { a,b->
                    onNavigateUp(a,b)
                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }}, onPopBackStack)

}
