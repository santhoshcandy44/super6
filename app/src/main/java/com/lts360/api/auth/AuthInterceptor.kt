package com.lts360.api.auth

import com.lts360.App
import com.lts360.api.auth.managers.CriticalListener
import com.lts360.api.auth.managers.RefreshTokenManager
import com.lts360.api.auth.managers.RetryListener
import com.lts360.api.auth.managers.TokenManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject


class AuthInterceptor @Inject constructor(
    private val app: App,
    private val tokenManager: TokenManager,
) : Interceptor {

    private val mutex = Mutex()

    private val waitingJobs = mutableListOf<Job>()

    private var parentJob = Job()
    var scope = CoroutineScope(Dispatchers.IO + parentJob)


    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val signInMethod = tokenManager.getSignInMethod()

        var response: Response

        when (signInMethod) {
            "legacy_email" -> {

                val authToken = tokenManager.getAccessToken()

                request = request.newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .build()

                response = chain.proceed(request)

                if (response.code == 401) {

                    val isFirstJob = waitingJobs.isEmpty()

                    val deferredResult = CompletableDeferred<Response>()

                    val childJob = scope.launch {

                        mutex.withLock {

                            RefreshTokenManager.onRefreshAccessToken(
                                tokenManager,
                                authToken,
                                object : CriticalListener {
                                    override fun onSuccess(newToken: String) {
                                        // Retry the original request with the new token
                                        response.close()
                                        request = request.newBuilder()
                                            .header("Authorization", "Bearer $newToken")
                                            .build()
                                        response = chain.proceed(request)
                                        deferredResult.complete(response)
                                    }

                                    override fun onError(e: Exception?) {
                                        if(e!=null){
                                            deferredResult.completeExceptionally(IOException("Unexpected behaviour"))
                                        }else{
                                            deferredResult.complete(response)
                                            parentJob.cancel()
                                        }
                                    }

                                    override fun onFailed(responseCode: Int) {
                                        deferredResult.complete(response)
                                    }
                                },
                                object : RetryListener {

                                    override fun onRetry(newToken: String) {
                                        try {
                                            // Retry the original request with the new token
                                            response.close()
                                            request = request.newBuilder()
                                                .header("Authorization", "Bearer $newToken")
                                                .build()
                                            response = chain.proceed(request)
                                            deferredResult.complete(response)
                                        }catch (_:Exception){
                                            deferredResult.completeExceptionally(IOException("Unexpected behaviour"))
                                        }
                                    }
                                }
                            )

                        }
                    }

                    waitingJobs.add(childJob)

                    childJob.invokeOnCompletion { cause ->
                        if (cause != null) {
                            deferredResult.completeExceptionally(IOException("Req cancelled"))
                        } else {
                            if (waitingJobs.all { p -> p.isCompleted }) {
                                parentJob.complete()
                                newWorkerBuild()
                            }
                        }
                    }

                    runBlocking {
                        try {
                            parentJob.join()
                            deferredResult.await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        } finally {
                            mutex.withLock {
                                waitingJobs.remove(childJob)
                            }
                        }
                    }


                    runBlocking {
                        mutex.withLock {
                            if (isFirstJob) {
                                if (childJob.isCancelled ) {
                                    if (response.code == 401) {
                                        app.setIsInvalidSession(true)
                                        throw IOException("Unexpected behaviour")
                                    }
                                }
                            } else {
                                if (childJob.isCancelled) {
                                    delay(1000)
                                    throw IOException("Job cancelled: Unexpected behaviour")
                                }
                            }
                        }
                    }
                }

                if(response.code==498){
                    runBlocking {
                        app.setIsInvalidSession(true)
                    }
                }

                if (response.code == 403) {
                    runBlocking {
                        app.logout(signInMethod)
                    }
                }
            }

            "google" -> {
                // Get the current ID token
                val authToken = tokenManager.getAccessToken()

                // Add Authorization header with the ID token
                request = request.newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .build()

                response = chain.proceed(request)

                if (response.code == 401) {

                    val isFirstJob = waitingJobs.isEmpty()

                    val deferredResult = CompletableDeferred<Response>()

                    val childJob = scope.launch {

                        mutex.withLock {

                            RefreshTokenManager.onRefreshAccessToken(
                                tokenManager,
                                authToken,
                                object : CriticalListener {
                                    override fun onSuccess(newToken: String) {
                                        // Retry the original request with the new token
                                        response.close()
                                        request = request.newBuilder()
                                            .header("Authorization", "Bearer $newToken")
                                            .build()
                                        response = chain.proceed(request)
                                        deferredResult.complete(response)
                                    }

                                    override fun onError(e: Exception?) {
                                        if(e!=null){
                                            deferredResult.completeExceptionally(IOException("Unexpected behaviour"))
                                        }else{
                                            deferredResult.complete(response)
                                            parentJob.cancel()
                                        }
                                    }

                                    override fun onFailed(responseCode: Int) {
                                        deferredResult.complete(response)
                                    }
                                },
                                object : RetryListener {

                                    override fun onRetry(newToken: String) {
                                        try {
                                            response.close()
                                            request = request.newBuilder()
                                                .header("Authorization", "Bearer $newToken")
                                                .build()
                                            response = chain.proceed(request)
                                            deferredResult.complete(response)
                                        }catch (_:Exception){
                                            deferredResult.completeExceptionally(IOException("Unexpected behaviour"))
                                        }
                                    }
                                }
                            )

                        }
                    }
                    waitingJobs.add(childJob)

                    childJob.invokeOnCompletion { cause ->

                        if (cause != null) {
                            deferredResult.completeExceptionally(IOException("Req cancelled"))
                        } else {

                            if (waitingJobs.all { p -> p.isCompleted }) {
                                parentJob.complete()
                                newWorkerBuild()
                            }
                        }
                    }

                    runBlocking {
                        parentJob.join()
                        try {
                            deferredResult.await()
                        } catch (e: Exception) {
                            throw e
                        } finally {
                            mutex.withLock {
                                waitingJobs.remove(childJob) // Remove the completed job
                            }
                        }
                    }

                    runBlocking {
                        mutex.withLock {
                            if (isFirstJob) {
                                if (childJob.isCancelled) {
                                    if (response.code == 401) {
                                        app.setIsInvalidSession(true)
                                        throw IOException("Unexpected behaviour")
                                    }
                                }
                            } else {
                                if (childJob.isCancelled) {
                                    delay(1000) //wai for
                                    throw IOException("Job cancelled: Unexpected behaviour")
                                }
                            }
                        }
                    }

                }

                if (response.code == 403) {
                    runBlocking {
                        app.logout(signInMethod)
                    }
                }


                if(response.code==498){
                    runBlocking {
                        app.setIsInvalidSession(true)
                    }
                }
            }

            "guest"->{
                response = chain.proceed(request)
            }
            else -> {
                response = chain.proceed(request)
                if (response.code == 401) {
                    runBlocking {
                        app.logout(signInMethod)
                    }
                }
            }
        }

        return response
    }

    fun clearJobs() {
        parentJob.cancel()
        newWorkerBuild()
    }

    private fun newWorkerBuild() {
        parentJob = Job()
        scope = CoroutineScope(Dispatchers.IO + parentJob)
    }

}

