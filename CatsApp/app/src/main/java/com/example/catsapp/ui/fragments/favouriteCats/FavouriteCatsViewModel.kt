package com.example.catsapp.ui.fragments.favouriteCats

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.example.catsapp.api.models.req.FavouriteRequest
import com.example.catsapp.api.models.res.BodyResponse
import com.example.catsapp.api.models.res.FavouriteResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.db.RoomCatsRepository
import com.example.catsapp.db.dao.FavouriteIdsEntity
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.common.CatsAppKeys
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class FavouriteCatsViewModel(
    application: Application
) : AndroidViewModel(application) {

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

    private val _insertAllFavouriteEntitiesToDBStatus = MutableLiveData<List<FavouriteResponse>>()
    val insertAllFavouriteEntitiesToDBStatus: LiveData<List<FavouriteResponse>> =
        _insertAllFavouriteEntitiesToDBStatus

    private val _loadAllFavouriteEntitiesFromDBStatus = MutableLiveData<List<FavouriteIdsEntity>>()
    val loadAllFavouriteEntitiesFromDBStatus: LiveData<List<FavouriteIdsEntity>> =
        _loadAllFavouriteEntitiesFromDBStatus

    private val deletedFavourites = mutableSetOf<Int>()
    fun getDeletedFavourites() = deletedFavourites

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

    fun sendFavouriteToServerAndSaveToDB(image_id: String): Observable<BodyResponse> {
        return catService.sendFavouriteRequest(
            FavouriteRequest(
                image_id = image_id,
                sub_id = CatsAppKeys.SUB_ID
            )
        ).toObservable().flatMap {
            val favouriteIdsEntity =
                FavouriteIdsEntity(
                    imageId = image_id,
                    favouriteId = it.id!!
                )
            catDatabaseList?.add(favouriteIdsEntity)
            roomCatsRepository.insertFavourite(favouriteIdsEntity)
                .andThen(Observable.just(it))
        }
    }

    fun deleteFavouriteFromServerAndFromDB(favourite_id: String): Observable<BodyResponse> {
        return catService.deleteFavourite(favourite_id)
            .toObservable().flatMap {
                catDatabaseList?.remove(catDatabaseList?.find { favouriteIdsEntity ->
                    favouriteIdsEntity.favouriteId == favourite_id
                })
                roomCatsRepository.deleteFavourite(favourite_id)
                    .andThen(Observable.just(it))
            }
    }

    // Livedata
    fun deleteFavouriteFromLiveData(favouriteResponse: FavouriteResponse?) {
        val pagingData = favourites.value ?: return
        pagingData
            .filter { favouriteResponse?.id != it.id }
            .let {
                _favourites.value = it
                favouriteResponse?.id?.let { id ->
                    deletedFavourites.add(id)
                }
            }
    }

    // Database
    fun insertAllFavouriteEntitiesToDB() {
        compositeDisposable.add(getAllFavouritesFromServer().toObservable().flatMap { list ->
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
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _insertAllFavouriteEntitiesToDBStatus.value = it

                Log.d("RETROFIT", "Successful insert favourites from server to database")
            }, {
                Log.d(
                    "RETROFIT",
                    "Exception during inserting favourites from server to database -> ${it.localizedMessage}"
                )
            })
        )
    }

    fun setupFavouriteIdsEntityList(favouriteIdsEntityList: List<FavouriteIdsEntity>) {
        catDatabaseList = favouriteIdsEntityList as MutableList<FavouriteIdsEntity>
    }

    //    fun loadAllFavouriteEntitiesFromDatabase() = roomCatsRepository.loadAllFavouriteEntities()
    fun loadAllFavouriteEntitiesFromDB() {
        roomCatsRepository.loadAllFavouriteEntities()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _loadAllFavouriteEntitiesFromDBStatus.value = it

                Log.d("RETROFIT", "Successful load favourites from database")
            }, {
                Log.d(
                    "RETROFIT",
                    "Exception during loading favourites from database -> ${it.localizedMessage}"
                )
            })
    }

    fun getCatDatabaseList() = catDatabaseList


    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}