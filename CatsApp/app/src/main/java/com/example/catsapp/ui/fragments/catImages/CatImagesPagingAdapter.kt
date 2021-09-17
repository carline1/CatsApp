package com.example.catsapp.ui.fragments.catImages

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.catsapp.R
import com.example.catsapp.api.models.res.CatImageResponse
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.findNavController
import coil.request.ImageRequest
import com.example.catsapp.db.dao.FavouriteIdsEntity
import com.example.catsapp.ui.fragments.favouriteCats.FavouriteCatsViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers


class CatImagesPagingAdapter(
    context: Context,
    private val catImagesViewModel: CatImagesViewModel,
    private val favouritesViewModel: FavouriteCatsViewModel
) : PagingDataAdapter<CatImageResponse, CatImagesPagingAdapter.ImageViewHolder>(DiffCallback) {

    private val imageLoader = ImageLoader.Builder(context)
        .componentRegistry {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder(context))
            } else {
                add(GifDecoder())
            }
        }
        .build()

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.image_view_holder, parent, false), catImagesViewModel, favouritesViewModel)
    }

    inner class ImageViewHolder(
        itemView: View,
        private val catImagesViewModel: CatImagesViewModel,
        private val favouritesViewModel: FavouriteCatsViewModel
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val favouriteBtn = itemView.findViewById<ImageButton>(R.id.favouriteBtn)
        private val progressBar = itemView.findViewById<FrameLayout>(R.id.image_view_progress_bar)

        fun bind(item: CatImageResponse?) {
            if (favouritesViewModel.getCatDatabaseList()?.find { it.imageId == item?.id } != null)
                favouriteBtn.setImageResource(R.drawable.ic_favourite_active)
            else
                favouriteBtn.setImageResource(R.drawable.ic_favourite)

            val imageRequest = ImageRequest.Builder(imageView.context)
                .data(item?.url)
                .crossfade(10)
                .placeholder(R.drawable.image_placeholder)
                .target(imageView)
                .build()
            imageLoader.enqueue(imageRequest)

            itemView.setOnClickListener {
                it.startAnimation(AlphaAnimation(10f, 0.8f))

                val action = CatImagesFragmentDirections.actionCatImagesFragmentToCardCatFragment(
                    item?.id,
                    item?.url
                )
                it.findNavController().navigate(action)
            }

            favouriteBtn.visibility = View.VISIBLE

            favouriteBtn.setOnClickListener {
                if (favouritesViewModel.getCatDatabaseList()?.find { it.imageId == item?.id } == null) {
                    progressBar.visibility = View.VISIBLE

                    catImagesViewModel.compositeDisposable.add(catImagesViewModel.sendFavouriteToServer(item?.id!!)
                        .toObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext {
                            favouritesViewModel.insertFavouriteEntityToDatabase(
                                FavouriteIdsEntity(
                                    imageId = item.id,
                                    favouriteId = it.id!!
                                )
                            )
                        }
                        .doFinally {
                            progressBar.visibility = View.GONE
                        }
                        .subscribe({
                            Log.d("RETROFIT", "Successful image sending -> ${it.message}, id: ${it.id}")

                            favouriteBtn.setImageResource(R.drawable.ic_favourite_active)
                            Toast.makeText(itemView.context, "The cat was successfully added to favourites", Toast.LENGTH_SHORT).show()
                            favouritesViewModel.refreshFavourites()
                        }, {
                            Log.d("RETROFIT", "Exception during sendFavourite request -> ${it.localizedMessage}")
                            Toast.makeText(itemView.context, "Error while adding a cat to favorites", Toast.LENGTH_SHORT).show()
                        })
                    )
                } else {
                    progressBar.visibility = View.VISIBLE

                    catImagesViewModel.compositeDisposable.add(favouritesViewModel.deleteFavouriteFromServer(
                        favouritesViewModel.getCatDatabaseList()?.find { it.imageId == item?.id }!!.favouriteId
                    )
                        .toObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext {
                            favouritesViewModel.deleteFavouriteEntityFromDatabase(item?.id!!)
                        }
                        .doFinally {
                            progressBar.visibility = View.GONE
                        }
                        .subscribe({
                            Log.d("RETROFIT", "Successful image deleting from server -> ${it.message}, id: ${it.id}")

                            favouriteBtn.setImageResource(R.drawable.ic_favourite)
                            Toast.makeText(itemView.context, "The cat was successfully deleted from favourites", Toast.LENGTH_SHORT).show()
                            favouritesViewModel.refreshFavourites()
                        }, {
                            Log.d("RETROFIT", "Exception during deleteFavourite request -> ${it.localizedMessage}")
                            Toast.makeText(itemView.context, "Error while deleting a cat from favorites", Toast.LENGTH_SHORT).show()
                        })
                    )
                }
            }
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<CatImageResponse>() {
        override fun areItemsTheSame(oldItem: CatImageResponse, newItem: CatImageResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CatImageResponse, newItem: CatImageResponse): Boolean {
            return oldItem == newItem
        }
    }
}

