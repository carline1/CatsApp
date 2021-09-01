package com.example.catsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.ui.viewmodels.CatViewModel
import com.example.catsapp.ui.adapters.FavouriteCatsPagingAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable

class FavouritesCatsFragment : Fragment(R.layout.fragment_favourite_cats) {

    private val compositeDisposable = CompositeDisposable()
    val viewModel by activityViewModels<CatViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagingAdapter = FavouriteCatsPagingAdapter(viewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.favouriteListRecyclerView)
        recyclerView.adapter = pagingAdapter
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)

        compositeDisposable.add(viewModel.getFavourites().subscribe {
            pagingAdapter.submitData(lifecycle, it)
        })
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}