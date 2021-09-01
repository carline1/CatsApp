package com.example.catsapp.ui.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.example.catsapp.R
import com.example.catsapp.api.models.res.FavouriteResponse
import com.example.catsapp.ui.fragments.CardFragmentSelectionEnum
import com.example.catsapp.ui.fragments.FavouritesCatsFragmentDirections
import com.example.catsapp.ui.viewmodels.CatViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class FavouriteCatsPagingAdapter(private val viewModel: CatViewModel) :
    PagingDataAdapter<FavouriteResponse, FavouriteCatsPagingAdapter.FavouriteCatsViewHolder>(DiffCallback) {

    override fun onBindViewHolder(holder: FavouriteCatsViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteCatsViewHolder {
        return FavouriteCatsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_view_holder, parent, false), viewModel)
    }

    class FavouriteCatsViewHolder(itemView: View, private val viewModel: CatViewModel) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val deleteFromFavouriteBtn = itemView.findViewById<ImageButton>(R.id.deleteFromFavouriteBtn)
        private val compositeDisposable = CompositeDisposable()

        fun bind(item: FavouriteResponse?) {
            itemView.apply {
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

                imageView.load(
                    item?.image?.url,
                    imageLoader
                ) {
                    crossfade(true)
                    placeholder(R.drawable.image_placeholder)
                }
            }

            itemView.setOnClickListener {
                it.startAnimation(AlphaAnimation(10f, 0.8f))

                val action = FavouritesCatsFragmentDirections.actionFavouritesCatsFragmentToCardCatFragment(
                    item?.image_id,
                    item?.image?.url,
                    null,
                    CardFragmentSelectionEnum.FavouriteCats
                )
                it.findNavController().navigate(action)
            }

            deleteFromFavouriteBtn.visibility = View.VISIBLE

            deleteFromFavouriteBtn.setOnClickListener {
                compositeDisposable.add(viewModel.deleteFavourite(item?.id.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        itemView.findNavController().navigate(R.id.action_favouritesCatsFragment_self)
                    }, {
                        Log.d("RETROFIT", "Exception during deleteFavourite request -> ${it.localizedMessage}")
                    })
                )
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FavouriteResponse>() {
        override fun areItemsTheSame(oldItem: FavouriteResponse, newItem: FavouriteResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavouriteResponse, newItem: FavouriteResponse): Boolean {
            return oldItem == newItem
        }
    }
}