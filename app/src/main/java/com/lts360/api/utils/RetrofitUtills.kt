package com.lts360.api.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException



sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: Throwable) : Result<Nothing>()
}


sealed class ResultError(val errorMessage: String) {
    class ServerError(errorMessage: String = "Server error") : ResultError(errorMessage)

    class NoInternet(errorMessage: String = "No internet connection") : ResultError(errorMessage)

    class Timeout(errorMessage: String = "Request timed out. Please try again later") :
        ResultError(errorMessage)

    class ConnectionError(errorMessage: String = "Couldn't reach the server. Please try again") :
        ResultError(errorMessage)

    class Unknown(errorMessage: String = "An unknown error occurred") : ResultError(errorMessage)
}


fun mapExceptionToError(exception: Throwable): ResultError {
    return when (exception) {
        is UnknownHostException -> ResultError.ServerError()
        is SocketTimeoutException -> ResultError.Timeout()
        is ConnectException -> ResultError.ConnectionError()
        is NoInternetException -> ResultError.NoInternet()
        else -> ResultError.Unknown(exception.message ?: "An unknown error occurred")
    }
}



