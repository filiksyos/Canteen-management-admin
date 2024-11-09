package com.canteenManagment.admin.ui.FoodDetail.addFood

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ItemCustomSpinnerBinding

// Custom adapter for displaying integer options in a Spinner
class CustomeSpinnerAdapter(context: Context, list: List<Int>) : ArrayAdapter<Int>(context, R.layout.item_custom_spinner, list) {

    // Returns the view for the selected item in the Spinner
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    // Returns the dropdown view for each item in the Spinner
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    // Initialize the view for each item, setting the display text
    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate item layout using view binding
        val binding = ItemCustomSpinnerBinding.inflate(LayoutInflater.from(context), parent, false)
        // Set item text based on its value
        binding.TVtitle.text = getItem(position).toString()
        return binding.root
    }
}
