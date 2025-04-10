package com.example.lab_8.repository

import android.content.Context
import com.example.lab_8.data.AppDatabase
import com.example.lab_8.data.TransactionEntity
import com.example.lab_8.model.TransactionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class TransactionRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).transactionDao()
    private val _transactions = MutableStateFlow<List<TransactionModel>>(emptyList())
    val transactions: StateFlow<List<TransactionModel>> = _transactions

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val entities = dao.getAll()
            _transactions.value = entities.map { it.toModel() }
        }
    }

    fun addTransaction(description: String, amount: Double, isIncome: Boolean, currency: String) {
        val tx = TransactionEntity(UUID.randomUUID().toString(), description, amount, isIncome, Date(), currency)
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(tx)
            val updatedList = dao.getAll().map { it.toModel() }
            _transactions.value = updatedList
        }
    }

    private fun TransactionEntity.toModel() =
        TransactionModel(id, description, amount, isIncome, date, currency)
} 