package com.example.catsapp.ui.fragments.catImages

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.databinding.FragmentCatImagesBinding
import com.example.catsapp.ui.common.FullScreenStateChanger
import com.example.catsapp.ui.common.LoaderStateAdapter
import com.example.catsapp.ui.common.CatsAppKeys
import com.example.catsapp.ui.fragments.favouriteCats.FavouriteCatsViewModel
import kotlinx.coroutines.launch


class CatImagesFragment : Fragment() {

    private var binding: FragmentCatImagesBinding? = null
    private var prefs: SharedPreferences? = null

    private val catImagesViewModel by activityViewModels<CatImagesViewModel>()
    private val favouritesViewModel by activityViewModels<FavouriteCatsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCatImagesBinding.inflate(inflater, container, false)
        this.binding = binding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val pagingAdapter =
            CatImagesPagingAdapter(requireContext(), catImagesViewModel, favouritesViewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.imageListRecyclerView)
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

        binding?.catImagesRetryButton?.setOnClickListener { pagingAdapter.retry() }

        pagingAdapter.addLoadStateListener { state ->
            binding?.imageListRecyclerView?.isVisible = state.refresh !is LoadState.Loading
            binding?.catImagesProgressBar?.isVisible = state.refresh is LoadState.Loading
            binding?.catImagesRetryButton?.isVisible = state.refresh is LoadState.Error
        }


        if (prefs?.getBoolean(CatsAppKeys.FIRST_RUN_KEY, true) == true) {
            binding?.catImagesLoadingFavProgressBar?.visibility = View.VISIBLE
            binding?.catImagesLayout?.visibility = View.GONE
            FullScreenStateChanger.fullScreen(requireActivity(), true)

            loadFavouritesFromServerToDatabase(pagingAdapter)
        } else {
            if (favouritesViewModel.getCatDatabaseList() == null) {
                loadAllFavouritesFromDatabase(pagingAdapter)
            } else {
                paging(pagingAdapter)
            }
        }
    }

    private fun loadFavouritesFromServerToDatabase(pagingAdapter: CatImagesPagingAdapter) {
        favouritesViewModel.insertAllFavouriteEntitiesToDBStatus.observe(viewLifecycleOwner) {
            binding?.catImagesLoadingFavProgressBar?.visibility = View.GONE
            binding?.catImagesLayout?.visibility = View.VISIBLE
            FullScreenStateChanger.fullScreen(requireActivity(), false)

            prefs?.edit()?.putBoolean(CatsAppKeys.FIRST_RUN_KEY, false)?.apply()
            paging(pagingAdapter)
        }
        favouritesViewModel.insertAllFavouriteEntitiesToDB()
    }

    private fun loadAllFavouritesFromDatabase(pagingAdapter: CatImagesPagingAdapter) {
        favouritesViewModel.loadAllFavouriteEntitiesFromDBStatus.observe(viewLifecycleOwner) {
            favouritesViewModel.setupFavouriteIdsEntityList(it)
            paging(pagingAdapter)
        }
        favouritesViewModel.loadAllFavouriteEntitiesFromDB()
    }

    private fun paging(pagingAdapter: CatImagesPagingAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            catImagesViewModel.catImages.observe(viewLifecycleOwner, {
                pagingAdapter.submitData(lifecycle, it)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_cat_image_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filter_item_menu)
            findNavController().navigate(R.id.action_catImagesFragment_to_filterFragment)

        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = context.getSharedPreferences(
            CatsAppKeys.PREFERENCE_FILE_KEY,
            AppCompatActivity.MODE_PRIVATE
        )
    }
}