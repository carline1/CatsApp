package com.example.catsapp.api.models.res


import com.google.gson.annotations.SerializedName

data class ImageAnalysisResponse(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("image_id")
    val imageId: String?,
    @SerializedName("labels")
    val imageAnalysisResponseLabels: List<ImageAnalysisResponseLabel>?,
    val vendor: String?
)