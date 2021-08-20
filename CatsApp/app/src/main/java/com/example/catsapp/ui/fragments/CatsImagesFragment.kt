package com.example.catsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.api.paging.CatsFragmentViewModel
import com.example.catsapp.api.paging.CatsFragmentViewModelFactory
import com.example.catsapp.api.paging.ImagePagingAdapter
import com.example.catsapp.api.services.ImagesService
import com.example.catsapp.di.CatsApp
import com.example.catsapp.di.appComponent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


class CatsImagesFragment : Fragment(R.layout.fragment_cats_images) {

    private val compositeDisposable = CompositeDisposable()

    // Не работает почему-то :(
    @Inject lateinit var imagesService: ImagesService

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel by viewModels<CatsFragmentViewModel>(
            { this },
            { CatsFragmentViewModelFactory(requireActivity().appComponent.getImageService()) }
        )

        val pagingAdapter = ImagePagingAdapter(requireContext())
        val recyclerView = view.findViewById<RecyclerView>(R.id.imageListRecyclerView)
        recyclerView.adapter = pagingAdapter
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)

        compositeDisposable.add(viewModel.images.subscribe {
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