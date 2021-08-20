package com.example.catsapp.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyRequestInterceptor : Interceptor {

    private val apiKey = "6701b0e7-3b4c-46fa-9b32-c4eac129079e"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .header("x-api-key", apiKey)
            .method(originalRequest.method, originalRequest.body)
            .build()

        return chain.proceed(newRequest)
    }
}