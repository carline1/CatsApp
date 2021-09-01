package com.example.catsapp.ui.adapters

import android.os.Build
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
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
import com.example.catsapp.api.models.res.CatImageResponse
import com.example.catsapp.ui.fragments.LoadedCatsFragmentDirections
import com.example.catsapp.ui.viewmodels.CatViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class LoadedCatsPagingAdapter(private val viewModel: CatViewModel)
    : PagingDataAdapter<CatImageResponse, LoadedCatsPagingAdapter.ImageViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_view_holder, parent, false), viewModel)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class ImageViewHolder(itemView: View, private val viewModel: CatViewModel) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        private val compositeDisposable = CompositeDisposable()

        fun bind(item: CatImageResponse?) {
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

                val action = LoadedCatsFragmentDirections.actionLoadedCatsFragmentToCatAnalysisCardFragment(
                    item?.url,
                    item?.id
                )
                it.findNavController().navigate(action)
            }

            itemView.setOnCreateContextMenuListener { menu, v, menuInfo ->
                menu?.add(0, v?.id!!, 0, "Delete")
                    ?.setOnMenuItemClickListener {
                        compositeDisposable.add(viewModel.deleteUploadImage(item?.id!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                itemView.findNavController().navigate(R.id.action_loadedCatsFragment_self)
//                                Toast.makeText(v.context, "Cat successfully deleted!", Toast.LENGTH_SHORT).show()
                            }, {
                                itemView.findNavController().navigate(R.id.action_loadedCatsFragment_self)
                                Log.d("RETROFIT", "Exception during deleteUploadedImage request -> ${it.localizedMessage}")
//                                Toast.makeText(v.context, "Error! Try again", Toast.LENGTH_SHORT).show()
                            }))
                        true
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