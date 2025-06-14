package com.lts360.compose.ui.profile

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.lts360.compose.ui.auth.OtpVerificationScreen
import com.lts360.compose.ui.profile.viewmodels.EditEmailEmailOTPVerificationViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun EditEmailEmailOtpVerificationScreen(
    onNavigatePop:()->Unit,
    onPopBackStack:()-> Unit,
    viewModel: EditEmailEmailOTPVerificationViewModel = koinViewModel(),

    ){

    val userId = viewModel.userId
    val email = viewModel.email

    val context= LocalContext.current
    val isLoading by viewModel.loading.collectAsState()

    OtpVerificationScreen(viewModel,email,isLoading,{
        viewModel.onResendOtp(
            userId,
            email,{
            Toast.makeText(context,"Otp has been sent to $email",Toast.LENGTH_SHORT)
                .show()
        }){

            Toast.makeText(context,it,Toast.LENGTH_SHORT)
                .show()

        }},

        {otp->
            viewModel.onVerifyEmailEditEmail(
                viewModel.userId,
                viewModel.email,
                otp,
                onSuccess = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                        .show()
                    onNavigatePop()
                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }
        }, onPopBackStack)

}

