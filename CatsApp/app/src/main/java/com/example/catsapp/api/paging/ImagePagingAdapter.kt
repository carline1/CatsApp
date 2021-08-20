package com.example.catsapp.api.paging

import android.content.Context
import android.os.Build.VERSION.SDK_INT
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
import com.example.catsapp.api.models.ImageResponse

class ImagePagingAdapter(context: Context) :
    PagingDataAdapter<ImageResponse, ImagePagingAdapter.ImageViewHolder>(DiffCallback) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(inflater.inflate(R.layout.image_view_holder, parent, false))
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)

        fun bind(item: ImageResponse?) {
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
                }
            }
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<ImageResponse>() {
        override fun areItemsTheSame(oldItem: ImageResponse, newItem: ImageResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageResponse, newItem: ImageResponse): Boolean {
            return oldItem == newItem
        }
    }
}

