package com.example.catsapp.ui.fragments.loadedCats

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.catsapp.api.models.res.BodyResponse
import com.example.catsapp.api.models.res.CatImageResponse
import com.example.catsapp.api.models.res.DeleteImageResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class LoadedCatsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    init {
        application.appComponent.inject(this)
    }
    @Inject
    lateinit var catService: CatService

    val compositeDisposable = CompositeDisposable()

    private var _loadedImages = setupLoadedImagesPager()
    var loadedImages: LiveData<PagingData<CatImageResponse>> = _loadedImages
        private set

    private fun setupLoadedImagesPager() =
        Pager(PagingConfig(pageSize = CatsAppKeys.PAGE_SIZE, initialLoadSize = CatsAppKeys.INITIAL_LOAD_SIZE)) {
            val query = mutableMapOf(
                "order" to "asc",
                "sub_id" to CatsAppKeys.SUB_ID
            )
            LoadedCatsPagingSource(catService, query)
        }.liveData.cachedIn(viewModelScope) as MutableLiveData<PagingData<CatImageResponse>>

    fun refreshLoadedImages() {
        _loadedImages = setupLoadedImagesPager()
        loadedImages = _loadedImages
    }

    // LiveData
    fun deleteLoadedImageFromLiveData(catImageResponse: CatImageResponse?) {
        val pagingData = loadedImages.value ?: return
        pagingData
            .filter { catImageResponse?.id != it.id }
            .let { _loadedImages.value = it }
    }

    // Server
    fun sendImageToServer(image: Bitmap): Single<BodyResponse> {
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()

        val body = MultipartBody.Part.createFormData(
            "file", "photo_" + System.currentTimeMillis(),
            byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
        )
        val subId = CatsAppKeys.SUB_ID.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        return catService.sendImageRequest(body, subId)
    }

    fun deleteLoadedImageFromServer(delete_id: String): Single<DeleteImageResponse> {
        return catService.deleteUploadImage(delete_id)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}