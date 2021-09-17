package com.example.catsapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.catsapp.db.dao.CatsDao
import com.example.catsapp.db.dao.FavouriteIdsEntity

@Database(
    entities = [
        FavouriteIdsEntity::class
    ],
    version = 1
)
abstract class RoomCatsDatabase : RoomDatabase() {

    abstract fun catsDao(): CatsDao
}