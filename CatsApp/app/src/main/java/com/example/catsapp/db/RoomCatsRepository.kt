package com.example.catsapp.db

import com.example.catsapp.db.dao.CatsDao
import com.example.catsapp.db.dao.FavouriteIdsEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class RoomCatsRepository(private val catsDao: CatsDao) {

    fun loadAllFavouriteEntities(): Single<List<FavouriteIdsEntity>> = catsDao.loadAllFavouriteEntities()
    fun insertAll(favouriteIdsEntityList: List<FavouriteIdsEntity>) = catsDao.insertAll(favouriteIdsEntityList)
    fun insertFavourite(favouriteIdsEntity: FavouriteIdsEntity): Completable = catsDao.insertFavourite(favouriteIdsEntity)
    fun deleteFavourite(imageId: String): Completable = catsDao.deleteFavourite(imageId)

}