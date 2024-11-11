package com.canteenManagment.admin.ui.FoodDetail.listFood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ItemFoodListLayoutBinding
import com.canteenmanagment.canteen_managment_library.models.Food

// RecyclerView adapter to display a list of food items with options to view or change availability status
class FoodListRecyclerViewAdapter(
    private val foodList: List<Food>, // List of food items to display
    private val listener: ClickListener // Listener for handling item click events
) : RecyclerView.Adapter<FoodListRecyclerViewAdapter.ViewHolder>() {

    // Inflate item layout and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodListLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    // Bind data for each food item to its corresponding views in the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foodList[position] // Get the food item at the current position

        // Bind food item data to UI elements
        holder.binding.apply {
            TVTitle.text = food.name // Set the food name
            TVPrice.text = "${food.price} Birr." // Set the food price with currency

            // Set the availability toggle
            TBAvailable.isChecked = food.available

            // Set up click listeners for item and availability toggle
            root.setOnClickListener { listener.openActivity(position) }
            TBAvailable.setOnClickListener {
                listener.changeFoodStatus(position, TBAvailable.isChecked) // Update food availability
            }

            // Load food image using Glide with caching and error handling
            Glide.with(root.context)
                .load(food.imageurl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(IMFoodImage)
        }
    }

    // Return the total count of items in the food list
    override fun getItemCount(): Int = foodList.size

    // ViewHolder class that holds references to the views in each item layout
    class ViewHolder(val binding: ItemFoodListLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    // Listener class with lambdas for item click and food status change actions
    class ClickListener(
        val openActivity: (Int) -> Unit, // Lambda to open activity for editing the selected food item
        val changeFoodStatus: (Int, Boolean) -> Unit // Lambda to change food availability status
    )
}
