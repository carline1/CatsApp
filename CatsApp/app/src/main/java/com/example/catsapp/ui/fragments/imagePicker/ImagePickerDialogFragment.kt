package com.example.catsapp.ui.fragments.imagePicker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.catsapp.R

class ImagePickerDialogFragment : DialogFragment() {

    private lateinit var listener: ImagePickerDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
            .setTitle("Choose image picker")
            .setItems(R.array.chooseImagePicker) { dialog, which ->
                when(which){
                    0 -> listener.onDialogGalleryClick(this)
                    1 -> listener.onDialogCameraClick(this)
                }
            }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = parentFragment as ImagePickerDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    interface ImagePickerDialogListener {
        fun onDialogGalleryClick(dialog: DialogFragment)
        fun onDialogCameraClick(dialog: DialogFragment)
    }
}