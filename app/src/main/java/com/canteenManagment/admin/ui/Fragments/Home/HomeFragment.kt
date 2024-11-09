package com.canteenManagment.admin.ui.Fragments.Home

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.canteenManagment.admin.databinding.FragmentHomeBinding
import com.canteenManagment.admin.helper.CustomProgressBar
import com.canteenmanagment.canteen_managment_library.apiManager.FirebaseApiManager
import com.canteenmanagment.canteen_managment_library.models.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    // View binding property to access layout views
    private var _binding: FragmentHomeBinding? = null
    // Safe non-null access to binding (only valid between onCreateView and onDestroyView)
    private val binding get() = _binding!!

    // Coroutine scope to handle background tasks on the main thread
    val scope = CoroutineScope(Dispatchers.Main)

    // List to store in-progress orders from Firebase
    private lateinit var orderList: MutableList<Order>

    // Custom progress dialog to indicate loading state
    private lateinit var progressDialog: CustomProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialize view binding with the fragment's layout
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize the custom progress dialog
        progressDialog = CustomProgressBar(activity as Activity)

        // Load and display data in the RecyclerView
        loadData()

        return binding.root // Return the root view of the binding layout
    }

    // Function to load data from Firebase and display in the RecyclerView
    private fun loadData() {
        progressDialog.startDialog() // Show loading indicator
        scope.launch {
            // Fetch all in-progress orders from Firebase
            FirebaseApiManager.getAllInProgressOrder().let {
                progressDialog.stopDiaolog() // Hide loading indicator
                if (it.isSuccess) {
                    // Set orderList with fetched data and update RecyclerView adapter
                    orderList = it.data as MutableList<Order>
                    binding.OrderRecyclerView.adapter = PrepareOrderListRecyclerViewAdapter(orderList) { order ->
                        makeOrderReady(order)
                    }
                }
            }
        }
    }

    // Function to update an order status to "ready" and refresh the RecyclerView
    private fun makeOrderReady(order: Order) {
        progressDialog.startDialog() // Show loading indicator
        scope.launch {
            // Mark order as ready in Firebase
            FirebaseApiManager.makeOrderReady(order).let {
                progressDialog.stopDiaolog() // Hide loading indicator
                if (it.isSuccess) {
                    // Remove the completed order from the list
                    orderList.remove(order)
                    // Update the RecyclerView with the updated order list
                    binding.OrderRecyclerView.adapter = PrepareOrderListRecyclerViewAdapter(orderList) { order ->
                        makeOrderReady(order)
                    }
                }
            }
        }
    }

    // Clean up view binding reference to avoid memory leaks
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
