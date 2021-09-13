package com.example.catsapp.ui.adapters

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
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.findNavController
import coil.request.ImageRequest
import com.example.catsapp.ui.fragments.CatsImagesFragmentDirections
import com.example.catsapp.ui.viewmodels.CatViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class CatImagePagingAdapter(context: Context, private val viewModel: CatViewModel) :
    PagingDataAdapter<CatImageResponse, CatImagePagingAdapter.ImageViewHolder>(DiffCallback) {

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
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_view_holder, parent, false), viewModel)
    }

    inner class ImageViewHolder(itemView: View, private val viewModel: CatViewModel) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val favouriteBtn = itemView.findViewById<ImageButton>(R.id.favouriteBtn)
        private val compositeDisposable = CompositeDisposable()

        fun bind(item: CatImageResponse?) {
            if (item?.id in viewModel.favouritesLocalStorage)
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

                val action = CatsImagesFragmentDirections.actionCatsImagesFragmentToCardCatFragment(
                    item?.id,
                    item?.url
                )
                it.findNavController().navigate(action)
            }

            favouriteBtn.visibility = View.VISIBLE

            favouriteBtn.setOnClickListener {
                compositeDisposable.add(viewModel.sendFavouriteToServer(item?.id!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { compositeDisposable.dispose() }
                    .subscribe({
                        Log.d("RETROFIT", "Successful image sending -> ${it.message}, id: ${it.id}")
                        viewModel.addFavToStorage(item.id)
                        favouriteBtn.setImageResource(R.drawable.ic_favourite_active)
                        Toast.makeText(itemView.context, "The cat was successfully added to favourites", Toast.LENGTH_SHORT).show()
                        viewModel.refreshFavourites()
                    }, {
                        Log.d("RETROFIT", "Exception during sendFavourite request -> ${it.localizedMessage}")
                        viewModel.addFavToStorage(item.id)
                        favouriteBtn.setImageResource(R.drawable.ic_favourite_active)
                        Toast.makeText(itemView.context, "The cat is already in the favourites", Toast.LENGTH_SHORT).show()
                    })
                )
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

