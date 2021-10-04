package com.example.catsapp.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.databinding.LoadStateItemBinding

class LoaderStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<LoaderStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = LoadStateViewHolder(parent, retry)

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState
    ) = holder.bind(loadState)

    class LoadStateViewHolder(
        parent: ViewGroup,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.load_state_item, parent, false)
    ) {

        private val binding = LoadStateItemBinding.bind(itemView)
        private val progressBar: ProgressBar = binding.progressBar
        private val retryBtn: Button = binding.retryButton
            .also { it.setOnClickListener { retry.invoke() } }

        fun bind(loadState: LoadState) {
            progressBar.isVisible = loadState is LoadState.Loading
            retryBtn.isVisible = loadState !is LoadState.Loading
        }
    }
}

