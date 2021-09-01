package com.example.catsapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.example.catsapp.R
import com.example.catsapp.api.models.res.ImageAnalysisResponse
import com.example.catsapp.databinding.FragmentCatAnalysisBinding
import com.example.catsapp.ui.adapters.LabelsAnalysisAdapter
import com.example.catsapp.ui.viewmodels.CatViewModel
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

        view.apply {
            val imageLoader =
                ImageLoader.Builder(context)
                    .componentRegistry {
                        if (Build.VERSION.SDK_INT >= 28) {
                            add(ImageDecoderDecoder(context))
                        } else {
                            add(GifDecoder())
                        }
                    }
                    .build()

            binding.catAnalysisCardImage.load(
                args.imageUrl,
                imageLoader
            ) {
                crossfade(true)
            }
        }

        binding.catAnalysisCardBackBtn.setOnClickListener {
            view.findNavController().popBackStack()
        }

        compositeDisposable.add(viewModel.getImageAnalysis(args.id!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("RETROFIT", "${it[0].imageId}, id: ${it[0].createdAt}")
                setupCatAnalysisCardFragment(view, it[0])
            }, {
                Log.d("RETROFIT", "Exception during imageAAnalysis request -> ${it.localizedMessage}")
            })
        )
    }

    @SuppressLint("SimpleDateFormat")
    fun setupCatAnalysisCardFragment(view: View, imageAnalysisResponse: ImageAnalysisResponse) {
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
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}