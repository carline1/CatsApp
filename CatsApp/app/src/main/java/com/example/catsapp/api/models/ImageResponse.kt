package com.example.catsapp.api.models


data class ImageResponse(
    val breedResponses: List<BreedResponse>,
    val id: String,
    val url: String,
)