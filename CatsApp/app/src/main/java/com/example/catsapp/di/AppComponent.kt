package com.example.catsapp.di

import com.example.catsapp.ui.fragments.catAnalysisCard.CatAnalysisCardViewModel
import com.example.catsapp.ui.fragments.catCard.CatCardViewModel
import com.example.catsapp.ui.fragments.catImages.CatImagesViewModel
import com.example.catsapp.ui.fragments.favouriteCats.FavouriteCatsViewModel
import com.example.catsapp.ui.fragments.filter.FilterViewModel
import com.example.catsapp.ui.fragments.loadedCats.LoadedCatsViewModel
import dagger.Component
import javax.inject.Singleton

@Component(modules = [RemoteModule::class, RoomModule::class])
@Singleton
interface AppComponent  {
    fun inject(viewModel: CatImagesViewModel)
    fun inject(viewModel: FavouriteCatsViewModel)
    fun inject(viewModel: LoadedCatsViewModel)
    fun inject(viewModel: FilterViewModel)
    fun inject(viewModel: CatCardViewModel)
    fun inject(viewModel: CatAnalysisCardViewModel)
}