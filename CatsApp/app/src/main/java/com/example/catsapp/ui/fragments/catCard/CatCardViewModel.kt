package com.example.catsapp.ui.fragments.catCard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.catsapp.api.models.Resource
import com.example.catsapp.api.models.req.VoteRequest
import com.example.catsapp.api.models.res.BodyResponse
import com.example.catsapp.api.models.res.CatImageResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class CatCardViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    @Inject
    lateinit var catService: CatService

    private val compositeDisposable = CompositeDisposable()

    private val _getImageFromServerStatus = MutableLiveData<Resource<CatImageResponse>>()
    val getImageFromServerStatus: LiveData<Resource<CatImageResponse>> = _getImageFromServerStatus

    private val _sendVoteToServerStatus = MutableLiveData<BodyResponse>()
    val sendVoteToServerStatus: LiveData<BodyResponse> = _sendVoteToServerStatus

    private val imageId: String =
        savedStateHandle["imageId"] ?: throw IllegalArgumentException("Missing imageId arg")

    init {
        application.appComponent.inject(this)
    }

    fun sendVoteToServer(value: Int) {
        compositeDisposable.add(
            catService.sendVoteRequest(
                VoteRequest(
                    image_id = imageId,
                    sub_id = CatsAppKeys.SUB_ID,
                    value = value
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _sendVoteToServerStatus.value = it
                    Log.d("RETROFIT", "Successful vote sending -> ${it.message}, id: ${it.id}")
                }, {
                    Log.d("RETROFIT", "Exception during vote request -> ${it.localizedMessage}")
                })
        )
    }

    fun getImageFromServer() {
        compositeDisposable.add(catService.getImage(image_id = imageId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _getImageFromServerStatus.value = Resource.Loading() }
            .subscribe({
                _getImageFromServerStatus.value = Resource.Success(it)
                Log.d(
                    "RETROFIT",
                    "Successful getting cat image info from server -> ${it.url}, id: ${it.id}"
                )
            }, {
                _getImageFromServerStatus.value = Resource.Error()
                Log.d("RETROFIT", "Exception during image request -> ${it.localizedMessage}")
            })
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}