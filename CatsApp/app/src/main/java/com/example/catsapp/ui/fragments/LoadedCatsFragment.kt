package com.example.catsapp.ui.fragments

import android.Manifest
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catsapp.R
import com.example.catsapp.databinding.FragmentLoadedCatsBinding
import com.example.catsapp.ui.adapters.LoadedCatsPagingAdapter
import com.example.catsapp.ui.viewmodels.CatViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class LoadedCatsFragment : Fragment(), ImagePickerDialogFragment.ImagePickerDialogListener {

    private lateinit var binding: FragmentLoadedCatsBinding
    private val viewModel by activityViewModels<CatViewModel>()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadedCatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagingAdapter = LoadedCatsPagingAdapter(viewModel)
        val recyclerView = view.findViewById<RecyclerView>(R.id.loadedListRecyclerView)
        recyclerView.adapter = pagingAdapter
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)
        val query = mutableMapOf(
            "order" to "asc",
            "sub_id" to CatViewModel.SUB_ID
        )

        compositeDisposable.add(viewModel.getLoadedImages(query).subscribe {
            pagingAdapter.submitData(lifecycle, it)
        })

        binding.addCardButton.setOnClickListener {
            val dialog = ImagePickerDialogFragment()
            dialog.show(childFragmentManager, "ImagePickerDialogFragment")
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private val camera = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        compositeDisposable.add(viewModel.sendImage(bitmap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.loadedListRecyclerView.findNavController().navigate(R.id.action_loadedCatsFragment_self)
            }, {
                Log.d("RETROFIT", "Exception during sendImage request -> ${it.localizedMessage}")
            })
        )
    }

    private val gallery = registerForActivityResult(ActivityResultContracts.GetContent()) { callback ->
        compositeDisposable.add(
            viewModel.sendImage(MediaStore.Images.Media.getBitmap(requireContext().contentResolver, callback))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.loadedListRecyclerView.findNavController().navigate(R.id.action_loadedCatsFragment_self)
            }, {
                Log.d("RETROFIT", "Exception during sendImage request -> ${it.localizedMessage}")
            })
        )
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            camera.launch()
        } else {
            Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            gallery.launch("image/*")
        } else {
            Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDialogGalleryClick(dialog: DialogFragment) {
        storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onDialogCameraClick(dialog: DialogFragment) {
        cameraPermission.launch(Manifest.permission.CAMERA)
    }

}