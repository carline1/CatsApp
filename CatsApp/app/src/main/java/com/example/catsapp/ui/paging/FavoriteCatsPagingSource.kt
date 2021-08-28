package com.example.catsapp.ui.paging

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.example.catsapp.api.models.res.FavoriteResponse
import com.example.catsapp.api.services.CatService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FavoriteCatsPagingSource(
    private val catService: CatService
) : RxPagingSource<Int, FavoriteResponse>() {
    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FavoriteResponse>> {
        val page = params.key ?: 0
        val limit = params.loadSize
        val response = catService.getFavorites(CatViewModel.SUB_ID, limit.toString(), page.toString())

        return response
            .subscribeOn(Schedulers.io())
            .map<LoadResult<Int, FavoriteResponse>> {
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

    override fun getRefreshKey(state: PagingState<Int, FavoriteResponse>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}