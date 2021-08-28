package com.example.catsapp.ui.paging

import android.content.Context
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
import com.example.catsapp.api.models.res.FavoriteResponse
import com.example.catsapp.ui.fragments.CardFragmentSelectionEnum
import com.example.catsapp.ui.fragments.FavoritesCatsFragmentDirections
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class FavoriteCatsPagingAdapter(context: Context, private val viewModel: CatViewModel) :
    PagingDataAdapter<FavoriteResponse, FavoriteCatsPagingAdapter.FavoriteCatsViewHolder>(DiffCallback) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: FavoriteCatsViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteCatsViewHolder {
        return FavoriteCatsViewHolder(inflater.inflate(R.layout.image_view_holder, parent, false), viewModel)
    }

    class FavoriteCatsViewHolder(itemView: View, private val viewModel: CatViewModel) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val deleteFromFavoriteBtn = itemView.findViewById<ImageButton>(R.id.deleteFromFavoriteBtn)
        private val compositeDisposable = CompositeDisposable()

        fun bind(item: FavoriteResponse?) {
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

                val action = FavoritesCatsFragmentDirections.actionFavoritesCatsFragmentToCardCatFragment(
                    item?.image_id,
                    item?.image?.url,
                    null,
                    CardFragmentSelectionEnum.FavoriteCats
                )
                it.findNavController().navigate(action)
            }

            deleteFromFavoriteBtn.visibility = View.VISIBLE

            deleteFromFavoriteBtn.setOnClickListener {
                compositeDisposable.add(viewModel.deleteFavorite(item?.id.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        itemView.findNavController().navigate(R.id.action_favoritesCatsFragment_self)
                    }, {
                        Log.d("RETROFIT", "Exception during deleteFavorite request -> ${it.localizedMessage}")
                    })
                )
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FavoriteResponse>() {
        override fun areItemsTheSame(oldItem: FavoriteResponse, newItem: FavoriteResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavoriteResponse, newItem: FavoriteResponse): Boolean {
            return oldItem == newItem
        }
    }
}