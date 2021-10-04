package com.example.catsapp.api.models

sealed class Resource<T>() {
    class Success<T>(val data: T) : Resource<T>()
    class Loading<T> : Resource<T>()
    class Error<T> : Resource<T>()
}
