package com.example.catsapp.ui.paging

import android.os.Bundle
import androidx.lifecycle.*
import androidx.paging.*
import androidx.paging.rxjava3.cachedIn
import androidx.paging.rxjava3.observable
import com.example.catsapp.api.models.req.FavoriteRequest
import com.example.catsapp.api.models.req.VoteRequest
import com.example.catsapp.api.models.res.*
import com.example.catsapp.api.services.CatService
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi

class CatViewModel(
    private val catService: CatService,
) : ViewModel() {

    var requestParams = mutableMapOf<String, String>()
    var bundleFilterFragment = MutableLiveData<Bundle>()

    @ExperimentalCoroutinesApi
    fun getImages(): Observable<PagingData<CatImageResponse>> {
        return Pager(PagingConfig(pageSize = 16, initialLoadSize = 16)) {
                CatImagePagingSource(catService, requestParams)
        }.observable.cachedIn(viewModelScope)
    }

    @ExperimentalCoroutinesApi
    fun getFavorites(): Observable<PagingData<FavoriteResponse>> {
        return Pager(PagingConfig(pageSize = 16, initialLoadSize = 16)) {
            FavoriteCatsPagingSource(catService)
        }.observable.cachedIn(viewModelScope)
    }

    fun deleteFavorite(favorite_id: String): Single<FavoriteDELETEResponse> {
        return catService.deleteFavorite(favorite_id)
    }

    fun setVote(image_id: String?, value: Int): Single<VotePOSTResponse> {
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

    fun setFavorite(image_id: String): Single<FavoritePOSTResponse> {
        return catService.sendFavoriteRequest(
            FavoriteRequest(
                image_id = image_id,
                sub_id = SUB_ID
            )
        )
    }

    fun getImage(image_id: String): Single<CatImageResponse> {
        return catService.getImage(image_id = image_id)
    }

    companion object BreedsAndCategoriesMapKeys {
        const val BREED_LIST_KEY = "breedList"
        const val CATEGORY_LIST_KEY = "categoryList"
        const val SUB_ID = "user162746871621874621874681"
    }
}


class CatsFragmentViewModelFactory(private val catService: CatService) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CatViewModel(catService) as T
    }
}