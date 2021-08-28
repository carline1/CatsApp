package com.example.catsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.ui.paging.CatViewModel
import com.example.catsapp.ui.paging.CatsFragmentViewModelFactory
import com.example.catsapp.ui.paging.CatImagePagingAdapter
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


class CatsImagesFragment : Fragment(R.layout.fragment_cats_images) {

    private val compositeDisposable = CompositeDisposable()

    // Не работает почему-то :( (inject вызвал в onAttach)
    @Inject
    lateinit var catService: CatService

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val viewModel by activityViewModels<CatViewModel> {
            CatsFragmentViewModelFactory(requireActivity().appComponent.getCatService())
        }

        val pagingAdapter = CatImagePagingAdapter(requireContext(), viewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.imageListRecyclerView)
        recyclerView.adapter = pagingAdapter
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)

        compositeDisposable.add(viewModel.getImages().subscribe {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_cat_image_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filter_item_menu)
            findNavController().navigate(R.id.action_catsImagesFragment_to_filterFragment)

        return true
    }
}