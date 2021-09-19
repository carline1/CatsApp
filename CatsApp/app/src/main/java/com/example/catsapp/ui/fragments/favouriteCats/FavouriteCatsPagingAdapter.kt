package com.example.catsapp.ui.fragments.favouriteCats

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.catsapp.R
import com.example.catsapp.api.models.res.FavouriteResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class FavouriteCatsPagingAdapter(
    context: Context,
    private val favouritesViewModel: FavouriteCatsViewModel
) : PagingDataAdapter<FavouriteResponse, FavouriteCatsPagingAdapter.FavouriteCatsViewHolder>(
    DiffCallback
) {

    private val imageLoader =
        ImageLoader.Builder(context)
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder(context))
                } else {
                    add(GifDecoder())
                }
            }
            .build()

    override fun onBindViewHolder(holder: FavouriteCatsViewHolder, position: Int) {
        return holder.bind(getItem(position), position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteCatsViewHolder {
        return FavouriteCatsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.image_view_holder, parent, false), favouritesViewModel
        )
    }

    inner class FavouriteCatsViewHolder(
        itemView: View,
        private val favouritesViewModel: FavouriteCatsViewModel
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val deleteFromFavouriteBtn =
            itemView.findViewById<ImageButton>(R.id.deleteFromFavouriteBtn)

        fun bind(item: FavouriteResponse?, position: Int) {
            val imageRequest = ImageRequest.Builder(imageView.context)
                .data(item?.image?.url)
                .crossfade(10)
                .placeholder(R.drawable.image_placeholder)
                .target(imageView)
                .build()
            imageLoader.enqueue(imageRequest)

            itemView.setOnClickListener {
                it.startAnimation(AlphaAnimation(10f, 0.8f))

                val action =
                    FavouriteCatsFragmentDirections.actionFavouriteCatsFragmentToCardCatFragment(
                        item?.image_id,
                        item?.image?.url
                    )
                it.findNavController().navigate(action)
            }

            deleteFromFavouriteBtn.visibility = View.VISIBLE

            deleteFromFavouriteBtn.setOnClickListener {
                favouritesViewModel.compositeDisposable.add(favouritesViewModel.deleteFavouriteFromServer(
                    item?.id.toString()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d(
                            "RETROFIT",
                            "Successful image deleting from favourites-> ${it.message}, id: ${it.id}"
                        )
                        favouritesViewModel.deleteFavouriteFromLiveData(item)
                        notifyItemRangeChanged(position, itemCount - position)
                    }, {
                        Log.d(
                            "RETROFIT",
                            "Exception during deleteFavourite request -> ${it.localizedMessage}"
                        )
                        Toast.makeText(
                            imageView.context,
                            "Image deleting error",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                )
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FavouriteResponse>() {
        override fun areItemsTheSame(
            oldItem: FavouriteResponse,
            newItem: FavouriteResponse
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FavouriteResponse,
            newItem: FavouriteResponse
        ): Boolean {
            return oldItem == newItem
        }
    }
}