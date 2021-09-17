package com.example.catsapp.api.models.res


data class FavouriteResponse(
    val created_at: String?,
    val id: Int?,
    val image: CatImageResponse?,
    val image_id: String?,
    val sub_id: String?,
    val user_id: String?
)