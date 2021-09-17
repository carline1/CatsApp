package com.example.catsapp.db.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.catsapp.db.dao.FavouriteIdsEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class FavouriteIdsEntity(
    @PrimaryKey
    @ColumnInfo(name = "imageId")
    val imageId: String,

    @ColumnInfo(name = "favouriteId")
    val favouriteId: String
) {
    companion object {
        const val TABLE_NAME = "favourites_ids_table"
    }
}