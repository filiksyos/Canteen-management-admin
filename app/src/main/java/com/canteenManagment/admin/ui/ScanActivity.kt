package com.canteenManagment.admin.ui

import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.canteenManagment.admin.BaseActivity.BaseActivity
import com.canteenManagment.admin.R
import com.canteenManagment.admin.databinding.ActivityScanBinding
import com.canteenManagment.admin.helper.CustomProgressBar
import com.canteenManagment.admin.helper.showShortToast
import com.canteenmanagment.canteen_managment_library.apiManager.FirebaseApiManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.launch

class ScanActivity : BaseActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var progressDialog: CustomProgressBar
    private val mContext = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan)
        setContentView(binding.root)

        // Set up the back button
        binding.IMback.setOnClickListener {
            onBackPressed()
        }

        progressDialog = CustomProgressBar(this)

        // Initialize DecoratedBarcodeView (ZXing scanner view)
        barcodeView = binding.barcodeScanner
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                // Handle the scan result
                val scannedText = result.text
                barcodeView.pause() // Stop scanning after receiving a result
                makeOrderSuccess(scannedText)
            }

            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {
                // You can handle potential scan points if needed
            }
        })
    }

    private fun makeOrderSuccess(orderId: String) {
        progressDialog.startDialog()
        scope.launch {
            FirebaseApiManager.makeOrderSuccess(orderId).let {
                progressDialog.stopDiaolog()
                if (it.isSuccess) {
                    showShortToast("Order Given Successfully", mContext)
                } else {
                    showShortToast(it.message, mContext)
                }
                // Resume scanning after handling result
                barcodeView.resume()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume() // Resume scanning
    }

    override fun onPause() {
        barcodeView.pause() // Pause scanning to release resources
        super.onPause()
    }
}
