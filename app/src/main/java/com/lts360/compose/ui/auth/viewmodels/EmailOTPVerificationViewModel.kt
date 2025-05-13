package com.lts360.compose.ui.auth.viewmodels

import android.os.CountDownTimer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.api.utils.mapExceptionToError
import com.lts360.compose.ui.auth.repos.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


open class EmailOTPVerificationViewModel(protected val authRepository: AuthRepository) :
    ViewModel() {


    private var countDownTimer: CountDownTimer? = null
    private val countDownTime: Long = 60000
    private var timeLeftInMillis: Long = countDownTime

    private val _otpFields = MutableStateFlow(List(6) { TextFieldValue( "", TextRange.Zero ) })
    val otpFields = _otpFields.asStateFlow()


    private val _timeLeft = MutableStateFlow("60s")
    val timeLeft = _timeLeft.asStateFlow()

    private val _resendEnabled = MutableStateFlow(false)
    val resendEnabled = _resendEnabled.asStateFlow()

    private val _timerVisible = MutableStateFlow(false)
    val timerVisible = _timerVisible.asStateFlow()


    private val _focusedIndex = MutableStateFlow(0)
    val focusedIndex = _focusedIndex.asStateFlow()


    private var otpErrorMessage: String = ""


    private val mutex = Mutex()


    init {
        startTimer()
    }


    // Timer logic
    private fun startTimer() {
        _timerVisible.value = true // Show timer layout
        countDownTimer = object : CountDownTimer(countDownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimer()
            }

            override fun onFinish() {
                _timerVisible.value = false // Show timer layout\
                _resendEnabled.value = true
                _timeLeft.value = "00s"
            }
        }.start()
    }

    private fun updateTimer() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        // Format the seconds with leading zero if needed
        _timeLeft.value = String.format("%02ds", seconds)
    }


    fun updateFocusedIndex(index: Int) {
        _focusedIndex.value = index
    }

    fun onUpdateOtpField(value: String, p: Int) {
        viewModelScope.launch {
            updateOtpField(value, p)
        }
    }


    private suspend fun updateOtpField(value: String, p: Int) {
        mutex.withLock {
            if (p in _otpFields.value.indices) {
                _otpFields.value = _otpFields.value.mapIndexed { index, currentValue ->
                    if (index == p) {
                        // Create a new TextFieldValue with the cursor at the end of the value
                        TextFieldValue(
                            text = value,
                            selection = TextRange(value.length)
                        )
                    } else {
                        currentValue // Keep the current value for other indices
                    }
                }
            }
        }
    }


    fun updateDigit(newValue: String, i: Int, onFocus: (Int) -> Unit) {
        val lastDigit = if (newValue.isEmpty()) ' ' else newValue.last()

        // Launch a coroutine to handle state updates
        viewModelScope.launch {
            // If the last character is a digit, update the current field
            if (!lastDigit.isWhitespace() && lastDigit.isDigit()) {
                // Update the current OTP field
                updateOtpField(lastDigit.toString(), i)

                // Handle the next field if it is empty
                if (i < _otpFields.value.size - 1 && _otpFields.value[i + 1].text.isEmpty()) {
                    // Clear next field
                    updateOtpField("", i + 1)
                    // Focus on the next field
                    onFocus(i + 1)
                }

            } else {
                // If the last character is whitespace, clear the next field
                if (lastDigit.isWhitespace()) {
                    // Clear next field
                    updateOtpField("", i)

                    // Move focus back to the previous field if not the first one
                    if (i != 0) {
                        onFocus(i - 1)
                    }
                }
            }
        }
    }


    private fun updateResendButtonEnabled(value: Boolean) {
        _resendEnabled.value = value
    }

    private fun timerVisibility(value: Boolean) {
        _timerVisible.value = value
    }

    private fun onStartTimer() {
        startTimer()

    }


    protected fun onRegisterReSendEmailVerificationOTP(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            authRepository.onRegisterReSendEmailVerificationOTP(email, onSuccess = {
                updateResendButtonEnabled(false)
                onStartTimer()
                timerVisibility(true)
                onSuccess()
            }) {
                timerVisibility(false)
                updateResendButtonEnabled(true)
                otpErrorMessage = mapExceptionToError(it).errorMessage
                onError(otpErrorMessage)
            }
        }
    }


    protected fun onEditEmailReSendEmailVerificationOTPValidUser(
        userId: Long,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            authRepository.onEditEmailReSendEmailVerificationOTPValidUser(
                userId,
                email, onSuccess = {
                    updateResendButtonEnabled(false)
                onStartTimer()
                timerVisibility(true)
                onSuccess()
            }) {
                timerVisibility(false)
                updateResendButtonEnabled(true)
                otpErrorMessage = mapExceptionToError(it).errorMessage
                onError(otpErrorMessage)
            }
        }
    }




    protected fun onForgotPasswordReSendEmailOtpVerification(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            authRepository.onForgotPasswordReSendEmailOtpVerification(
                email, onSuccess = {
                    updateResendButtonEnabled(false)
                    onStartTimer()
                    timerVisibility(true)
                    onSuccess()
                }) {
                timerVisibility(false)
                updateResendButtonEnabled(true)
                otpErrorMessage = mapExceptionToError(it).errorMessage
                onError(otpErrorMessage)
            }
        }
    }





    protected fun onProtectedForgotPasswordReSendEmailOtpVerificationValidUser(
        userId: Long,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            authRepository.onProtectedForgotPasswordReSendEmailOtpVerificationValidUser(
                userId,
                email, onSuccess = {
                    updateResendButtonEnabled(false)
                    onStartTimer()
                    timerVisibility(true)
                    onSuccess()
                }) {
                timerVisibility(false)
                updateResendButtonEnabled(true)
                otpErrorMessage = mapExceptionToError(it).errorMessage
                onError(otpErrorMessage)
            }
        }
    }




    override fun onCleared() {
        super.onCleared()
        if (countDownTimer != null) {
            countDownTimer!!.cancel() // Cancel the countdown timer when activity is stopped
        }
    }

}