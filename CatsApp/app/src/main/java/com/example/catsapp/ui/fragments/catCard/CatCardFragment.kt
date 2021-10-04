package com.example.catsapp.ui.fragments.catCard

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.net.Uri
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import coil.request.ImageRequest
import com.example.catsapp.api.models.Resource
import com.example.catsapp.api.models.res.BreedResponse
import com.example.catsapp.databinding.FragmentCatCardBinding
import com.example.catsapp.ui.common.CatsAppKeys
import com.example.catsapp.ui.common.FullScreenStateChanger


class CatCardFragment : Fragment() {

    private var binding: FragmentCatCardBinding? = null
    private val args by navArgs<CatCardFragmentArgs>()

    private val catCardViewModel by viewModels<CatCardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCatCardBinding.inflate(inflater, container, false)
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

        binding?.cardImageBackBtn?.setOnClickListener {
            view.findNavController().popBackStack()
        }

        fun voteBtn(view: View, value: Int) {
            catCardViewModel.sendVoteToServerStatus.observe(viewLifecycleOwner) {
                Toast.makeText(view.context, "${it.message}FUL request", Toast.LENGTH_SHORT).show()
            }
            catCardViewModel.sendVoteToServer(value)

            binding?.voteButtonsLayout?.visibility = View.INVISIBLE
            binding?.alreadyVotedTextview?.visibility = View.VISIBLE
        }

        binding?.nopeVoteBtn?.setOnClickListener {
            voteBtn(it, 0)
        }

        binding?.loveVoteBtn?.setOnClickListener {
            voteBtn(it, 1)
        }

        binding?.catCardRetryButton?.setOnClickListener {
            catCardViewModel.getImageFromServer()
        }

        catCardViewModel.getImageFromServerStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    fragmentState(CatsAppKeys.LOADING)
                }
                is Resource.Success -> {
                    val imageRequest = binding?.catCardImage?.let { imageView ->
                        ImageRequest.Builder(view.context)
                            .data(args.imageUrl)
                            .crossfade(10)
                            .target(
                                onSuccess = { result ->
                                    fragmentState(CatsAppKeys.SUCCESS)
                                    imageView.setImageDrawable(result)
                                    if (it.data.breeds != null)
                                        if (it.data.breeds.isNotEmpty())
                                            setupCardFragment(it.data.breeds[0])
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
        catCardViewModel.getImageFromServer()
    }

    private fun fragmentState(stateId: Int) {
        when (stateId) {
            CatsAppKeys.LOADING -> {
                binding?.catCardImage?.visibility = View.GONE
                binding?.catCardInfo?.visibility = View.GONE
                binding?.catCardProgressBar?.visibility = View.VISIBLE
                binding?.catCardRetryButton?.visibility = View.GONE
            }
            CatsAppKeys.SUCCESS -> {
                binding?.catCardImage?.visibility = View.VISIBLE
                binding?.catCardInfo?.visibility = View.VISIBLE
                binding?.catCardProgressBar?.visibility = View.GONE
                binding?.catCardRetryButton?.visibility = View.GONE
            }
            CatsAppKeys.ERROR -> {
                binding?.catCardImage?.visibility = View.GONE
                binding?.catCardInfo?.visibility = View.GONE
                binding?.catCardProgressBar?.visibility = View.GONE
                binding?.catCardRetryButton?.visibility = View.VISIBLE
            }
        }
    }

    private fun setupCardFragment(breed: BreedResponse?) {
        binding?.cardImageName?.text = breed?.name
        binding?.cardImageDescription?.text = breed?.description
        binding?.cardImageTemperament?.text = breed?.temperament
        if (breed?.affectionLevel != null)
            binding?.cardImageAffectionLevelRatingBar?.rating = breed.affectionLevel.toFloat()
        else
            binding?.cardImageAffectionLevelBlock?.visibility = View.GONE

        if (breed?.adaptability != null)
            binding?.cardImageAdaptabilityRatingBar?.rating = breed.adaptability.toFloat()
        else
            binding?.cardImageAdaptabilityBlock?.visibility = View.GONE

        if (breed?.childFriendly != null)
            binding?.cardImageChildFriendlyRatingBar?.rating = breed.childFriendly.toFloat()
        else
            binding?.cardImageChildFriendlyBlock?.visibility = View.GONE

        if (breed?.dogFriendly != null)
            binding?.cardImageDogFriendlyRatingBar?.rating = breed.dogFriendly.toFloat()
        else
            binding?.cardImageDogFriendlyBlock?.visibility = View.GONE

        if (breed?.energyLevel != null)
            binding?.cardImageEnergyLevelRatingBar?.rating = breed.energyLevel.toFloat()
        else
            binding?.cardImageEnergyLevelBlock?.visibility = View.GONE

        if (breed?.grooming != null)
            binding?.cardImageGroomingRatingBar?.rating = breed.grooming.toFloat()
        else
            binding?.cardImageGroomingBlock?.visibility = View.GONE

        if (breed?.healthIssues != null)
            binding?.cardImageHealthIssuesRatingBar?.rating = breed.healthIssues.toFloat()
        else
            binding?.cardImageHealthIssuesBlock?.visibility = View.GONE

        if (breed?.intelligence != null)
            binding?.cardImageIntelligenceRatingBar?.rating = breed.intelligence.toFloat()
        else
            binding?.cardImageIntelligenceBlock?.visibility = View.GONE

        if (breed?.sheddingLevel != null)
            binding?.cardImageSheddingLevelRatingBar?.rating = breed.sheddingLevel.toFloat()
        else
            binding?.cardImageSheddingLevelBlock?.visibility = View.GONE

        if (breed?.socialNeeds != null)
            binding?.cardImageSocialNeedsRatingBar?.rating = breed.socialNeeds.toFloat()
        else
            binding?.cardImageSocialNeedsBlock?.visibility = View.GONE

        if (breed?.strangerFriendly != null)
            binding?.cardImageStrangerFriendlyRatingBar?.rating = breed.strangerFriendly.toFloat()
        else
            binding?.cardImageStrangerFriendlyBlock?.visibility = View.GONE

        if (breed?.vocalisation != null)
            binding?.cardImageVocalisationRatingBar?.rating = breed.vocalisation.toFloat()
        else
            binding?.cardImageVocalisationBlock?.visibility = View.GONE

        if (breed?.wikipediaUrl != null)
            binding?.cardImageWikiBtn?.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(breed.wikipediaUrl))
                startActivity(browserIntent)
            }
        else
            binding?.cardImageWikiBtn?.visibility = View.GONE

        binding?.cardImageBreedInfo?.visibility = View.VISIBLE
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