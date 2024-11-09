package com.canteenManagment.admin.ui.FoodDetail.addFood

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.canteenManagment.admin.BaseActivity.BaseActivity
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ActivityAddFoodBinding
import com.canteenManagment.admin.helper.CustomProgressBar
import com.canteenManagment.admin.helper.showShortToast
import com.canteenManagment.admin.ui.FoodDetail.listFood.FoodListActivity.Companion.DATA_CHANGE
import com.canteenManagment.admin.ui.Fragments.Menu.MenuFragment.Companion.CATEGORY_NAME
import com.canteenmanagment.canteen_managment_library.apiManager.CustomeResult
import com.canteenmanagment.canteen_managment_library.apiManager.FirebaseApiManager
import com.canteenmanagment.canteen_managment_library.models.Food
import kotlinx.coroutines.launch

// Activity to handle adding a new food item, including selecting an image, entering details, and storing data
class AddFoodActivity : BaseActivity(), View.OnClickListener, View.OnLongClickListener {

    private lateinit var binding: ActivityAddFoodBinding // Binding for safe access to views
    private val mContext: Context = this
    private val progressDialog: CustomProgressBar = CustomProgressBar(this) // Progress dialog to indicate loading state
    private var imageUri: Uri? = null // URI for storing selected food image

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding with the layout
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_food)
        setContentView(binding.root)

        // Set up back button and title text based on selected category
        binding.IMback.setOnClickListener(this)
        binding.TVtitle.text = "Add ${intent.getStringExtra(CATEGORY_NAME)}"

        // Set up click listener for Add button to trigger addFood method
        binding.BTAdd.setOnClickListener(this)

        // Set up spinner for selecting counter number with custom adapter
        binding.SPCounterNumber.adapter = CustomeSpinnerAdapter(this, listOf(1, 2, 3, 4, 5))

        // Set click and long-click listeners for selecting food image
        binding.IMFoodImage.setOnClickListener(this)
        binding.IMFoodImage.setOnLongClickListener(this)
    }

    // Handle click events for back button, Add button, and food image
    override fun onClick(v: View?) {
        when (v?.id) {
            // Handle back button click with custom transition
            R.id.IMback -> {
                super.onBackPressed()
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_bottom)
            }
            // Trigger addFood method when Add button is clicked
            R.id.BT_add -> addFood()

            // Open image chooser to select a food image
            R.id.IM_Food_Image -> chooseImage()
        }
    }

    // Function to add a new food item to the database
    private fun addFood() {
        progressDialog.startDialog()

        scope.launch {
            uploadImage().let { uploadResult ->
                progressDialog.stopDiaolog()

                if (uploadResult.isSuccess) {
                    val uploadedImageUrl = uploadResult.data.toString()  // Use the URL directly from uploadResult
                    Log.d("AddFoodActivity", "Image uploaded successfully, URL: $uploadedImageUrl")

                    val food = Food().apply {
                        name = binding.ETname.text.toString().trim()
                        price = binding.ETPrice.text.toString().trim().toInt()
                        counterNumber = binding.SPCounterNumber.selectedItemPosition + 1
                        category = intent.getStringExtra(CATEGORY_NAME)
                        available = true
                        imageurl = uploadedImageUrl  // Ensure this is the full URL
                        availableTimes = getSelectedChip()
                    }

                    progressDialog.startDialog()

                    FirebaseApiManager.storeFoodData(food).let { storeResult ->
                        progressDialog.stopDiaolog()
                        Log.d("AddFoodActivity", "Store result: ${storeResult.message}")

                        if (storeResult.isSuccess) {
                            showShortToast(storeResult.message, mContext)
                            setResult(DATA_CHANGE)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
                        } else {
                            showShortToast(storeResult.message, mContext)
                        }
                    }
                } else {
                    showShortToast(uploadResult.message, mContext)
                    Log.e("AddFoodActivity", "Image upload failed: ${uploadResult.message}")
                }
            }
        }
    }

    // Function to upload the selected image to Firebase and return the result
    private suspend fun uploadImage(): CustomeResult {
        if (imageUri != null) {
            val cr = contentResolver
            val mime = MimeTypeMap.getSingleton()

            // Generate file extension, default to "jpg" if MIME type is not found
            val fileExtension = mime.getExtensionFromMimeType(imageUri?.let { cr.getType(it) }) ?: "jpg"
            val filename = "${binding.ETname.text.toString().trim()}.$fileExtension"  // Filename only, e.g., "hffy.jpg"

            // Log the upload details for debugging
            Log.d("FirebaseStorage", "Uploading to path: ${FirebaseApiManager.BaseUrl.FOOD}")
            Log.d("FirebaseStorage", "Image URI: $imageUri")
            Log.d("FirebaseStorage", "Generated Filename: $filename")

            // Upload image to Firebase Storage under "Food" folder only
            return FirebaseApiManager.uploadFile(
                imageUri!!,
                filename,
                FirebaseApiManager.BaseUrl.FOOD  // Upload to "Food" folder only
            )
        } else {
            Log.e("FirebaseStorage", "Image URI is null; please select an image.")
            return CustomeResult(false, "Please select an image") // Return error if no image selected
        }
    }


    // Function to open an intent for image selection
    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, CHOOSE_IMAGE)
    }

    // Function to retrieve selected availability times from chips
    private fun getSelectedChip(): List<String> {
        val timeList = mutableListOf<String>()
        if (binding.CHMorning.isChecked) timeList.add("Morning")
        if (binding.CHAfternoon.isChecked) timeList.add("Afternoon")
        if (binding.CHEvening.isChecked) timeList.add("Evening")
        return timeList
    }

    // Override back button with custom transition animation
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    // Long-click listener (no specific action required, returns true to indicate handled)
    override fun onLongClick(v: View?): Boolean = true

    // Handle result from image chooser
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

    // Companion object to hold constants for result codes
    companion object {
        const val CHOOSE_IMAGE = 2001 // Request code for image selection intent
    }
}

