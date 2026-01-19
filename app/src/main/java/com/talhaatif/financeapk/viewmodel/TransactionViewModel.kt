package com.talhaatif.financeapk.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.models.Transaction

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val utils = Util()

    private val _transactions = MutableLiveData<Map<String, List<Transaction>>>()
    val transactions: LiveData<Map<String, List<Transaction>>> get() = _transactions

    fun fetchTransactions() {
        val userId = auth.currentUser?.uid ?: utils.getLocalData(getApplication(), "uid")
        
        if (userId.isNullOrEmpty()) {
            _transactions.value = emptyMap()
            return
        }

        db.collection("transactions")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { result ->
                val transactionsList = mutableListOf<Transaction>()
                for (document in result) {
                    try {
                        val transAmount = document.get("transAmount")
                        val amountStr = if (transAmount is Double) {
                            transAmount.toString()
                        } else {
                            document.getString("transAmount") ?: "0"
                        }

                        val transaction = Transaction(
                            uid = document.getString("uid") ?: "",
                            transAmount = amountStr,
                            transType = document.getString("transType") ?: "",
                            transDate = document.getString("transDate") ?: "",
                            notes = document.getString("notes") ?: "",
                            category = document.getString("category") ?: ""
                        )
                        transactionsList.add(transaction)
                    } catch (e: Exception) {
                        Log.e("TransactionVM", "Error parsing transaction: ${e.message}")
                    }
                }
                _transactions.value = groupTransactionsByDate(transactionsList)
            }
            .addOnFailureListener { e ->
                Log.e("TransactionVM", "Error fetching transactions: ${e.message}")
                _transactions.value = emptyMap()
            }
    }

    private fun groupTransactionsByDate(transactions: List<Transaction>): Map<String, List<Transaction>> {
        // Sort transactions by date descending (optional but recommended)
        return transactions.sortedByDescending { it.transDate }.groupBy { it.transDate }
    }
}
