package com.example.lab_8.model

import java.util.Date

data class TransactionModel(
    val id: String,
    val description: String,
    val amount: Double,
    val isIncome: Boolean,
    val date: Date,
    val currency: String
) 