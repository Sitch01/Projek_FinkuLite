package com.talhaatif.financeapk

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.talhaatif.financeapk.databinding.ActivityAddTransactionsBinding
import com.talhaatif.financeapk.dialog.CategoryPickerDialog
import com.talhaatif.financeapk.dialog.CustomProgressDialog
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.firebase.Variables
import com.talhaatif.financeapk.models.Category
import com.talhaatif.financeapk.viewmodel.AddTransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddTransactions : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionsBinding
    private lateinit var viewModel: AddTransactionViewModel
    private val utils = Util()
    private var selectedDate = ""
    private var selectedCategory: Category? = null
    private lateinit var progressDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = CustomProgressDialog(this)
        
        viewModel = ViewModelProvider(this).get(AddTransactionViewModel::class.java)

        // Date picker listeners
        val dateClickListener = { _: android.view.View -> showDatePicker() }
        binding.layoutDatePicker.setOnClickListener(dateClickListener)
        binding.btnDatePicker.setOnClickListener(dateClickListener)
        binding.tvDate.setOnClickListener(dateClickListener)

        // List of categories
        val categories = listOf(
            Category("Food", R.drawable.ic_food),
            Category("Transport", R.drawable.ic_baseline_directions_transit_24),
            Category("Shopping", R.drawable.ic_baseline_shopping_cart_24),
            Category("Entertainment", R.drawable.ic_entertain),
            Category("Health", R.drawable.ic_health)
        )

        // Category picker listeners
        val categoryClickListener = { _: android.view.View -> showCategoryPicker(categories) }
        binding.layoutCategoryPicker.setOnClickListener(categoryClickListener)
        binding.btnCategoryPicker.setOnClickListener(categoryClickListener)
        binding.selectedCategory.setOnClickListener(categoryClickListener)

        // Tab Layout configuration
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateUIForTab(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Initial UI state
        updateUIForTab(0)

        // Save Button logic
        binding.btnAddRecord.setOnClickListener {
            saveTransaction()
        }

        // Observe transaction status
        viewModel.transactionState.observe(this) { result ->
            progressDialog.dismiss()
            result.onSuccess {
                Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUIForTab(position: Int) {
        val currency = utils.getLocalData(this, "currency") ?: "Rp"
        if (position == 0) { // Expense
            binding.tvAmount.text = "-$currency"
            binding.tvAmount.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.etAmount.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.red))
            binding.tabLayout.setTabTextColors(Color.GRAY, ContextCompat.getColor(this, R.color.red))
        } else { // Income
            binding.tvAmount.text = "+$currency"
            binding.tvAmount.setTextColor(ContextCompat.getColor(this, R.color.green))
            binding.etAmount.setTextColor(ContextCompat.getColor(this, R.color.green))
            binding.tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.green))
            binding.tabLayout.setTabTextColors(Color.GRAY, ContextCompat.getColor(this, R.color.green))
        }
    }

    private fun showCategoryPicker(categories: List<Category>) {
        val dialog = CategoryPickerDialog(this, categories) { category ->
            selectedCategory = category
            binding.selectedCategory.text = category.name
            
            // Apply tint and icon
            val tintColor = Variables.categoryTintMap[category.name.lowercase()] ?: Color.BLACK
            binding.btnCategoryPicker.imageTintList = ColorStateList.valueOf(tintColor)
            binding.btnCategoryPicker.setImageResource(category.imageResId)
        }
        dialog.show()
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selection
            }
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            binding.tvDate.text = selectedDate
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun saveTransaction() {
        val amountInput = binding.etAmount.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val transactionType = if (binding.tabLayout.selectedTabPosition == 0) "Expense" else "Income"

        if (amountInput.isEmpty()) {
            Toast.makeText(this, "Masukkan jumlah uang", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Pilih tanggal", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedCategory == null) {
            Toast.makeText(this, "Pilih kategori", Toast.LENGTH_SHORT).show()
            return
        }

        val currencyType = utils.getLocalData(this, "currency") ?: "Rp"
        val fullAmountString = "$amountInput $currencyType"

        progressDialog.setMessage("Menyimpan Transaksi...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        viewModel.addTransaction(
            this,
            fullAmountString,
            transactionType,
            selectedDate,
            notes,
            category = selectedCategory!!.name.lowercase()
        )
    }
}
