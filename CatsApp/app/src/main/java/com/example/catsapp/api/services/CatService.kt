package com.example.catsapp.api.services

import com.example.catsapp.api.models.req.FavouriteRequest
import com.example.catsapp.api.models.req.VoteRequest
import com.example.catsapp.api.models.res.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface CatService {
    @GET("images/search")
    fun getImages(@QueryMap parameters: Map<String, String>): Single<List<CatImageResponse>>

    @POST("votes")
    fun sendVoteRequest(@Body voteRequest: VoteRequest): Single<BodyResponse>

    @GET("breeds")
    fun getBreeds(
        @Query("sub_id") sub_id: String,
        @Query("limit") limit: String,
        @Query("page") page: String
    ): Single<List<BreedFilterResponse>>

    @GET("categories")
    fun getCategories(
        @Query("sub_id") sub_id: String,
        @Query("limit") limit: String,
        @Query("page") page: String
    ): Single<List<CategoryFilterResponse>>

    @POST("favourites")
    fun sendFavouriteRequest(@Body favouriteRequest: FavouriteRequest): Single<BodyResponse>

    @GET("favourites")
    fun getFavourites(
        @Query("sub_id") sub_id: String,
        @Query("limit") limit: String,
        @Query("page") page: String
    ): Single<List<FavouriteResponse>>

    @DELETE("favourites/{favourite_id}")
    fun deleteFavourite(@Path("favourite_id") favourite_id: String): Single<BodyResponse>

    @GET("images/{image_id}")
    fun getImage(@Path("image_id") image_id: String): Single<CatImageResponse>

    @POST("images/upload")
    @Multipart
    fun sendImageRequest(
        @Part body: MultipartBody.Part,
        @Part("sub_id") sub_id: RequestBody
    ): Single<BodyResponse>

    @GET("images")
    fun getUploadImages(@QueryMap parameters: Map<String, String>): Single<List<CatImageResponse>>

    @DELETE("images/{image_id}")
    fun deleteUploadImage(@Path("image_id") image_id: String): Single<DeleteImageResponse>

    @GET("images/{image_id}/analysis")
    fun getImageAnalysis(@Path("image_id") image_id: String): Single<List<ImageAnalysisResponse>>
}