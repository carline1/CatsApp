package com.example.catsapp.di

import com.example.catsapp.ui.fragments.CatsImagesFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = [RemoteModule::class])
@Singleton
interface AppComponent  {
    fun inject(fragment: CatsImagesFragment)
}