package com.example.catsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.ui.viewmodels.CatViewModel
import com.example.catsapp.ui.viewmodels.CatsFragmentViewModelFactory
import com.example.catsapp.ui.adapters.CatImagePagingAdapter
import com.example.catsapp.api.services.CatService
import com.example.catsapp.databinding.FragmentCatsImagesBinding
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.adapters.LoaderStateAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject


class CatsImagesFragment : Fragment() {

    @Inject lateinit var catService: CatService

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: FragmentCatsImagesBinding
    val viewModel by activityViewModels<CatViewModel> {
        CatsFragmentViewModelFactory(requireActivity().application, catService)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatsImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val pagingAdapter = CatImagePagingAdapter(requireContext(), viewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.imageListRecyclerView)
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
            binding.imageListRecyclerView.isVisible = state.refresh != LoadState.Loading
            binding.catImagesProgressBar.isVisible = state.refresh == LoadState.Loading
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.catImages.observe(viewLifecycleOwner, {
                pagingAdapter.submitData(lifecycle, it)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_cat_image_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filter_item_menu)
            findNavController().navigate(R.id.action_catsImagesFragment_to_filterFragment)

        return true
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