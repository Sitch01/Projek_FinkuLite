package com.talhaatif.financeapk.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.talhaatif.financeapk.R

class CustomProgressDialog(context: Context) : Dialog(context) {

    private var message: String = "Loading..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) // Remove the default title
        setContentView(R.layout.custom_progress_dialog)

        // Set transparent background
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set the loading message
        val tvLoadingMessage = findViewById<TextView>(R.id.tvLoadingMessage)
        tvLoadingMessage.text = message
    }

    // Method to update the loading message
    fun setMessage(message: String) {
        this.message = message
        if (isShowing) {
            findViewById<TextView>(
                R.id.tvLoadingMessage).text = message
        }
    }
}