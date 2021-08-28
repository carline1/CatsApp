package com.example.catsapp.ui.paging

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
import coil.load
import com.example.catsapp.R
import com.example.catsapp.api.models.res.CatImageResponse
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.catsapp.ui.fragments.CardFragmentSelectionEnum
import com.example.catsapp.ui.fragments.CatsImagesFragmentDirections
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class CatImagePagingAdapter(context: Context, private val viewModel: CatViewModel) :
    PagingDataAdapter<CatImageResponse, CatImagePagingAdapter.ImageViewHolder>(DiffCallback) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(inflater.inflate(R.layout.image_view_holder, parent, false), viewModel)
    }

    class ImageViewHolder(itemView: View, private val viewModel: CatViewModel) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val favoriteBtn = itemView.findViewById<ImageButton>(R.id.favoriteBtn)
        private val compositeDisposable = CompositeDisposable()

        fun bind(item: CatImageResponse?) {
            itemView.apply {
                val imageLoader =
                    ImageLoader.Builder(context)
                        .componentRegistry {
                            if (SDK_INT >= 28) {
                                add(ImageDecoderDecoder(context))
                            } else {
                                add(GifDecoder())
                            }
                        }
                        .build()

                imageView.load(
                    item?.url,
                    imageLoader
                ) {
                    crossfade(true)
                    placeholder(R.drawable.image_placeholder)
                }
            }

            itemView.setOnClickListener {
                it.startAnimation(AlphaAnimation(10f, 0.8f))

                Log.d("TEST", "${item?.breeds.toString()}, ${item?.id}, ${item?.url}")

                val breed = if (item?.breeds?.size != 0) item?.breeds?.get(0) else null
                val action = CatsImagesFragmentDirections.actionCatsImagesFragmentToCardCatFragment(
                    item?.id,
                    item?.url,
                    breed,
                    CardFragmentSelectionEnum.CatImage
                )
                it.findNavController().navigate(action)
            }

            favoriteBtn.visibility = View.VISIBLE

            favoriteBtn.setOnClickListener {
                compositeDisposable.add(viewModel.setFavorite(item?.id!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("RETROFIT", "${it.message}, id: ${it.id}")
                        Toast.makeText(itemView.context, "The cat was successfully added to favorites", Toast.LENGTH_SHORT).show()
                    }, {
                        Toast.makeText(itemView.context, "The cat is already in the favorites", Toast.LENGTH_SHORT).show()
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

