package com.example.catsapp.api.models.res


import com.google.gson.annotations.SerializedName

data class ImageAnalysisResponseLabel(
    @SerializedName("Confidence")
    val confidence: Double,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Parents")
    val imageAnalysisResponseParents: List<ImageAnalysisResponseParent>
)