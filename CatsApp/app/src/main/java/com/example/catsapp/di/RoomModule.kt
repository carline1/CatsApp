package com.example.catsapp.di

import android.app.Application
import androidx.room.Room
import com.example.catsapp.db.RoomCatsDatabase
import com.example.catsapp.db.RoomCatsRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule(private val application: Application) {

    @Provides
    fun provideApplication(): Application = application

    @Provides
    fun provideCatsDatabase(application: Application): RoomCatsDatabase =
        Room.databaseBuilder(
            application,
            RoomCatsDatabase::class.java,
            "cats_room_database"
        ).build()

    @Provides
    @Singleton
    fun provideRoomCatsRepository(roomCatsDatabase: RoomCatsDatabase): RoomCatsRepository =
        RoomCatsRepository(roomCatsDatabase.catsDao())
}