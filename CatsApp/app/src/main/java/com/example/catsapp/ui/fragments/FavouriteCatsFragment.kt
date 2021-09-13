package com.example.catsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.databinding.FragmentFavouriteCatsBinding
import com.example.catsapp.ui.viewmodels.CatViewModel
import com.example.catsapp.ui.adapters.FavouriteCatsPagingAdapter
import com.example.catsapp.ui.adapters.LoaderStateAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class FavouriteCatsFragment : Fragment() {

    lateinit var binding: FragmentFavouriteCatsBinding
    private val compositeDisposable = CompositeDisposable()
    val viewModel by activityViewModels<CatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteCatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagingAdapter = FavouriteCatsPagingAdapter(requireContext(), viewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.favouriteListRecyclerView)
        recyclerView.adapter = pagingAdapter
        val layoutManager = GridLayoutManager(view.context, 2)
        recyclerView.layoutManager = layoutManager
        layoutManager.spanSizeLookup =  object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == pagingAdapter.itemCount  && pagingAdapter.itemCount > 0) {
                    2
                } else {
                    1
                }
            }
        }
        recyclerView.adapter = pagingAdapter.withLoadStateHeaderAndFooter(
            header = LoaderStateAdapter(),
            footer = LoaderStateAdapter()
        )

        pagingAdapter.addLoadStateListener { state ->
            binding.favouriteListRecyclerView.isVisible = state.refresh != LoadState.Loading
            binding.favouritesProgressBar.isVisible = state.refresh == LoadState.Loading
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favourites.observe(viewLifecycleOwner, {
                pagingAdapter.submitData(lifecycle, it)
            })
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}