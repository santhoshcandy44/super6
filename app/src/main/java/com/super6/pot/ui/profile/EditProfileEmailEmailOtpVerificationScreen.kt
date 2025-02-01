package com.super6.pot.ui.profile

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.super6.pot.ui.auth.OtpVerificationScreen
import com.super6.pot.ui.profile.viewmodels.EditEmailEmailOTPVerificationViewModel


@Composable
fun EditEmailEmailOtpVerificationScreen(
    onNavigatePop:()->Unit,
    onPopBackStack:()-> Unit,
    viewModel: EditEmailEmailOTPVerificationViewModel = hiltViewModel(),

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

