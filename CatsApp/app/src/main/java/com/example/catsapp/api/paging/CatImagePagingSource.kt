package com.example.catsapp.api.paging

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.example.catsapp.api.models.ImageResponse
import com.example.catsapp.api.services.ImagesService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class CatImagePagingSource(private val imagesService: ImagesService)
    : RxPagingSource<Int, ImageResponse>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, ImageResponse>> {

        val page = params.key ?: 0
        val limit = params.loadSize
        val query = mapOf("limit" to limit.toString(), "page" to page.toString(), "order" to "Asc")
        val response = imagesService.getImages(query)

        return response
            .subscribeOn(Schedulers.io())
            .map<LoadResult<Int, ImageResponse>> {
                Log.d("RETROFIT", it.toString())
                LoadResult.Page(
                    data = it,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (it.size < limit) null else page + 1
                )
            }.onErrorReturn { e ->
                LoadResult.Error(e)
            }
        }

    override fun getRefreshKey(state: PagingState<Int, ImageResponse>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}

