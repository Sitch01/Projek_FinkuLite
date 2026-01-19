package com.talhaatif.financeapk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ExpenseFragment : Fragment() {

    companion object {
        fun newInstance() = ExpenseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Sementara tampilkan text sederhana
        return inflater.inflate(android.R.layout.simple_list_item_1, container, false).apply {
            findViewById<android.widget.TextView>(android.R.id.text1).text = "Ini Halaman Pengeluaran (Chart akan ditambah nanti)"
        }
    }
}