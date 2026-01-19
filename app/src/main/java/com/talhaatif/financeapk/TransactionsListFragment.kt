package com.talhaatif.financeapk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.talhaatif.financeapk.adapter.TransactionsAdapter
import com.talhaatif.financeapk.databinding.FragmentTransactionsListBinding
import com.talhaatif.financeapk.viewmodel.TransactionViewModel


class TransactionsListFragment : Fragment() {

    private lateinit var binding: FragmentTransactionsListBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "Transactions Fragment", Toast.LENGTH_SHORT).show()

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        adapter = TransactionsAdapter(emptyMap())

        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecyclerView.adapter = adapter

        viewModel.transactions.observe(viewLifecycleOwner) { groupedTransactions ->
            adapter.updateData(groupedTransactions)
        }

        viewModel.fetchTransactions()
    }
}

