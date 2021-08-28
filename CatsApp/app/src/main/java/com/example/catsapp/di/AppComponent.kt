package com.example.catsapp.di

import androidx.fragment.app.Fragment
import com.example.catsapp.ui.activities.MainActivity
import com.example.catsapp.api.services.CatService
import dagger.Component
import javax.inject.Singleton

@Component(modules = [RemoteModule::class])
@Singleton
interface AppComponent  {
    fun getCatService(): CatService
    fun inject(activity: MainActivity)
    fun inject(fragment: Fragment)
}