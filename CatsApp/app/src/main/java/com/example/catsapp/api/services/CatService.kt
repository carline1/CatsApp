package com.example.catsapp.api.services

import com.example.catsapp.api.models.req.FavoriteRequest
import com.example.catsapp.api.models.req.VoteRequest
import com.example.catsapp.api.models.res.*
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface CatService {
    @GET("images/search")
    fun getImages(@QueryMap parameters: Map<String, String>): Single<List<CatImageResponse>>

    @POST("votes")
    fun sendVoteRequest(@Body voteRequest: VoteRequest): Single<VotePOSTResponse>

    @GET("breeds")
    fun getBreeds(): Single<List<BreedFilterResponse>>

    @GET("categories")
    fun getCategories(): Single<List<CategoryFilterResponse>>

    @POST("favourites")
    fun sendFavoriteRequest(@Body favoriteRequest: FavoriteRequest): Single<FavoritePOSTResponse>

    @GET("favourites")
    fun getFavorites(
        @Query("sub_id") sub_id: String,
        @Query("limit") limit: String,
        @Query("page") page: String
    ): Single<List<FavoriteResponse>>

    @DELETE("favourites/{favourite_id}")
    fun deleteFavorite(@Path("favourite_id") favourite_id: String): Single<FavoriteDELETEResponse>

    @GET("images/{image_id}")
    fun getImage(@Path("image_id") image_id: String): Single<CatImageResponse>
}