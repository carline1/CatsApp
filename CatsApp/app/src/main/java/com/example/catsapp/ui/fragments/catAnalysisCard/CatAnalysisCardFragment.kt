package com.example.catsapp.ui.fragments.catAnalysisCard

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.catsapp.R
import com.example.catsapp.api.models.Resource
import com.example.catsapp.api.models.res.ImageAnalysisResponse
import com.example.catsapp.databinding.FragmentCatAnalysisBinding
import com.example.catsapp.ui.common.CatsAppKeys
import com.example.catsapp.ui.common.FullScreenStateChanger
import java.text.SimpleDateFormat
import java.util.*

class CatAnalysisCardFragment : Fragment() {

    private var binding: FragmentCatAnalysisBinding? = null
    private val args by navArgs<CatAnalysisCardFragmentArgs>()

    private val catAnalysisViewModel by viewModels<CatAnalysisCardViewModel>()

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

        binding?.catAnalysisCardBackBtn?.setOnClickListener {
            view.findNavController().popBackStack()
        }

        binding?.catAnalysisRetryButton?.setOnClickListener {
            catAnalysisViewModel.getImageAnalysisFromServer()
        }

        catAnalysisViewModel.getImageAnalysisFromServerStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    fragmentState(CatsAppKeys.LOADING)
                }
                is Resource.Success -> {
                    val imageRequest = binding?.catAnalysisCardImage?.let { imageView ->
                        ImageRequest.Builder(view.context)
                            .data(args.imageUrl)
                            .crossfade(10)
                            .target(
                                onSuccess = { result ->
                                    fragmentState(CatsAppKeys.SUCCESS)
                                    imageView.setImageDrawable(result)
                                    setupCatAnalysisCardFragment(view, it.data)
                                },
                                onError = {
                                    fragmentState(CatsAppKeys.ERROR)
                                }
                            )
                            .build()
                    }
                    if (imageRequest != null) {
                        imageLoader.enqueue(imageRequest)
                    }
                }
                is Resource.Error -> {
                    fragmentState(CatsAppKeys.ERROR)
                }
            }
        }
        catAnalysisViewModel.getImageAnalysisFromServer()
    }

    private fun fragmentState(stateId: Int) {
        when (stateId) {
            CatsAppKeys.LOADING -> {
                binding?.catAnalysisCardImage?.visibility = View.GONE
                binding?.catAnalysisCardInfo?.visibility = View.GONE
                binding?.catAnalysisProgressBar?.visibility = View.VISIBLE
                binding?.catAnalysisRetryButton?.visibility = View.GONE
            }
            CatsAppKeys.SUCCESS -> {
                binding?.catAnalysisCardImage?.visibility = View.VISIBLE
                binding?.catAnalysisCardInfo?.visibility = View.VISIBLE
                binding?.catAnalysisProgressBar?.visibility = View.GONE
                binding?.catAnalysisRetryButton?.visibility = View.GONE
            }
            CatsAppKeys.ERROR -> {
                binding?.catAnalysisCardImage?.visibility = View.GONE
                binding?.catAnalysisCardInfo?.visibility = View.GONE
                binding?.catAnalysisProgressBar?.visibility = View.GONE
                binding?.catAnalysisRetryButton?.visibility = View.VISIBLE
            }
            else -> {
            }
        }
    }

    private fun setupCatAnalysisCardFragment(
        view: View,
        imageAnalysisResponse: ImageAnalysisResponse
    ) {
        binding?.catAnalysisImageId?.text =
            resources.getString(R.string.image_analysis_id, imageAnalysisResponse.imageId)

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        val createdAt = formatter.format(parser.parse(imageAnalysisResponse.createdAt!!)!!) + " GMT"
        binding?.catAnalysisCreatedAt?.text =
            resources.getString(R.string.image_analysis_created_at, createdAt)
        binding?.catAnalysisVendor?.text =
            resources.getString(R.string.image_analysis_vendor, imageAnalysisResponse.vendor)

        if (imageAnalysisResponse.imageAnalysisResponseLabels?.isNotEmpty() == true) {
            binding?.catAnalysisLabels?.visibility = View.VISIBLE

            val recyclerView = binding?.catAnalysisListRecyclerView
            recyclerView?.layoutManager = LinearLayoutManager(view.context)
            recyclerView?.adapter =
                LabelsAnalysisAdapter(imageAnalysisResponse.imageAnalysisResponseLabels)
        }
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