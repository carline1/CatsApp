package com.example.catsapp.ui.fragments.favouriteCats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.catsapp.api.models.res.BodyResponse
import com.example.catsapp.api.models.res.FavouriteResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.db.RoomCatsRepository
import com.example.catsapp.db.dao.FavouriteIdsEntity
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FavouriteCatsViewModel(
    application: Application
) : AndroidViewModel(application)  {

    init {
        application.appComponent.inject(this)
    }
    @Inject
    lateinit var catService: CatService
    @Inject
    lateinit var roomCatsRepository: RoomCatsRepository

    val compositeDisposable = CompositeDisposable()

    private var _favourites = setupFavouritePager()
    var favourites: LiveData<PagingData<FavouriteResponse>> = _favourites
        private set

    // Key - favouriteId
    // Value - imageId
    private var catDatabaseList: MutableList<FavouriteIdsEntity>? = null

    // Pager
    private fun setupFavouritePager() = Pager(
        PagingConfig(
            pageSize = CatsAppKeys.PAGE_SIZE,
            initialLoadSize = CatsAppKeys.INITIAL_LOAD_SIZE
        )
    ) {
        FavouriteCatsPagingSource(catService)
    }.liveData.cachedIn(viewModelScope) as MutableLiveData<PagingData<FavouriteResponse>>

    fun refreshFavourites() {
        _favourites = setupFavouritePager()
        favourites = _favourites
    }

    // Server
    private fun getAllFavouritesFromServer(): Single<List<FavouriteResponse>> {
        return Observable.range(0, Int.MAX_VALUE - 1)
            .concatMap { page ->
                catService.getFavourites(
                    CatsAppKeys.SUB_ID,
                    CatsAppKeys.PAGE_SIZE.toString(),
                    page.toString()
                ).toObservable()
            }
            .takeWhile { results -> results.isNotEmpty() }
            .scan { results, results2 ->
                val list: MutableList<FavouriteResponse> = ArrayList()
                list.addAll(results)
                list.addAll(results2)
                list
            }
            .last(listOf())
    }

    fun deleteFavouriteFromServer(favourite_id: String): Single<BodyResponse> {
        return catService.deleteFavourite(favourite_id)
    }

    // Livedata
    fun deleteFavouriteFromLiveData(favouriteResponse: FavouriteResponse?) {
        val pagingData = favourites.value ?: return
        pagingData
            .filter { favouriteResponse?.id != it.id }
            .let { _favourites.value = it }
    }

    // Database
    fun insertAllFavouriteEntitiesToDatabase(): Observable<List<FavouriteResponse>> {
        return getAllFavouritesFromServer().toObservable().flatMap { list ->
            val favouriteIdsEntityList = list.map {
                FavouriteIdsEntity(
                    imageId = it.image_id!!,
                    favouriteId = it.id.toString()
                )
            }
            setupFavouriteIdsEntityList(favouriteIdsEntityList)
            roomCatsRepository.insertAll(favouriteIdsEntityList)
                .andThen(Observable.just(list))
        }
    }

    fun insertFavouriteEntityToDatabase(favouriteIdsEntity: FavouriteIdsEntity): Completable {
        catDatabaseList?.add(favouriteIdsEntity)
        return roomCatsRepository.insertFavourite(favouriteIdsEntity)
    }

    fun setupFavouriteIdsEntityList(favouriteIdsEntityList: List<FavouriteIdsEntity>) {
        catDatabaseList = favouriteIdsEntityList as MutableList<FavouriteIdsEntity>
    }

    fun loadAllFavouriteEntitiesFromDatabase() = roomCatsRepository.loadAllFavouriteEntities()

    fun deleteFavouriteEntityFromDatabase(imageId: String): Completable {
        catDatabaseList?.remove(catDatabaseList?.find { it.imageId == imageId })
        return roomCatsRepository.deleteFavourite(imageId)
    }

    fun getCatDatabaseList() = catDatabaseList


    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}