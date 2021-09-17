package com.example.catsapp.ui.fragments.catImages

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.example.catsapp.api.models.res.CatImageResponse
import com.example.catsapp.api.services.CatService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class CatImagesPagingSource(
    private val catService: CatService,
    private val requestParams: MutableMap<String, String>
) : RxPagingSource<Int, CatImageResponse>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, CatImageResponse>> {

        val page = params.key ?: 0
        val limit = params.loadSize
        if (!requestParams.containsKey("order")) requestParams["order"] = "Asc"
        requestParams["page"] = page.toString()
        requestParams["limit"] = limit.toString()
        val query = requestParams
        val response = catService.getImages(query)

        return response
            .subscribeOn(Schedulers.io())
            .map<LoadResult<Int, CatImageResponse>> {
                LoadResult.Page(
                    data = it,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (it.size < limit) null else page + 1
                )
            }.onErrorReturn { e ->
                LoadResult.Error(e)
            }
        }

    override fun getRefreshKey(state: PagingState<Int, CatImageResponse>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}