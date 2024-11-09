package com.canteenManagment.admin.ui.Fragments.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ItemFoodListLayoutBinding
import com.canteenManagment.admin.databinding.ItemPreparingFoodListLayoutBinding
import com.canteenmanagment.canteen_managment_library.models.Food
import com.canteenmanagment.canteen_managment_library.models.Order

// Adapter to display a list of in-progress orders in a RecyclerView, allowing the user to mark orders as ready
class PrepareOrderListRecyclerViewAdapter(
    val orderList: List<Order>, // List of orders to display
    val makeOrderReady: (order: Order) -> Unit // Lambda function to mark an order as ready
) : RecyclerView.Adapter<PrepareOrderListRecyclerViewAdapter.ViewHolder>() {

    // Inflates the item layout and returns a ViewHolder with the binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPreparingFoodListLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    // Binds data for each order item to the corresponding views in the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]

        // Display the Order ID
        holder.binding.TVOrderId.text = "Order ID : ${order.id}"

        // Build and display a list of food items and their quantities for this order
        val foodList = StringBuilder()
        for (cartFood in order.foodList!!) {
            foodList.append("${cartFood.food.name} X ${cartFood.quantity}\n")
        }
        holder.binding.TVFoodItems.text = foodList.toString()

        // Set up click listener to mark the order as ready
        holder.binding.BTReady.setOnClickListener {
            makeOrderReady(order) // Call the lambda function with the selected order
        }
    }

    // Returns the total count of items in the order list
    override fun getItemCount(): Int {
        return orderList.size
    }

    // ViewHolder class that holds references to the views in each item layout
    class ViewHolder(var binding: ItemPreparingFoodListLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
