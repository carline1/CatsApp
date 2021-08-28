package com.example.catsapp.api.models.res


data class CatImageResponse(
    val breeds: List<BreedResponse>?,
    val id: String,
    val url: String,
)