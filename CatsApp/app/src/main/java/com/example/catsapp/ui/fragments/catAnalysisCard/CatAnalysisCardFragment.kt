package com.example.catsapp.ui.fragments.catAnalysisCard

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.example.catsapp.ui.common.FullScreenStateChanger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat

class CatAnalysisCardFragment : Fragment() {

    private var binding: FragmentCatAnalysisBinding? = null
    private val args by navArgs<CatAnalysisCardFragmentArgs>()

    private val catAnalysisViewModel by activityViewModels<CatAnalysisCardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCatAnalysisBinding.inflate(inflater, container, false)
        this.binding = binding
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

        val imageRequest = binding?.catAnalysisCardImage?.let {
            ImageRequest.Builder(view.context)
                .data(args.imageUrl)
                .crossfade(10)
                .placeholder(R.drawable.image_placeholder)
                .target(it)
                .build()
        }
        if (imageRequest != null) {
            imageLoader.enqueue(imageRequest)
        }

        binding?.catAnalysisCardBackBtn?.setOnClickListener {
            view.findNavController().popBackStack()
        }

        catAnalysisViewModel.compositeDisposable.add(catAnalysisViewModel.getImageAnalysisFromServer(args.id!!)
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
        binding?.catAnalysisImageId?.text = resources.getString(R.string.image_analysis_id, imageAnalysisResponse.imageId)
        val parser  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val formatter = SimpleDateFormat("dd.MM.yyyy, HH:mm")
        val createdAt = formatter.format(parser.parse(imageAnalysisResponse.createdAt!!)!!) + " GMT"
        binding?.catAnalysisCreatedAt?.text = resources.getString(R.string.image_analysis_created_at, createdAt)
        binding?.catAnalysisVendor?.text = resources.getString(R.string.image_analysis_vendor, imageAnalysisResponse.vendor)

        if (imageAnalysisResponse.imageAnalysisResponseLabels?.isNotEmpty() == true) {
            binding?.catAnalysisLabels?.visibility = View.VISIBLE

            val recyclerView = binding?.catAnalysisListRecyclerView
            recyclerView?.layoutManager = LinearLayoutManager(view.context)
            recyclerView?.adapter = LabelsAnalysisAdapter(imageAnalysisResponse.imageAnalysisResponseLabels)
        }

        binding?.catAnalysisCardInfo?.visibility = View.VISIBLE
        binding?.catAnalysisProgressBar?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, true)
    }

    override fun onStop() {
        super.onStop()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, false)
    }
}