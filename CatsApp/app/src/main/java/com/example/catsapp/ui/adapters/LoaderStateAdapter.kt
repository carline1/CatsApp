package com.example.catsapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.databinding.LoadStateItemBinding

class LoaderStateAdapter: LoadStateAdapter<LoaderStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = LoadStateViewHolder(parent)

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState
    ) = holder.bind(loadState)

    class LoadStateViewHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.load_state_item, parent, false)
    ) {

        private val binding = LoadStateItemBinding.bind(itemView)
        private val progressBar: ProgressBar = binding.progressBar
        private val errorMsg: TextView = binding.errorMsg

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                errorMsg.text = loadState.error.localizedMessage
            }

            progressBar.isVisible = loadState is LoadState.Loading
            errorMsg.isVisible = loadState is LoadState.Error
        }
    }
}

