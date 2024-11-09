package com.canteenManagment.admin.ui.FoodDetail.listFood

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.canteenManagment.admin.BaseActivity.BaseActivity
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ActivityFoodListBinding
import com.canteenManagment.admin.helper.CustomProgressBar
import com.canteenManagment.admin.helper.showShortToast
import com.canteenManagment.admin.ui.FoodDetail.addFood.AddFoodActivity
import com.canteenManagment.admin.ui.FoodDetail.editFood.EditFoodActivity
import com.canteenManagment.admin.ui.Fragments.Menu.MenuFragment.Companion.CATEGORY_NAME
import com.canteenmanagment.canteen_managment_library.apiManager.FirebaseApiManager
import com.canteenmanagment.canteen_managment_library.models.Food
import kotlinx.coroutines.launch

// Activity to display a list of food items within a selected category and handle actions like editing and adding food items
class FoodListActivity : BaseActivity(), View.OnClickListener {

    // Binding to access layout views safely
    lateinit var binding: ActivityFoodListBinding
    private val mContext: Context = this
    private lateinit var foodList: List<Food> // List to hold food items for the selected category
    private val progressDialog: CustomProgressBar = CustomProgressBar(this) // Progress dialog for loading indication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_list)

        // Initialize view binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_list)
        // Set the title to the category name passed in the intent
        binding.title = intent.getStringExtra(CATEGORY_NAME)

        // Set up click listeners for the back button and add button
        binding.IMback.setOnClickListener(this)
        binding.FABadd.setOnClickListener(this)

        // Load data to display food items in the RecyclerView
        loadData()

        // Set up pull-to-refresh behavior to reload food items
        binding.SRRefreshLayout.setOnRefreshListener {
            loadData()
        }
    }

    // Function to load food items for the selected category from Firebase and display in the RecyclerView
    private fun loadData() {
        scope.launch {
            // Fetch all food items for the selected category
            FirebaseApiManager.getAllFoodFromCategory(intent.getStringExtra(CATEGORY_NAME)!!).let {
                binding.SRRefreshLayout.isRefreshing = false // Stop refreshing indicator
                foodList = it // Assign fetched data to foodList
                binding.RVFoodList.visibility = View.VISIBLE // Make RecyclerView visible

                // Set up the RecyclerView adapter with the fetched food data and click listeners
                binding.RVFoodList.adapter = FoodListRecyclerViewAdapter(it,
                    FoodListRecyclerViewAdapter.ClickListener(
                        // Navigate to EditFoodActivity to edit selected food item
                        { position ->
                            val intent = Intent(mContext, EditFoodActivity::class.java)
                            intent.putExtra(FOOD_ITEM, foodList[position])
                            startActivityForResult(intent, DATA_CHANGE)
                        },
                        // Change availability status of a food item
                        { position, status ->
                            val food = foodList[position]
                            food.available = status
                            updateFood(food) // Update food availability in Firebase
                        })
                )
            }
        }
    }

    // Function to update a food item's availability in Firebase
    private fun updateFood(food: Food) {
        progressDialog.startDialog() // Show loading dialog

        scope.launch {
            // Update food item in Firebase
            FirebaseApiManager.updateFoodData(food).let {
                progressDialog.stopDiaolog() // Hide loading dialog
                if (!it.isSuccess) {
                    showShortToast(it.message, mContext) // Show error if update fails
                }
            }
        }
    }

    // Handle click events for back button and add button
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.IMback -> onBackPressed() // Handle back button click
            R.id.FABadd -> { // Handle add button click to add a new food item
                val intent = Intent(this, AddFoodActivity::class.java)
                intent.putExtra(CATEGORY_NAME, intent.getStringExtra(CATEGORY_NAME))
                startActivityForResult(intent, DATA_CHANGE)
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
            }
        }
    }

    // Override back button to handle custom visibility changes
    override fun onBackPressed() {
        binding.CL.visibility = View.INVISIBLE
        super.onBackPressed()
    }

    // Handle result from activities like AddFoodActivity or EditFoodActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DATA_CHANGE) {
            binding.RVFoodList.visibility = View.GONE // Hide RecyclerView during refresh
            loadData() // Reload data after changes
        }
    }

    // Companion object to hold constants for data keys
    companion object {
        const val FOOD_ITEM = "Food Item" // Key for passing food item data
        const val DATA_CHANGE = 2002 // Request code for data changes
    }
}
