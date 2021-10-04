package com.example.catsapp.ui.fragments.filter

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.catsapp.api.models.res.BreedFilterResponse
import com.example.catsapp.api.models.res.CategoryFilterResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class FilterViewModel(
    application: Application
) : AndroidViewModel(application) {

    init {
        application.appComponent.inject(this)
    }

    @Inject
    lateinit var catService: CatService

    private val compositeDisposable = CompositeDisposable()

    var bundleFilterFragment = bundleOf()
        private set

    var breedList = listOf<BreedFilterResponse>()
        private set
    var categoryList = listOf<CategoryFilterResponse>()
        private set

    private val _getBreedsAndCategoriesFromServerStatus = MutableLiveData<Map<String, List<Any>>>()
    val getBreedsAndCategoriesFromServerStatus: LiveData<Map<String, List<Any>>> =
        _getBreedsAndCategoriesFromServerStatus

    // Setup filter
    fun setupBundleFilter(bundle: Bundle) {
        bundleFilterFragment = bundle
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

    fun getBreedsAndCategoriesFromServer() {
        compositeDisposable.add(Single.zip(
            getBreedsFromServer(),
            getCategoriesFromServer(),
            { breed: List<BreedFilterResponse>, categoryFilter: List<CategoryFilterResponse> ->
                mapOf(
                    CatsAppKeys.BREED_LIST_KEY to breed,
                    CatsAppKeys.CATEGORY_LIST_KEY to categoryFilter
                )
            }
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _getBreedsAndCategoriesFromServerStatus.value = it
            }, {
                Log.d(
                    "RETROFIT",
                    "Exception during breedAndCategory request -> ${it.localizedMessage}"
                )
            })
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}