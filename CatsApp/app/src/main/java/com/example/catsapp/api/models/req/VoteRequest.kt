package com.example.catsapp.api.models.req

data class VoteRequest(
    val image_id: String?,
    val sub_id: String,
    val value: Int
)