package com.example.catsapp.di

import android.app.Application
import android.content.Context

class CatsApp : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent
            .builder()
            .roomModule(RoomModule(this))
            .build()
    }
}

val Context.appComponent: AppComponent
    get() = when (this) {
        is CatsApp -> appComponent
        else -> this.applicationContext.appComponent
    }