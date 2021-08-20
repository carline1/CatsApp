package com.example.catsapp.api.services

import com.example.catsapp.api.models.ImageResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap
import io.reactivex.rxjava3.core.Single

interface ImagesService {
    @GET("./images/search")
    fun getImages(@QueryMap parameters: Map<String, String>): Single<List<ImageResponse>>
}