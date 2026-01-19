package com.talhaatif.financeapk

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.talhaatif.financeapk.databinding.ActivitySignUpScreenBinding
import com.talhaatif.financeapk.dialog.CustomProgressDialog
import com.talhaatif.financeapk.viewmodel.AuthViewModel
import java.io.ByteArrayOutputStream

class SignUpScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpScreenBinding
    private val viewModel: AuthViewModel by viewModels()
    private var imgChange = false
    private lateinit var imageUri: Uri
    private lateinit var bitmap: Bitmap
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = CustomProgressDialog(this)


        val currencies = listOf("IDR", "USD", "EUR", "PKR", "INR", "GBP")
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, currencies)
        (binding.currencySelector as? MaterialAutoCompleteTextView)?.setAdapter(adapter)

        // Set the default selected value to "IDR" (Rupiah)
        binding.currencySelector.setText("IDR", false) 

        binding.register.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val name = binding.name.text.toString()
            var currency = binding.currencySelector.text.toString()




            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || currency.isEmpty()) {
                Toast.makeText(this, "Harap isi semua bidang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            currency = when {
                currency.equals("IDR", true) -> "Rp"
                currency.equals("PKR", true) -> "Rs"
                currency.equals("INR", true) -> "₹"
                currency.equals("EUR", true) -> "€"
                currency.equals("GBP", true) -> "£"
                currency.equals("USD", true) -> "$"
                else -> "Rp"
            }



            // Check if an image is selected
            val finalImageUri = if (imgChange) imageUri else null
            val finalBitmap = if (imgChange) bitmap else null

            progressDialog.setMessage("Creating Account...")
            progressDialog.show()
            viewModel.signUp(this, email, password, name, currency, finalImageUri , finalBitmap)
        }

        binding.imageView.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1)
        }

        viewModel.authState.observe(this, Observer {
            progressDialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        })

        viewModel.errorMessage.observe(this, Observer { message ->
            progressDialog.dismiss()
            Toast.makeText(this, "Gagal: ${message}", Toast.LENGTH_SHORT).show()
        })



        binding.login.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data!!
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            binding.imageView.setImageBitmap(bitmap)
            imgChange = true
        }
    }
}
