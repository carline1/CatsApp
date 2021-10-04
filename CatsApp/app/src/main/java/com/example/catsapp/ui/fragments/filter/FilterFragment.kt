package com.example.catsapp.ui.fragments.filter

import android.os.Bundle
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
import com.example.catsapp.ui.common.CatsAppKeys
import com.example.catsapp.ui.fragments.catImages.CatImagesViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class FilterFragment : BottomSheetDialogFragment() {

    private var binding: FragmentFilterBinding? = null

    private val filterViewModel by activityViewModels<FilterViewModel>()
    private val catImagesViewModel by activityViewModels<CatImagesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFilterBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (filterViewModel.breedList.isNotEmpty() && filterViewModel.categoryList.isNotEmpty()) {
            setupSpinners(view, filterViewModel.breedList, filterViewModel.categoryList)
        } else {
            @Suppress("UNCHECKED_CAST")
            filterViewModel.getBreedsAndCategoriesFromServerStatus.observe(viewLifecycleOwner) {
                val noneBreedFilterResponse = BreedFilterResponse("", "None")
                val noneCategoryFilterResponse = CategoryFilterResponse(-1, "None")

                val breadList =
                    listOf(noneBreedFilterResponse) + (it[CatsAppKeys.BREED_LIST_KEY] as List<BreedFilterResponse>)
                val categoryList =
                    listOf(noneCategoryFilterResponse) + (it[CatsAppKeys.CATEGORY_LIST_KEY] as List<CategoryFilterResponse>)
                filterViewModel.setupBreedList(breadList)
                filterViewModel.setupCategoryList(categoryList)

                setupSpinners(view, filterViewModel.breedList, filterViewModel.categoryList)
            }
            filterViewModel.getBreedsAndCategoriesFromServer()
        }

        binding?.applyFilterBtn?.setOnClickListener {
            val requestParams = mutableMapOf(
                "order" to binding?.spinnerOrder?.selectedItem.toString(),
                "mime_types" to convertTypeName(binding?.spinnerType?.selectedItem.toString())
            )
            if ((binding?.spinnerBreed?.selectedItem as BreedFilterResponse).name !in listOf(
                    "None",
                    "Error"
                ) &&
                (binding?.spinnerBreed?.selectedItem as BreedFilterResponse).id != null
            )
                requestParams["breed_id"] =
                    (binding?.spinnerBreed?.selectedItem as BreedFilterResponse).id.toString()
            if ((binding?.spinnerCategory?.selectedItem as CategoryFilterResponse).name !in listOf(
                    "None",
                    "Error"
                ) &&
                (binding?.spinnerCategory?.selectedItem as CategoryFilterResponse).id != null
            )
                requestParams["category_ids"] =
                    (binding?.spinnerCategory?.selectedItem as CategoryFilterResponse).id.toString()

            catImagesViewModel.setRequestParams(requestParams)
            catImagesViewModel.refreshCatImages()
            findNavController().navigate(R.id.action_filterFragment_to_catImagesFragment)
        }

        binding?.resetFilterBtn?.setOnClickListener {
            binding?.spinnerOrder?.setSelection(0)
            binding?.spinnerType?.setSelection(0)
            binding?.spinnerBreed?.setSelection(0)
            binding?.spinnerCategory?.setSelection(0)
        }
    }

    private fun convertTypeName(typeName: String): String {
        return when (typeName) {
            "All" -> "gif,jpg,png"
            "Static" -> "jpg,png"
            "Animated" -> "gif"
            else -> throw IllegalArgumentException()
        }
    }

    private fun setupSpinners(
        view: View,
        breedList: List<BreedFilterResponse>,
        categoryList: List<CategoryFilterResponse>
    ) {
        // Setup spinners
        val adapterOrderSpinner: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            view.context,
            R.array.orderNames,
            android.R.layout.simple_spinner_item
        )
        adapterOrderSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinnerOrder?.adapter = adapterOrderSpinner

        val adapterTypeSpinner: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            view.context,
            R.array.typeNames,
            android.R.layout.simple_spinner_item
        )
        adapterTypeSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinnerType?.adapter = adapterTypeSpinner

        val adapterBreedSpinner: ArrayAdapter<BreedFilterResponse> =
            ArrayAdapter<BreedFilterResponse>(
                view.context,
                android.R.layout.simple_spinner_item,
                breedList
            )
        adapterBreedSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinnerBreed?.adapter = adapterBreedSpinner

        val adapterCategoryFilterSpinner: ArrayAdapter<CategoryFilterResponse> =
            ArrayAdapter<CategoryFilterResponse>(
                view.context,
                android.R.layout.simple_spinner_item,
                categoryList
            )
        adapterCategoryFilterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinnerCategory?.adapter = adapterCategoryFilterSpinner


        // Restore filter fragment instance state
        val bundleFilterFragment = filterViewModel.bundleFilterFragment
        binding?.spinnerOrder?.setSelection(bundleFilterFragment.getInt(ORDER_POSITION_KEY))
        binding?.spinnerType?.setSelection(bundleFilterFragment.getInt(TYPE_POSITION_KEY))
        binding?.spinnerBreed?.setSelection(bundleFilterFragment.getInt(BREED_POSITION_KEY))
        binding?.spinnerCategory?.setSelection(bundleFilterFragment.getInt(CATEGORY_POSITION_KEY))


        binding?.filterProgressBar?.visibility = View.INVISIBLE
        binding?.filterMenu?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val filterBundle = bundleOf(
            ORDER_POSITION_KEY to binding?.spinnerOrder?.selectedItemPosition,
            TYPE_POSITION_KEY to binding?.spinnerType?.selectedItemPosition,
            BREED_POSITION_KEY to binding?.spinnerBreed?.selectedItemPosition,
            CATEGORY_POSITION_KEY to binding?.spinnerCategory?.selectedItemPosition
        )

        filterViewModel.setupBundleFilter(filterBundle)
    }

    companion object {
        const val ORDER_POSITION_KEY = "order_position"
        const val TYPE_POSITION_KEY = "type_position"
        const val BREED_POSITION_KEY = "breed_position"
        const val CATEGORY_POSITION_KEY = "category_position"
    }
}