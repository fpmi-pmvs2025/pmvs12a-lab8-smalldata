package com.example.lab_8.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lab_8.model.TransactionModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.NumberFormat
import java.util.*

@Composable
fun StatisticsScreen(transactions: List<TransactionModel>) {
    val income = transactions.filter { it.isIncome }.sumOf { it.amount }
    val expenses = transactions.filter { !it.isIncome }.sumOf { it.amount }
    val balance = income - expenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Общая статистика
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .padding(top = 90.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Общая статистика",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp).padding(top = 8.dp)
                )
                StatisticRow("Доходы", income)
                StatisticRow("Расходы", expenses)
                StatisticRow("Баланс", balance)
            }
        }

        // Круговая диаграмма
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        setHoleColor(android.graphics.Color.TRANSPARENT)
                        setTransparentCircleColor(android.graphics.Color.TRANSPARENT)
                        setDrawHoleEnabled(true)
                        setHoleRadius(50f)
                        setTransparentCircleRadius(55f)
                        setDrawCenterText(true)
                        centerText = "Доходы/Расходы"
                        setCenterTextSize(16f)
                        animateY(1000)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { chart ->
                    val entries = listOf(
                        PieEntry(income.toFloat(), "Доходы"),
                        PieEntry(expenses.toFloat(), "Расходы")
                    )
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = listOf(
                            android.graphics.Color.GREEN,
                            android.graphics.Color.RED
                        )
                        valueTextSize = 12f
                        valueTextColor = android.graphics.Color.WHITE
                    }
                    chart.data = PieData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun StatisticRow(label: String, value: Double) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            text = formatter.format(value),
            style = MaterialTheme.typography.bodyLarge
        )
    }
} 