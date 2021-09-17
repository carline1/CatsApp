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
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import coil.request.ImageRequest
import com.example.catsapp.R
import com.example.catsapp.api.models.res.BreedResponse
import com.example.catsapp.databinding.FragmentCatCardBinding
import com.example.catsapp.ui.common.FullScreenStateChanger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers


class CatCardFragment : Fragment() {

    private var binding: FragmentCatCardBinding? = null
    private val args by navArgs<CatCardFragmentArgs>()

    private val catCardViewModel by activityViewModels<CatCardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding  = FragmentCatCardBinding.inflate(inflater, container, false)
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

        val imageRequest = binding?.let {
            ImageRequest.Builder(view.context)
                .data(args.imageUrl)
                .crossfade(10)
                .placeholder(R.drawable.image_placeholder)
                .target(it.cardImage)
                .build()
        }
        if (imageRequest != null) {
            imageLoader.enqueue(imageRequest)
        }

        binding?.cardImageBackBtn?.setOnClickListener {
            view.findNavController().popBackStack()
        }

        fun voteBtn(view: View, value: Int){
            catCardViewModel.compositeDisposable.add(catCardViewModel.sendVoteToServer(args.imageId, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("RETROFIT", "Successful vote sending -> ${it.message}, id: ${it.id}")
                    Toast.makeText(view.context, "${it.message}FUL request", Toast.LENGTH_SHORT).show()
                }, {
                    Log.d("RETROFIT", "Exception during vote request -> ${it.localizedMessage}")
                })
            )

            binding?.voteButtonsLayout?.visibility = View.INVISIBLE
            binding?.alreadyVotedTextview?.visibility = View.VISIBLE
        }

        binding?.nopeVoteBtn?.setOnClickListener {
            voteBtn(it, 0)
        }

        binding?.loveVoteBtn?.setOnClickListener {
            voteBtn(it, 1)
        }

        catCardViewModel.compositeDisposable.add(catCardViewModel.getImageFromServer(args.imageId!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("RETROFIT", "Successful getting cat image info from server -> ${it.url}, id: ${it.id}")

                if (it.breeds != null)
                    if (it.breeds.isNotEmpty())
                        setupCardFragment(it.breeds[0])

                binding?.catCardInfo?.visibility = View.VISIBLE
                binding?.catCardProgressBar?.visibility = View.GONE
            }, {
                Log.d("RETROFIT", "Exception during image request -> ${it.localizedMessage}")
            })
        )
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