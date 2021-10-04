package com.example.catsapp.ui.fragments.catAnalysisCard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.catsapp.api.models.Resource
import com.example.catsapp.api.models.res.ImageAnalysisResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class CatAnalysisCardViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    @Inject
    lateinit var catService: CatService

    private val compositeDisposable = CompositeDisposable()

    private val _getImageAnalysisFromServerStatus =
        MutableLiveData<Resource<ImageAnalysisResponse>>()
    val getImageAnalysisFromServerStatus: LiveData<Resource<ImageAnalysisResponse>> =
        _getImageAnalysisFromServerStatus

    private val id: String =
        savedStateHandle["id"] ?: throw IllegalArgumentException("Missing id arg")

    init {
        application.appComponent.inject(this)
    }

    fun getImageAnalysisFromServer() {
        compositeDisposable.add(catService.getImageAnalysis(image_id = id)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { _getImageAnalysisFromServerStatus.value = Resource.Loading() }
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                _getImageAnalysisFromServerStatus.value = Resource.Success(it[0])
                Log.d(
                    "RETROFIT", "Successful getting uploaded image analysis from server -> " +
                            "id: ${it[0].imageId}, created at: ${it[0].createdAt}"
                )
            }, {
                _getImageAnalysisFromServerStatus.value = Resource.Error()
                Log.d(
                    "RETROFIT",
                    "Exception during imageAAnalysis request -> ${it.localizedMessage}"
                )
            })
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}

