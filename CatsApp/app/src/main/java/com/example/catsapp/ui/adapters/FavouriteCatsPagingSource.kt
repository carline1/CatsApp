package com.example.catsapp.ui.adapters

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.example.catsapp.api.models.res.FavouriteResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.ui.viewmodels.CatViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FavouriteCatsPagingSource(
    private val catService: CatService
) : RxPagingSource<Int, FavouriteResponse>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FavouriteResponse>> {
        val page = params.key ?: 0
        val limit = params.loadSize
        val response = catService.getFavourites(CatViewModel.SUB_ID, limit.toString(), page.toString())

        return response
            .subscribeOn(Schedulers.io())
            .map<LoadResult<Int, FavouriteResponse>> {
                LoadResult.Page(
                    data = it,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (it.size < limit) null else page + 1
                )
            }
            .onErrorReturn { e ->
                LoadResult.Error(e)
            }
    }

    override fun getRefreshKey(state: PagingState<Int, FavouriteResponse>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}