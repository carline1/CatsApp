package com.example.catsapp.ui.viewmodels

import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.*
import androidx.paging.rxjava3.cachedIn
import androidx.paging.rxjava3.observable
import com.example.catsapp.api.models.req.FavouriteRequest
import com.example.catsapp.api.models.req.VoteRequest
import com.example.catsapp.api.models.res.*
import com.example.catsapp.api.services.CatService
import com.example.catsapp.ui.adapters.CatImagePagingSource
import com.example.catsapp.ui.adapters.FavouriteCatsPagingSource
import com.example.catsapp.ui.adapters.LoadedCatsPagingSource
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream


class CatViewModel(
    private val catService: CatService,
) : ViewModel() {

    var requestParams = mutableMapOf<String, String>()
    var bundleFilterFragment = MutableLiveData<Bundle>()
    var breedList = listOf<BreedFilterResponse>()
    var categoryList = listOf<CategoryFilterResponse>()

    @ExperimentalCoroutinesApi
    fun getImages(): Observable<PagingData<CatImageResponse>> {
        return Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE)) {
                CatImagePagingSource(catService, requestParams)
        }.observable.cachedIn(viewModelScope)
    }

    @ExperimentalCoroutinesApi
    fun getFavourites(): Observable<PagingData<FavouriteResponse>> {
        return Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE)) {
            FavouriteCatsPagingSource(catService)
        }.observable.cachedIn(viewModelScope)
    }

    @ExperimentalCoroutinesApi
    fun getLoadedImages(query: MutableMap<String, String>): Observable<PagingData<CatImageResponse>> {
        return Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE)) {
            LoadedCatsPagingSource(catService, query)
        }.observable.cachedIn(viewModelScope)
    }

    fun deleteFavourite(favourite_id: String): Single<BodyResponse> {
        return catService.deleteFavourite(favourite_id)
    }

    fun deleteUploadImage(delete_id: String): Single<DeleteImageResponse> {
        return catService.deleteUploadImage(delete_id)
    }

    fun setVote(image_id: String?, value: Int): Single<BodyResponse> {
        return catService.sendVoteRequest(
            VoteRequest(
                image_id = image_id,
                sub_id = SUB_ID,
                value = value
            )
        )
    }

    fun getBreeds(): Single<List<BreedFilterResponse>> {
        return catService.getBreeds()
    }

    fun getCategories(): Single<List<CategoryFilterResponse>> {
        return catService.getCategories()
    }

    fun getBreedsAndCategories(): Single<Map<String, List<Any>>> {
        return Single.zip(
            getBreeds(),
            getCategories(),
            { breed: List<BreedFilterResponse>, categoryFilter: List<CategoryFilterResponse> ->
                mapOf(BREED_LIST_KEY to breed, CATEGORY_LIST_KEY to categoryFilter)}
        )
    }

    fun sendFavourite(image_id: String): Single<BodyResponse> {
        return catService.sendFavouriteRequest(
            FavouriteRequest(
                image_id = image_id,
                sub_id = SUB_ID
            )
        )
    }

    fun getImage(image_id: String): Single<CatImageResponse> {
        return catService.getImage(image_id = image_id)
    }

    fun sendImage(image: Bitmap): Single<BodyResponse> {
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val body = MultipartBody.Part.createFormData(
            "file", "photo_" + System.currentTimeMillis(),
            byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
        )
        val subId = SUB_ID.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        return catService.sendImageRequest(body, subId)
    }

    fun getImageAnalysis(image_id: String): Single<List<ImageAnalysisResponse>> {
        return catService.getImageAnalysis(image_id = image_id)
    }

    companion object BreedsAndCategoriesMapKeys {
        const val BREED_LIST_KEY = "breedList"
        const val CATEGORY_LIST_KEY = "categoryList"
        const val SUB_ID = "user162746871621874621874681"
        const val PAGE_SIZE = 16
        const val INITIAL_LOAD_SIZE = 16
    }
}


class CatsFragmentViewModelFactory(private val catService: CatService) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CatViewModel(catService) as T
    }
}