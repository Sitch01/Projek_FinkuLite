package com.talhaatif.financeapk

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.talhaatif.financeapk.databinding.FragmentHomeBinding
import com.talhaatif.financeapk.models.BudgetModel
import com.talhaatif.financeapk.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        pieChart = binding.pieChart

        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.fab.setOnClickListener {
            val intent = Intent(requireActivity(), AddTransactions::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        homeViewModel.budgetData.observe(viewLifecycleOwner) { budget ->
            val currency = homeViewModel.currencyType.value ?: "USD"
            updateUI(budget, currency)
        }

        homeViewModel.currencyType.observe(viewLifecycleOwner) { currency ->
            val budget = homeViewModel.budgetData.value
            updateUI(budget, currency)
        }
    }

    private fun updateUI(budget: BudgetModel?, currency: String) {
        if (budget == null) {
            binding.tvBalanceAmount.text = "$currency 0"
            binding.tvIncomeAmount.text = "$currency 0"
            binding.tvExpenseAmount.text = "$currency 0"
            return
        }

        binding.tvBalanceAmount.text = "$currency ${budget.balance}"
        binding.tvIncomeAmount.text = "$currency ${budget.income}"
        binding.tvExpenseAmount.text = "$currency ${budget.expense}"
        
        setupPieChart(budget)
    }

    private fun setupPieChart(budget: BudgetModel) {
        val entries = mutableListOf<PieEntry>()
        
        // Hanya tampilkan jika nilai > 0 agar chart tidak aneh
        if (budget.income > 0) entries.add(PieEntry(budget.income.toFloat(), "Pemasukan"))
        if (budget.expense > 0) entries.add(PieEntry(budget.expense.toFloat(), "Pengeluaran"))

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("Belum ada data transaksi")
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            // Hijau untuk Income, Merah untuk Expense
            colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"))
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 60f
            setHoleColor(Color.TRANSPARENT)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            setCenterText("Ringkasan")
            setCenterTextSize(16f)
            
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 12f
            }
            
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
