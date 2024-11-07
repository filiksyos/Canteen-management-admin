package com.canteenManagment.admin.ui.FoodDetail.listFood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ItemFoodListLayoutBinding
import com.canteenmanagment.canteen_managment_library.models.Food

class FoodListRecyclerViewAdapter(
    private val foodList: List<Food>,
    private val listener: ClickListener
) : RecyclerView.Adapter<FoodListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodListLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foodList[position]

        // Set up UI elements
        holder.binding.apply {
            TVTitle.text = food.name
            TVPrice.text = "${food.price} Rs."

            // Set availability
            TBAvailable.isChecked = food.available

            // Set on click listeners
            root.setOnClickListener { listener.openActivity(position) }
            TBAvailable.setOnClickListener {
                listener.changeFoodStatus(position, TBAvailable.isChecked)
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

    override fun getItemCount(): Int = foodList.size

    // ViewHolder class with binding
    class ViewHolder(val binding: ItemFoodListLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    // Listener class with lambdas for item clicks and food status updates
    class ClickListener(
        val openActivity: (Int) -> Unit,
        val changeFoodStatus: (Int, Boolean) -> Unit
    )
}
