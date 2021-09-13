package com.example.catsapp.ui.fragments

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
import com.example.catsapp.databinding.FragmentCardCatBinding
import android.net.Uri
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import coil.request.ImageRequest
import com.example.catsapp.R
import com.example.catsapp.api.models.res.BreedResponse
import com.example.catsapp.ui.viewmodels.CatViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class CardCatFragment : Fragment() {

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
            .target(binding.cardImage)
            .build()
        imageLoader.enqueue(imageRequest)

        binding.cardImageBackBtn.setOnClickListener {
            view.findNavController().popBackStack()
        }

        fun voteBtn(view: View, value: Int){
            compositeDisposable.add(viewModel.sendVoteToServer(args.imageId, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("RETROFIT", "Successful vote sending -> ${it.message}, id: ${it.id}")
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

        compositeDisposable.add(viewModel.getImageFromServer(args.imageId!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("RETROFIT", "Successful getting cat image info from server -> ${it.url}, id: ${it.id}")

                if (it.breeds != null)
                    if (it.breeds.isNotEmpty())
                        setupCardFragment(it.breeds[0])

                binding.cardCatInfo.visibility = View.VISIBLE
                binding.cardCatProgressBar.visibility = View.GONE
            }, {
                Log.d("RETROFIT", "Exception during image request -> ${it.localizedMessage}")
            })
        )
    }

    private fun setupCardFragment(breed: BreedResponse?) {
        binding.cardImageName.text = breed?.name
        binding.cardImageDescription.text = breed?.description
        binding.cardImageTemperament.text = breed?.temperament
        binding.cardImageAffectionLevel.rating = breed?.affectionLevel!!.toFloat()
        binding.cardImageAdaptability.rating = breed.adaptability.toFloat()
        binding.cardImageChildFriendly.rating = breed.childFriendly.toFloat()
        binding.cardImageDogFriendly.rating = breed.dogFriendly.toFloat()
        binding.cardImageEnergyLevel.rating = breed.energyLevel.toFloat()
        binding.cardImageGrooming.rating = breed.grooming.toFloat()
        binding.cardImageHealthIssues.rating = breed.healthIssues.toFloat()
        binding.cardImageIntelligence.rating = breed.intelligence.toFloat()
        binding.cardImageSheddingLevel.rating = breed.sheddingLevel.toFloat()
        binding.cardImageSocialNeeds.rating = breed.socialNeeds.toFloat()
        binding.cardImageStrangerFriendly.rating = breed.strangerFriendly.toFloat()
        binding.cardImageVocalisation.rating = breed.vocalisation.toFloat()

        binding.cardImageBreedInfo.visibility = View.VISIBLE

        binding.cardImageWikiBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(breed.wikipediaUrl))
            startActivity(browserIntent)
        }
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