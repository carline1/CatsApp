package com.example.catsapp.ui.fragments

import android.content.Context
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
import coil.load
import com.example.catsapp.R
import com.example.catsapp.databinding.FragmentCardCatBinding
import android.net.Uri
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.catsapp.api.models.res.BreedResponse
import com.example.catsapp.api.services.CatService
import com.example.catsapp.di.appComponent
import com.example.catsapp.ui.paging.CatViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


class CardCatFragment : Fragment(R.layout.fragment_card_cat) {

    // Тоже не работает :(
    @Inject
    lateinit var catService: CatService

    private lateinit var binding: FragmentCardCatBinding
    private val args by navArgs<CardCatFragmentArgs>()
    private val compositeDisposable = CompositeDisposable()

    private val viewModel by activityViewModels<CatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardCatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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

            binding.cardImage.load(
                args.imageUrl,
                imageLoader
            ) {
                crossfade(true)
            }
        }

        binding.catdImageBackBtn.setOnClickListener {
            view.findNavController().popBackStack()
        }

        fun voteBtn(view: View, value: Int){
            compositeDisposable.add(viewModel.setVote(args.imageId, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("RETROFIT", "${it.message}, id: ${it.id}")
                    Toast.makeText(view.context, "${it.message}FUL request", Toast.LENGTH_SHORT).show()
                }, {
                    Log.d("RETROFIT", "Exception during vote request -> ${it.localizedMessage}")
                })
            )

            binding.voteButtonsLayout.visibility = View.INVISIBLE
            binding.alreadyVotedTextview.visibility = View.VISIBLE
        }

        binding.nopeVoteBtn.setOnClickListener {
            voteBtn(it, 0)
        }

        binding.loveVoteBtn.setOnClickListener {
            voteBtn(it, 1)
        }

        if (args.fragmentSelection == CardFragmentSelectionEnum.CatImage) {
            if (args.breed != null) setupCardFragment(args.breed)
            binding.cardCatInfo.visibility = View.VISIBLE
        }
        else if (args.fragmentSelection == CardFragmentSelectionEnum.FavoriteCats) {
            compositeDisposable.add(viewModel.getImage(args.imageId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.breeds != null)
                        if (it.breeds.isNotEmpty())
                            setupCardFragment(it.breeds[0])

                    binding.cardCatInfo.visibility = View.VISIBLE
                }, {
                    Log.d("RETROFIT", "Exception during image request -> ${it.localizedMessage}")
                })
            )
        }
    }

    private fun setupCardFragment(breed: BreedResponse?) {
        binding.cardImageName.text = breed?.name
        binding.cardImageDescription.text = breed?.description
        binding.cardImageTemperament.text = breed?.temperament
        binding.cardImageAffectionLevel.numStars = breed?.affectionLevel!!
        binding.cardImageAdaptability.numStars = breed.adaptability
        binding.cardImageChildFriendly.numStars = breed.childFriendly
        binding.cardImageDogFriendly.numStars = breed.dogFriendly
        binding.cardImageEnergyLevel.numStars = breed.energyLevel
        binding.cardImageGrooming.numStars = breed.grooming
        binding.cardImageHealthIssues.numStars = breed.healthIssues
        binding.cardImageIntelligence.numStars = breed.intelligence
        binding.cardImageSheddingLevel.numStars = breed.sheddingLevel
        binding.cardImageSocialNeeds.numStars = breed.socialNeeds
        binding.cardImageStrangerFriendly.numStars = breed.strangerFriendly
        binding.cardImageVocalisation.numStars = breed.vocalisation

        binding.cardImageBreedInfo.visibility = View.VISIBLE

        binding.cardImageWikiBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(breed.wikipediaUrl))
            startActivity(browserIntent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}