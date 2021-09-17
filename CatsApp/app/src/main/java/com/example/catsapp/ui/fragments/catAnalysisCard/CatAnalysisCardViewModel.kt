package com.example.catsapp.ui.fragments.catAnalysisCard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.catsapp.api.models.res.ImageAnalysisResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class CatAnalysisCardViewModel(
    application: Application
) : AndroidViewModel(application) {

    init {
        application.appComponent.inject(this)
    }
    @Inject
    lateinit var catService: CatService

    val compositeDisposable = CompositeDisposable()

    fun getImageAnalysisFromServer(image_id: String): Single<List<ImageAnalysisResponse>> {
        return catService.getImageAnalysis(image_id = image_id)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}