package com.example.catsapp.di

import androidx.fragment.app.Fragment
import com.example.catsapp.MainActivity
import com.example.catsapp.api.services.ImagesService
import dagger.Component
import javax.inject.Singleton

@Component(modules = [RemoteModule::class])
@Singleton
interface AppComponent  {
    fun getImageService(): ImagesService
    fun inject(activity: MainActivity)
    fun inject(fragment: Fragment)
}