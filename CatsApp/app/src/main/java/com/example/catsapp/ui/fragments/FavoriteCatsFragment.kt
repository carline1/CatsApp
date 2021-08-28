package com.example.catsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.paging.CatViewModel
import com.example.catsapp.ui.paging.CatsFragmentViewModelFactory
import com.example.catsapp.ui.paging.FavoriteCatsPagingAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class FavoritesCatsFragment : Fragment(R.layout.fragment_favorite_cats) {

    private val compositeDisposable = CompositeDisposable()

    // Не работает почему-то :( (inject вызвал в onAttach)
    @Inject
    lateinit var catService: CatService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by activityViewModels<CatViewModel>()

        val pagingAdapter = FavoriteCatsPagingAdapter(view.context, viewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.favoriteListRecyclerView)
        recyclerView.adapter = pagingAdapter
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)

        compositeDisposable.add(viewModel.getFavorites().subscribe {
            pagingAdapter.submitData(lifecycle, it)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        context.appComponent.inject(this)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}