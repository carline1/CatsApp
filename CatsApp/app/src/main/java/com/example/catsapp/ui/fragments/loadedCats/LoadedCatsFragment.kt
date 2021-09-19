package com.example.catsapp.ui.fragments.loadedCats

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.databinding.FragmentLoadedCatsBinding
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.map
import androidx.paging.filter
import com.example.catsapp.ui.common.LoaderStateAdapter
import com.example.catsapp.ui.fragments.imagePicker.ImagePickerDialogFragment


class LoadedCatsFragment : Fragment(), ImagePickerDialogFragment.ImagePickerDialogListener {

    private var binding: FragmentLoadedCatsBinding? = null
    private val loadedCatsViewModel by activityViewModels<LoadedCatsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoadedCatsBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagingAdapter = LoadedCatsPagingAdapter(requireContext(), loadedCatsViewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.loadedListRecyclerView)
        recyclerView.adapter = pagingAdapter

        val layoutManager = GridLayoutManager(view.context, 2)
        recyclerView.layoutManager = layoutManager
        layoutManager.spanSizeLookup =  object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == pagingAdapter.itemCount  && pagingAdapter.itemCount > 0) {
                    2
                } else {
                    1
                }
            }
        }

        recyclerView.adapter = pagingAdapter.withLoadStateHeaderAndFooter(
            header = LoaderStateAdapter { pagingAdapter.retry() },
            footer = LoaderStateAdapter { pagingAdapter.retry() }
        )

        binding?.loadedCatsRetryButton?.setOnClickListener { pagingAdapter.retry() }

        pagingAdapter.addLoadStateListener { state ->
            binding?.loadedListRecyclerView?.isVisible = state.refresh !is LoadState.Loading
            binding?.loadedCatsProgressBar?.isVisible = state.refresh is LoadState.Loading
            binding?.loadedCatsRetryButton?.isVisible = state.refresh is LoadState.Error
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loadedCatsViewModel.loadedImages.map { pagingData ->
                pagingData.filter { it.id !in loadedCatsViewModel.getDeletedFavourites() }
            }
                .observe(viewLifecycleOwner, {
                pagingAdapter.submitData(lifecycle, it)
            })
        }


        binding?.addCardButton?.setOnClickListener {
            val dialog = ImagePickerDialogFragment()
            dialog.show(childFragmentManager, "ImagePickerDialogFragment")
        }
    }

    private val camera = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null)
            uploadImage(bitmap)
    }

    private val gallery = registerForActivityResult(ActivityResultContracts.GetContent()) { callback: Uri? ->
        if (callback != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, callback)
            uploadImage(bitmap)
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        binding?.loadedCatsUploadingProgressBar?.visibility = View.VISIBLE
        binding?.loadedCatsContainer?.visibility = View.GONE

        loadedCatsViewModel.compositeDisposable.add(loadedCatsViewModel.sendImageToServer(bitmap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                binding?.loadedCatsUploadingProgressBar?.visibility = View.GONE
                binding?.loadedCatsContainer?.visibility = View.VISIBLE
            }
            .subscribe({
                Log.d("RETROFIT", "Successful upload image to server -> " +
                        "${it.message}, id: ${it.id}")
                loadedCatsViewModel.refreshLoadedImages()
                binding?.loadedListRecyclerView?.findNavController()?.navigate(R.id.action_loadedCatsFragment_self)
                Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_LONG).show()
            }, {
                Log.d("RETROFIT", "Exception during sendImage request -> ${it.localizedMessage}")
                Toast.makeText(context, "Error! Image not uploaded", Toast.LENGTH_LONG).show()
            })
        )
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            granted -> camera.launch()
            !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                binding?.loadedCatsContainer?.let {
                    Snackbar
                        .make(
                            it,
                            "You need to access camera permission to upload image",
                            Snackbar.LENGTH_LONG
                        )
                        .setAction("Settings") {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireContext().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private val storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        when {
            granted -> gallery.launch("image/*")
            !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                binding?.loadedCatsContainer?.let {
                    Snackbar
                        .make(
                            it,
                            "You need to access storage permission to upload image",
                            Snackbar.LENGTH_LONG
                        )
                        .setAction("Settings") {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", requireContext().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    override fun onDialogGalleryClick(dialog: DialogFragment) {
        storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onDialogCameraClick(dialog: DialogFragment) {
        cameraPermission.launch(Manifest.permission.CAMERA)
    }
}