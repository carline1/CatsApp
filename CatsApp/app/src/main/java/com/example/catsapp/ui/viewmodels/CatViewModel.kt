package com.example.catsapp.ui.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.*
import com.example.catsapp.api.models.req.FavouriteRequest
import com.example.catsapp.api.models.req.VoteRequest
import com.example.catsapp.api.models.res.*
import com.example.catsapp.api.services.CatService
import com.example.catsapp.ui.adapters.CatImagePagingSource
import com.example.catsapp.ui.adapters.FavouriteCatsPagingSource
import com.example.catsapp.ui.adapters.LoadedCatsPagingSource
import io.reactivex.rxjava3.core.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream


class CatViewModel(
    application: Application,
    private val catService: CatService,
) : AndroidViewModel(application) {

    var requestParams = mutableMapOf<String, String>()
    val bundleFilterFragment = MutableLiveData<Bundle>()
    var breedList = listOf<BreedFilterResponse>()
    var categoryList = listOf<CategoryFilterResponse>()

    private val _favouritesLocalStorage = loadFavFromStorage()
    val favouritesLocalStorage: Set<String> = _favouritesLocalStorage

    // Cat Images
    private var _catImages = setupCatImagesPager()
    var catImages: LiveData<PagingData<CatImageResponse>> = _catImages
        private set

    private fun setupCatImagesPager() = Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE)) {
        CatImagePagingSource(catService, requestParams)
    }.liveData.cachedIn(viewModelScope) as MutableLiveData<PagingData<CatImageResponse>>

    fun refreshCatImages() {
        _catImages = setupCatImagesPager()
        catImages = _catImages
    }

    fun getImageFromServer(image_id: String): Single<CatImageResponse> {
        return catService.getImage(image_id = image_id)
    }

    // Favorites
    private var _favourites = setupFavouritePager()
    var favourites: LiveData<PagingData<FavouriteResponse>> = _favourites
        private set

    private fun setupFavouritePager() = Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE)) {
        FavouriteCatsPagingSource(catService)
    }.liveData.cachedIn(viewModelScope) as MutableLiveData<PagingData<FavouriteResponse>>

    fun deleteFavouriteFromLiveData(favouriteResponse: FavouriteResponse?) {
        val pagingData = favourites.value ?: return
        pagingData
            .filter { favouriteResponse?.id != it.id }
            .let { _favourites.value = it }
    }

    fun refreshFavourites() {
        _favourites = setupFavouritePager()
        favourites = _favourites
    }

    fun sendFavouriteToServer(image_id: String): Single<BodyResponse> {
        return catService.sendFavouriteRequest(
            FavouriteRequest(
                image_id = image_id,
                sub_id = SUB_ID
            )
        )
    }

    fun deleteFavouriteFromServer(favourite_id: String): Single<BodyResponse> {
        return catService.deleteFavourite(favourite_id)
    }

    private fun saveFavToStorage() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            putStringSet(FAVOURITE_SET_KEY, favouritesLocalStorage)
            apply()
        }
    }

    fun addFavToStorage(id: String) {
        _favouritesLocalStorage.add(id)
        saveFavToStorage()
    }

    fun deleteFavFromStorage(id: String?) {
        _favouritesLocalStorage.remove(id)
        saveFavToStorage()
    }

    private fun loadFavFromStorage(): MutableSet<String> {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE)
        return sharedPreferences.getStringSet(FAVOURITE_SET_KEY, mutableSetOf<String>())
            ?: mutableSetOf()
    }

    // Upload Images
    private var _loadedImages = setupLoadedImagesPager()
    var loadedImages: LiveData<PagingData<CatImageResponse>> = _loadedImages
        private set

    private fun setupLoadedImagesPager() =
        Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE)) {
        val query = mutableMapOf(
            "order" to "asc",
            "sub_id" to SUB_ID
        )
        LoadedCatsPagingSource(catService, query)
    }.liveData.cachedIn(viewModelScope) as MutableLiveData<PagingData<CatImageResponse>>

    fun refreshLoadedImages() {
        _loadedImages = setupLoadedImagesPager()
        loadedImages = _loadedImages
    }

    fun deleteLoadedImageFromLiveData(catImageResponse: CatImageResponse?) {
        val pagingData = loadedImages.value ?: return
        pagingData
            .filter { catImageResponse?.id != it.id }
            .let { _loadedImages.value = it }
    }

    fun sendImageToServer(image: Bitmap): Single<BodyResponse> {
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

    fun deleteLoadedImageFromServer(delete_id: String): Single<DeleteImageResponse> {
        return catService.deleteUploadImage(delete_id)
    }

    fun getImageAnalysisFromServer(image_id: String): Single<List<ImageAnalysisResponse>> {
        return catService.getImageAnalysis(image_id = image_id)
    }

    // Vote fro the cat
    fun sendVoteToServer(image_id: String?, value: Int): Single<BodyResponse> {
        return catService.sendVoteRequest(
            VoteRequest(
                image_id = image_id,
                sub_id = SUB_ID,
                value = value
            )
        )
    }

    // Load categories for filter
    private fun getBreedsFromServer(): Single<List<BreedFilterResponse>> {
        return catService.getBreeds()
    }

    private fun getCategoriesFromServer(): Single<List<CategoryFilterResponse>> {
        return catService.getCategories()
    }

    fun getBreedsAndCategoriesFromServer(): Single<Map<String, List<Any>>> {
        return Single.zip(
            getBreedsFromServer(),
            getCategoriesFromServer(),
            { breed: List<BreedFilterResponse>, categoryFilter: List<CategoryFilterResponse> ->
                mapOf(BREED_LIST_KEY to breed, CATEGORY_LIST_KEY to categoryFilter)}
        )
    }


    companion object MapKeys {
        const val PREFERENCE_FILE_KEY = "com.example.catsapp.PREFERENCE_FILE_KEY"
        const val FAVOURITE_SET_KEY = "favSet"
        const val BREED_LIST_KEY = "breedList"
        const val CATEGORY_LIST_KEY = "categoryList"
        const val SUB_ID = "user162746871621874621874681"
        const val PAGE_SIZE = 16
        const val INITIAL_LOAD_SIZE = 16
    }
}

class CatsFragmentViewModelFactory(private val application: Application, private val catService: CatService) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CatViewModel(application, catService) as T
    }
}