package com.example.catsapp.ui.fragments.catImages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.catsapp.api.models.req.FavouriteRequest
import com.example.catsapp.api.models.res.BodyResponse
import com.example.catsapp.api.models.res.CatImageResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class CatImagesViewModel(
    application: Application
) : AndroidViewModel(application) {

    init {
        application.appComponent.inject(this)
    }
    @Inject
    lateinit var catService: CatService

    val compositeDisposable = CompositeDisposable()

    private var requestParams = mutableMapOf<String, String>()

    private var _catImages = setupCatImagesPager()
    var catImages: LiveData<PagingData<CatImageResponse>> = _catImages
        private set

    fun setRequestParams(newRequestParams: MutableMap<String, String>) {
        requestParams = newRequestParams
    }

    private fun setupCatImagesPager() =
        Pager(PagingConfig(pageSize = CatsAppKeys.PAGE_SIZE, initialLoadSize = CatsAppKeys.INITIAL_LOAD_SIZE)) {
            CatImagesPagingSource(catService, requestParams)
        }.liveData.cachedIn(viewModelScope) as MutableLiveData<PagingData<CatImageResponse>>

    fun refreshCatImages() {
        _catImages = setupCatImagesPager()
        catImages = _catImages
    }

    fun sendFavouriteToServer(image_id: String): Single<BodyResponse> {
        return catService.sendFavouriteRequest(
            FavouriteRequest(
                image_id = image_id,
                sub_id = CatsAppKeys.SUB_ID
            )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}