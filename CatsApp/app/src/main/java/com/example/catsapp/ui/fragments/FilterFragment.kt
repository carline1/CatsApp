package com.example.catsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.catsapp.R
import com.example.catsapp.api.models.res.BreedFilterResponse
import com.example.catsapp.api.models.res.CategoryFilterResponse
import com.example.catsapp.databinding.FragmentFilterBinding
import com.example.catsapp.ui.viewmodels.CatViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class FilterFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFilterBinding
    private val compositeDisposable = CompositeDisposable()

    private val viewModel by activityViewModels<CatViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.breedList.isNotEmpty() && viewModel.categoryList.isNotEmpty()) {
            setupSpinners(view, viewModel.breedList, viewModel.categoryList)
        } else {
            @Suppress("UNCHECKED_CAST")
            compositeDisposable.add(viewModel.getBreedsAndCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val noneBreedFilterResponse = BreedFilterResponse("", "None")
                    val noneCategoryFilterResponse = CategoryFilterResponse(-1, "None")
                    viewModel.breedList = listOf(noneBreedFilterResponse) + (it[CatViewModel.BREED_LIST_KEY] as List<BreedFilterResponse>)
                    viewModel.categoryList = listOf(noneCategoryFilterResponse) + (it[CatViewModel.CATEGORY_LIST_KEY] as List<CategoryFilterResponse>)

                    setupSpinners(view, viewModel.breedList, viewModel.categoryList)
                }, {
                    Log.d("RETROFIT", "Exception during breedAndCategory request -> ${it.localizedMessage}")
                })
            )
        }

        binding.applyFilterBtn.setOnClickListener {
            val requestParams = mutableMapOf(
                "order" to binding.spinnerOrder.selectedItem.toString(),
                "mime_types" to convertTypeName(binding.spinnerType.selectedItem.toString())
            )
            if ((binding.spinnerBreed.selectedItem as BreedFilterResponse).name != "None")
                requestParams["breed_id"] = (binding.spinnerBreed.selectedItem as BreedFilterResponse).id
            if ((binding.spinnerCategory.selectedItem as CategoryFilterResponse).name != "None")
                requestParams["category_ids"] = (binding.spinnerCategory.selectedItem as CategoryFilterResponse).id.toString()

            viewModel.requestParams = requestParams
            findNavController().navigate(R.id.action_filterFragment_to_catsImagesFragment)
        }
    }

    private fun convertTypeName(typeName: String): String {
        return when(typeName) {
            "All" -> "gif,jpg,png"
            "Static" -> "jpg,png"
            "Animated" -> "gif"
            else -> throw IllegalArgumentException()
        }
    }

    private fun setupSpinners(view: View, breedList: List<BreedFilterResponse>, categoryList: List<CategoryFilterResponse>) {
        // Setup spinners
        val adapterOrderSpinner: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            view.context,
            R.array.orderNames,
            android.R.layout.simple_spinner_item
        )
        adapterOrderSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrder.adapter = adapterOrderSpinner

        val adapterTypeSpinner: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            view.context,
            R.array.typeNames,
            android.R.layout.simple_spinner_item
        )
        adapterTypeSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = adapterTypeSpinner

        val adapterBreedSpinner: ArrayAdapter<BreedFilterResponse> = ArrayAdapter<BreedFilterResponse>(
            view.context,
            android.R.layout.simple_spinner_item,
            breedList
        )
        adapterBreedSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBreed.adapter = adapterBreedSpinner

        val adapterCategoryFilterSpinner: ArrayAdapter<CategoryFilterResponse> = ArrayAdapter<CategoryFilterResponse>(
            view.context,
            android.R.layout.simple_spinner_item,
            categoryList
        )
        adapterCategoryFilterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapterCategoryFilterSpinner


        // Restore filter fragment instance state
        viewModel.bundleFilterFragment.observe(viewLifecycleOwner) {
            binding.spinnerOrder.setSelection(it.getInt(ORDER_POSITION_KEY))
            binding.spinnerType.setSelection(it.getInt(TYPE_POSITION_KEY))
            binding.spinnerBreed.setSelection(it.getInt(BREED_POSITION_KEY))
            binding.spinnerCategory.setSelection(it.getInt(CATEGORY_POSITION_KEY))
        }

        binding.filterProgressBar.visibility = View.INVISIBLE
        binding.filterMenu.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.bundleFilterFragment.value = bundleOf(
            ORDER_POSITION_KEY to binding.spinnerOrder.selectedItemPosition,
            TYPE_POSITION_KEY to binding.spinnerType.selectedItemPosition,
            BREED_POSITION_KEY to binding.spinnerBreed.selectedItemPosition,
            CATEGORY_POSITION_KEY to binding.spinnerCategory.selectedItemPosition
        )
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
        const val ORDER_POSITION_KEY = "order_position"
        const val TYPE_POSITION_KEY = "type_position"
        const val BREED_POSITION_KEY = "breed_position"
        const val CATEGORY_POSITION_KEY = "category_position"
    }
}