package com.talhaatif.financeapk.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.talhaatif.financeapk.firebase.Util

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val utils = Util()

    val transactionState = MutableLiveData<Result<Boolean>>()

    fun addTransaction(context: Context, amountString: String, transType: String, selectedDate: String, notes: String, category : String) {
        val userId = auth.currentUser?.uid ?: utils.getLocalData(context, "uid")

        if (userId.isNullOrEmpty()) {
            transactionState.value = Result.failure(Exception("User ID tidak ditemukan. Silakan login ulang."))
            return
        }

        val cleanAmountString = amountString.replace("[^\\d.]".toRegex(), "")
        val amountDouble = cleanAmountString.toDoubleOrNull() ?: 0.0

        if (amountDouble <= 0) {
            transactionState.value = Result.failure(Exception("Jumlah transaksi tidak valid"))
            return
        }

        val transaction = hashMapOf(
            "uid" to userId,
            "transAmount" to amountDouble,
            "transType" to transType,
            "transDate" to selectedDate,
            "notes" to notes,
            "category" to category
        )

        db.collection("transactions").add(transaction)
            .addOnSuccessListener {
                updateBudget(userId, transType, amountDouble)
            }
            .addOnFailureListener { e ->
                transactionState.value = Result.failure(e)
            }
    }

    private fun updateBudget(userId: String, transType: String, amount: Double) {
        val budgetRef = db.collection("budget").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(budgetRef)

            // Jika dokumen budget belum ada, buat baru dengan nilai awal
            if (!snapshot.exists()) {
                val initialIncome = if (transType == "Income") amount else 0.0
                val initialExpense = if (transType == "Expense") amount else 0.0
                val initialBalance = if (transType == "Income") amount else -amount
                
                val newBudget = hashMapOf(
                    "uid" to userId,
                    "balance" to initialBalance,
                    "income" to initialIncome,
                    "expense" to initialExpense
                )
                transaction.set(budgetRef, newBudget)
            } else {
                // Jika sudah ada, update nilainya
                val currentBalance = snapshot.getDouble("balance") ?: 0.0
                val currentIncome = snapshot.getDouble("income") ?: 0.0
                val currentExpense = snapshot.getDouble("expense") ?: 0.0

                val newBalance = if (transType == "Income") currentBalance + amount else currentBalance - amount
                val newIncome = if (transType == "Income") currentIncome + amount else currentIncome
                val newExpense = if (transType == "Expense") currentExpense + amount else currentExpense

                transaction.update(budgetRef, "balance", newBalance)
                transaction.update(budgetRef, "income", newIncome)
                transaction.update(budgetRef, "expense", newExpense)
            }
            null // Transaction function must return something
        }.addOnSuccessListener {
            transactionState.value = Result.success(true)
        }.addOnFailureListener { e ->
            Log.e("AddTransaction", "Error updating budget: ${e.message}")
            transactionState.value = Result.failure(e)
        }
    }
}
