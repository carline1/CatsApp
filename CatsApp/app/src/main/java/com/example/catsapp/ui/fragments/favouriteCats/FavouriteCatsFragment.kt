package com.example.catsapp.ui.fragments.favouriteCats

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.databinding.FragmentFavouriteCatsBinding
import com.example.catsapp.ui.common.LoaderStateAdapter
import kotlinx.coroutines.launch

class FavouriteCatsFragment : Fragment() {

    private var binding: FragmentFavouriteCatsBinding? = null
    private val favouritesViewModel by activityViewModels<FavouriteCatsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFavouriteCatsBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagingAdapter = FavouriteCatsPagingAdapter(requireContext(), favouritesViewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.favouriteListRecyclerView)
        recyclerView.adapter = pagingAdapter
        val layoutManager = GridLayoutManager(view.context, 2)
        recyclerView.layoutManager = layoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == pagingAdapter.itemCount && pagingAdapter.itemCount > 0) {
                    2
                } else {
                    1
                }
            }
        }

        recyclerView.adapter = pagingAdapter.withLoadStateHeaderAndFooter(
            header = LoaderStateAdapter { pagingAdapter.retry() },
            footer = LoaderStateAdapter { pagingAdapter.retry() }
        )

        binding?.favouritesRetryButton?.setOnClickListener { pagingAdapter.retry() }

        pagingAdapter.addLoadStateListener { state ->
            binding?.favouriteListRecyclerView?.isVisible = state.refresh !is LoadState.Loading
            binding?.favouritesProgressBar?.isVisible = state.refresh is LoadState.Loading
            binding?.favouritesRetryButton?.isVisible = state.refresh is LoadState.Error
        }

        viewLifecycleOwner.lifecycleScope.launch {
            favouritesViewModel.favourites.map { pagingData ->
                pagingData.filter { it.id !in favouritesViewModel.getDeletedFavourites() }
            }
                .observe(viewLifecycleOwner, {
                    pagingAdapter.submitData(lifecycle, it)
                })
        }
    }
}