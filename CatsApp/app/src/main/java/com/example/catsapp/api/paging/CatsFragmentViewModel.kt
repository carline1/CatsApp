package com.example.catsapp.api.paging

import androidx.lifecycle.*
import androidx.paging.*
import androidx.paging.rxjava3.cachedIn
import androidx.paging.rxjava3.observable
import com.example.catsapp.api.models.ImageResponse
import com.example.catsapp.api.services.ImagesService
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi

class CatsFragmentViewModel(
    imagesService: ImagesService
) : ViewModel() {

    @ExperimentalCoroutinesApi
    val images: Observable<PagingData<ImageResponse>> = Pager(PagingConfig(pageSize = 10))
    {
        CatImagePagingSource(imagesService)
    }.observable.cachedIn(viewModelScope)
}


class CatsFragmentViewModelFactory(private val imagesService: ImagesService) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CatsFragmentViewModel(imagesService) as T
    }
}