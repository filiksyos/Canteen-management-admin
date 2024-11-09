package com.canteenManagment.admin.ui.FoodDetail.editFood

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canteenManagment.admin.BaseActivity.BaseActivity
import com.canteenManagment.admin.ui.FoodDetail.listFood.FoodListActivity.Companion.FOOD_ITEM
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ActivityEditFoodBinding
import com.canteenManagment.admin.helper.CustomProgressBar
import com.canteenManagment.admin.helper.DeleteCustomDialog
import com.canteenManagment.admin.helper.showShortToast
import com.canteenManagment.admin.ui.FoodDetail.addFood.CustomeSpinnerAdapter
import com.canteenManagment.admin.ui.FoodDetail.listFood.FoodListActivity
import com.canteenmanagment.canteen_managment_library.apiManager.CustomeResult
import com.canteenmanagment.canteen_managment_library.apiManager.FirebaseApiManager
import com.canteenmanagment.canteen_managment_library.models.Food
import kotlinx.coroutines.launch

// Activity to edit existing food items, including updating details and handling deletion
class EditFoodActivity : BaseActivity(), View.OnClickListener, View.OnLongClickListener {

    private lateinit var binding: ActivityEditFoodBinding // Binding for safe access to views
    private val mContext: Context = this
    private val progressDialog: CustomProgressBar = CustomProgressBar(this) // Dialog to indicate progress
    private val deleteDialog: DeleteCustomDialog = DeleteCustomDialog(this) // Dialog to confirm deletion
    private var imageUri: Uri? = null // URI for the selected image
    private lateinit var food: Food // Food item to be edited

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding with the layout
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_food)
        setContentView(binding.root)

        // Retrieve food item from intent
        food = intent.getSerializableExtra(FOOD_ITEM) as Food

        // Set click listener for back button and update title text
        binding.IMback.setOnClickListener(this)
        binding.TVtitle.text = "Update Item"

        // Pre-fill text fields and spinner based on the existing food data
        binding.ETname.setText(food.name)
        binding.ETPrice.setText(food.price.toString())

        // Set up spinner with custom adapter and pre-select the stored counter number
        binding.SPCounterNumber.adapter = CustomeSpinnerAdapter(this, listOf(1, 2, 3, 4, 5))
        food.counterNumber?.let {
            binding.SPCounterNumber.setSelection(it - 1, true)
        }

        // Set checkboxes based on availability times in food data
        if (food.availableTimes?.contains("Morning") == true) binding.CHMorning.isChecked = true
        if (food.availableTimes?.contains("Afternoon") == true) binding.CHAfternoon.isChecked = true
        if (food.availableTimes?.contains("Evening") == true) binding.CHEvening.isChecked = true

        // Load food image using Glide
        Glide.with(this)
            .load(food.imageurl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.error_image)
            .error(R.drawable.error_image)
            .into(binding.IMFoodImage)

        // Set up listeners for update, delete, and image selection
        binding.BTUpdate.setOnClickListener(this)
        binding.BTDeleteFood.setOnClickListener(this)
        binding.IMFoodImage.setOnClickListener(this)
        binding.IMFoodImage.setOnLongClickListener(this)
    }

    // Function to get a list of selected availability times based on checked chips
    private fun getSelectedChip(): List<String> {
        val timeList = mutableListOf<String>()
        if (binding.CHMorning.isChecked) timeList.add("Morning")
        if (binding.CHAfternoon.isChecked) timeList.add("Afternoon")
        if (binding.CHEvening.isChecked) timeList.add("Evening")
        return timeList
    }

    // Handle click events for various actions
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.IMback -> super.onBackPressed() // Navigate back on back button click
            R.id.BT_update -> updateFood() // Trigger food update
            R.id.IM_Food_Image -> chooseImage() // Open image chooser
            R.id.BT_Delete_food -> deleteDialog.startDialog { deleteFood() } // Show delete dialog on delete button click
        }
    }

    // Long-click listener (no specific action required, returns true to indicate handled)
    override fun onLongClick(v: View?): Boolean = true

    // Handle image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data?.data != null) {
            imageUri = data.data

            // Load selected image into ImageView using Glide
            Glide.with(this)
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(binding.IMFoodImage)
        }
    }

    // Function to handle updating food data
    private fun updateFood() {
        progressDialog.startDialog() // Show progress dialog during update

        scope.launch {
            // Upload image if selected and get the URL
            uploadImage().let { uploadResult ->
                progressDialog.stopDiaolog() // Stop progress dialog after upload

                if (uploadResult.isSuccess || uploadResult.message == "Please Select image") {
                    // Update food item with entered data
                    food.name = binding.ETname.text.toString().trim()
                    food.price = binding.ETPrice.text.toString().trim().toInt()
                    food.counterNumber = binding.SPCounterNumber.selectedItemPosition + 1
                    food.availableTimes = getSelectedChip()

                    // If image was uploaded, update image URL
                    if (uploadResult.data != null) food.imageurl = uploadResult.data.toString()

                    progressDialog.startDialog() // Show progress dialog during database update

                    scope.launch {
                        // Store updated food data in Firebase and show feedback
                        FirebaseApiManager.updateFoodData(food).let { updateResult ->
                            progressDialog.stopDiaolog() // Stop progress dialog

                            when (updateResult.isSuccess) {
                                true -> {
                                    showShortToast(updateResult.message, mContext) // Show success message
                                    setResult(FoodListActivity.DATA_CHANGE) // Set result to indicate data change
                                    super.onBackPressed() // Return to previous screen
                                }
                                false -> showShortToast(updateResult.message, mContext) // Show error message
                            }
                        }
                    }
                } else {
                    // Show error message if image upload failed
                    showShortToast(uploadResult.message, mContext)
                }
            }
        }
    }

    // Function to upload the selected image to Firebase
    private suspend fun uploadImage(): CustomeResult {
        if (imageUri != null) {
            val cr = contentResolver
            val mime = MimeTypeMap.getSingleton()

            // Generate unique filename using food name and MIME type
            val filename = "${binding.ETname.text.toString().trim()}.${mime.getExtensionFromMimeType(imageUri?.let { cr.getType(it) })}"

            // Upload image to Firebase and return result
            return FirebaseApiManager.uploadFile(
                imageUri!!,
                filename,
                FirebaseApiManager.BaseUrl.FOOD + "/" + binding.ETname.text.toString().trim()
            )
        } else {
            return CustomeResult(false, "Please Select image") // Return error if no image selected
        }
    }

    // Function to delete food item from database
    private fun deleteFood() {
        scope.launch {
            // Delete food data from Firebase and show feedback
            FirebaseApiManager.deleteFoodData(food).let { deleteResult ->
                deleteDialog.stopDialog() // Stop delete dialog

                when (deleteResult.isSuccess) {
                    true -> {
                        showShortToast(deleteResult.message, mContext) // Show success message
                        setResult(FoodListActivity.DATA_CHANGE) // Set result to indicate data change
                        super.onBackPressed() // Return to previous screen
                    }
                    false -> showShortToast(deleteResult.message, mContext) // Show error message
                }
            }
        }
    }

    // Function to open intent for image selection
    private fun chooseImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, CHOOSE_IMAGE)
    }

    // Companion object to hold constants for request codes
    companion object {
        const val CHOOSE_IMAGE = 2001
    }
}

