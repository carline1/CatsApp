package com.example.catsapp.ui.fragments.filter

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.catsapp.api.models.res.BreedFilterResponse
import com.example.catsapp.api.models.res.CategoryFilterResponse
import com.example.catsapp.api.models.res.FavouriteResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class FilterViewModel(
    application: Application
) : AndroidViewModel(application) {

    init {
        application.appComponent.inject(this)
    }
    @Inject
    lateinit var catService: CatService

    val compositeDisposable = CompositeDisposable()

    private val _bundleFilterFragment = MutableLiveData<Bundle>()
    val bundleFilterFragment: LiveData<Bundle> = _bundleFilterFragment

    var breedList = listOf<BreedFilterResponse>()
        private set
    var categoryList = listOf<CategoryFilterResponse>()
        private set

    // Setup filter
    fun setupBundleFilter(bundle: Bundle) {
        _bundleFilterFragment.value = bundle
    }

    fun setupBreedList(list: List<BreedFilterResponse>) {
        breedList = list
    }

    fun setupCategoryList(list: List<CategoryFilterResponse>) {
        categoryList = list
    }

    // Server
    private fun getBreedsFromServer(): Single<List<BreedFilterResponse>> {
        return Observable.range(0, Int.MAX_VALUE - 1)
            .concatMap { page ->
                catService.getBreeds(
                    CatsAppKeys.SUB_ID,
                    CatsAppKeys.PAGE_SIZE.toString(),
                    page.toString()
                ).toObservable()
            }
            .takeWhile { results -> results.isNotEmpty() }
            .scan { results, results2 ->
                val list: MutableList<BreedFilterResponse> = ArrayList()
                list.addAll(results)
                list.addAll(results2)
                list
            }
            .last(listOf())
    }

    private fun getCategoriesFromServer(): Single<List<CategoryFilterResponse>> {
        return Observable.range(0, Int.MAX_VALUE - 1)
            .concatMap { page ->
                catService.getCategories(
                    CatsAppKeys.SUB_ID,
                    CatsAppKeys.PAGE_SIZE.toString(),
                    page.toString()
                ).toObservable()
            }
            .takeWhile { results -> results.isNotEmpty() }
            .scan { results, results2 ->
                val list: MutableList<CategoryFilterResponse> = ArrayList()
                list.addAll(results)
                list.addAll(results2)
                list
            }
            .last(listOf())
    }

    fun getBreedsAndCategoriesFromServer(): Single<Map<String, List<Any>>> {
        return Single.zip(
            getBreedsFromServer(),
            getCategoriesFromServer(),
            { breed: List<BreedFilterResponse>, categoryFilter: List<CategoryFilterResponse> ->
                mapOf(CatsAppKeys.BREED_LIST_KEY to breed, CatsAppKeys.CATEGORY_LIST_KEY to categoryFilter)}
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}