package com.canteenManagment.admin.ui.Fragments.Menu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import com.canteenManagment.admin.ui.FoodDetail.listFood.FoodListActivity
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.FragmentMenuBinding
import com.canteenmanagment.canteen_managment_library.models.Food

// Fragment to display menu categories and handle navigation to category-specific food lists
class MenuFragment : Fragment(), View.OnClickListener, View.OnLongClickListener {

    // Binding for safe access to layout views
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout and initialize binding
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        // Bind category names to layout views for display
        binding.snacks = Food.Category.SNACKS.value
        binding.animal = Food.Category.ANIMAL.value
        binding.drinks = Food.Category.DRINKS.value
        binding.vegan = Food.Category.VEGAN.value

        // Set up click listeners for each category card
        binding.CL1.setOnClickListener(this)
        binding.CL2.setOnClickListener(this)
        binding.CL3.setOnClickListener(this)
        binding.CL4.setOnClickListener(this)

        // Set up long click listeners for additional options if needed
        binding.CL1.setOnLongClickListener(this)
        binding.CL2.setOnLongClickListener(this)
        binding.CL3.setOnLongClickListener(this)
        binding.CL4.setOnLongClickListener(this)

        return binding.root // Return the root view of the binding layout
    }

    // Handle click events to navigate to FoodListActivity with selected category
    override fun onClick(v: View?) {
        val intent = Intent(activity?.applicationContext, FoodListActivity::class.java)
        var options: ActivityOptionsCompat? = null

        // Determine the category selected and set up transition animation
        when (v?.id) {
            R.id.CL1 -> {
                // Once, I create a new database, "Snacks" should be replaced with Food.Category.FIX_THALI.value
                intent.putExtra(CATEGORY_NAME, Food.Category.SNACKS.value)
                options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as Activity, binding.CL1, "open_transition")
            }
            R.id.CL2 -> {
                intent.putExtra(CATEGORY_NAME, Food.Category.ANIMAL.value)
                options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as Activity, binding.CL2, "open_transition")
            }
            R.id.CL3 -> {
                intent.putExtra(CATEGORY_NAME, Food.Category.DRINKS.value)
                options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as Activity, binding.CL3, "open_transition")
            }
            R.id.CL4 -> {
                intent.putExtra(CATEGORY_NAME, Food.Category.VEGAN.value)
                options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity as Activity, binding.CL4, "open_transition")
            }
        }

        // Start FoodListActivity with the selected category and transition options
        startActivity(intent, options?.toBundle())
    }

    // Handle long-click events; here returns true to signal long-click is handled
    override fun onLongClick(v: View?): Boolean {
        return true
    }

    // Clean up binding reference to avoid memory leaks
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        // Key for passing category name to FoodListActivity
        const val CATEGORY_NAME = "TITLE_NAME"
    }
}
