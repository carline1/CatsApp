package com.example.catsapp.ui.fragments.catCard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.catsapp.api.models.req.VoteRequest
import com.example.catsapp.api.models.res.BodyResponse
import com.example.catsapp.api.models.res.CatImageResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class CatCardViewModel(
    application: Application
) : AndroidViewModel(application) {

    init {
        application.appComponent.inject(this)
    }
    @Inject
    lateinit var catService: CatService

    val compositeDisposable = CompositeDisposable()

    fun sendVoteToServer(image_id: String?, value: Int): Single<BodyResponse> {
        return catService.sendVoteRequest(
            VoteRequest(
                image_id = image_id,
                sub_id = CatsAppKeys.SUB_ID,
                value = value
            )
        )
    }

    fun getImageFromServer(image_id: String): Single<CatImageResponse> {
        return catService.getImage(image_id = image_id)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}