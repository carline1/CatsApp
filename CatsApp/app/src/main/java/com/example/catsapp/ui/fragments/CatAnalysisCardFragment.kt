package com.example.catsapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.catsapp.R
import com.example.catsapp.api.models.res.ImageAnalysisResponse
import com.example.catsapp.databinding.FragmentCatAnalysisBinding
import com.example.catsapp.ui.adapters.LabelsAnalysisAdapter
import com.example.catsapp.ui.viewmodels.CatViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat

class CatAnalysisCardFragment : Fragment() {

    private lateinit var binding: FragmentCatAnalysisBinding
    private val args by navArgs<CatAnalysisCardFragmentArgs>()
    private val compositeDisposable = CompositeDisposable()

    private val viewModel by activityViewModels<CatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageLoader =
            ImageLoader.Builder(requireContext())
                .componentRegistry {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder(requireContext()))
                    } else {
                        add(GifDecoder())
                    }
                }
                .build()

        val imageRequest = ImageRequest.Builder(view.context)
            .data(args.imageUrl)
            .crossfade(10)
            .placeholder(R.drawable.image_placeholder)
            .target(binding.catAnalysisCardImage)
            .build()
        imageLoader.enqueue(imageRequest)

        binding.catAnalysisCardBackBtn.setOnClickListener {
            view.findNavController().popBackStack()
        }

        compositeDisposable.add(viewModel.getImageAnalysisFromServer(args.id!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("RETROFIT", "Successful getting uploaded image analysis from server -> " +
                        "id: ${it[0].imageId}, created at: ${it[0].createdAt}")
                setupCatAnalysisCardFragment(view, it[0])
            }, {
                Log.d("RETROFIT", "Exception during imageAAnalysis request -> ${it.localizedMessage}")
            })
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupCatAnalysisCardFragment(view: View, imageAnalysisResponse: ImageAnalysisResponse) {
        binding.catAnalysisImageId.text = resources.getString(R.string.image_analysis_id, imageAnalysisResponse.imageId)
        val parser  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val formatter = SimpleDateFormat("dd.MM.yyyy, HH:mm")
        val createdAt = formatter.format(parser.parse(imageAnalysisResponse.createdAt)!!) + " GMT"
        binding.catAnalysisCreatedAt.text = resources.getString(R.string.image_analysis_created_at, createdAt)
        binding.catAnalysisVendor.text = resources.getString(R.string.image_analysis_vendor, imageAnalysisResponse.vendor)

        if (imageAnalysisResponse.imageAnalysisResponseLabels.isNotEmpty()) {
            binding.catAnalysisLabels.visibility = View.VISIBLE

            val recyclerView = binding.catAnalysisListRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(view.context)
            recyclerView.adapter = LabelsAnalysisAdapter(imageAnalysisResponse.imageAnalysisResponseLabels)
        }

        binding.catAnalysisCardInfo.visibility = View.VISIBLE
        binding.catAnalysisProgressBar.visibility = View.GONE
    }

    private fun showHideBottomBar(state: BottomBarState) {
        val navHostFragment = (activity as AppCompatActivity).findViewById<FragmentContainerView>(R.id.nav_host_fragment)
        val marginLayoutParams = navHostFragment.layoutParams as ViewGroup.MarginLayoutParams
        val marginBottom: Int
        val visibility: Int
        when(state) {
            BottomBarState.SHOW -> {
                val typeValue = TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.actionBarSize, typeValue, true)
                marginBottom = resources.getDimensionPixelSize(typeValue.resourceId)
                visibility = View.VISIBLE
            }
            BottomBarState.HIDE -> {
                marginBottom = 0
                visibility = View.GONE
            }
        }
        marginLayoutParams.setMargins(0, 0, 0, marginBottom)
        navHostFragment.requestLayout()
        val bottomNavView = (activity as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavView.visibility = visibility
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
        showHideBottomBar(BottomBarState.HIDE)
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
        showHideBottomBar(BottomBarState.SHOW)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}