package com.canteenManagment.admin.helper

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.view.LayoutInflater
import com.canteenManagment.admin.databinding.DeleteCustomeDiologBinding // Ensure the generated binding import is correct

class DeleteCustomDialog(private val activity: Activity) {

    private lateinit var alertDialog: Dialog
    private lateinit var binding: DeleteCustomeDiologBinding

    fun startDialog(deleteFood: () -> Unit) {
        // Inflate the layout with ViewBinding
        binding = DeleteCustomeDiologBinding.inflate(LayoutInflater.from(activity))

        // Initialize AlertDialog builder
        val dialogBuilder = AlertDialog.Builder(activity)

        // Set up button click listeners with ViewBinding
        binding.BTCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.BTDelete.setOnClickListener {
            deleteFood()
        }

        dialogBuilder.setView(binding.root)

        alertDialog = dialogBuilder.create()
        alertDialog.show()
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun stopDialog() {
        alertDialog.dismiss()
    }
}
