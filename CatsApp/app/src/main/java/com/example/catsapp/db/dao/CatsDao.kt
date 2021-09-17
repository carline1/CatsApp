package com.example.catsapp.db.dao

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface CatsDao {

    @Query("SELECT * FROM ${FavouriteIdsEntity.TABLE_NAME}")
    fun loadAllFavouriteEntities(): Single<List<FavouriteIdsEntity>>

    @Insert(entity = FavouriteIdsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertFavourite(favouriteIdsEntity: FavouriteIdsEntity): Completable

    @Insert(entity = FavouriteIdsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertAll(favouriteIdsEntityList: List<FavouriteIdsEntity>): Completable

    @Query("DELETE FROM ${FavouriteIdsEntity.TABLE_NAME} WHERE imageId like :imageId")
    fun deleteFavourite(imageId: String): Completable

//    @Query("DELETE FROM ${FavouriteIdsEntity.TABLE_NAME} WHERE favouriteId like :favouriteId")
//    fun deleteFavouriteUsingFavouriteId(favouriteId: String): Completable

}