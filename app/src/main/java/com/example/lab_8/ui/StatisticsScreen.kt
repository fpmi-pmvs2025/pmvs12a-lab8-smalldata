package com.example.lab_8.ui

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.lab_8.model.TransactionModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.*

@Composable
fun StatisticsScreen(transactions: List<TransactionModel>) {
    var exchangeRates by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        exchangeRates = fetchExchangeRates()
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val transactionsInUsd = transactions.mapNotNull { transaction ->
        val rate = exchangeRates[transaction.currency.uppercase()]
        rate?.let {
            transaction.copy(amount = transaction.amount / it)
        }
    }

    val income = transactionsInUsd.filter { it.isIncome }.sumOf { it.amount }
    val expenses = transactionsInUsd.filter { !it.isIncome }.sumOf { it.amount }
    val balance = income - expenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                    text = "Общая статистика (в USD)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(top = 8.dp)
                )
                StatisticRow("Доходы", income)
                StatisticRow("Расходы", expenses)
                StatisticRow("Баланс", balance)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        setHoleColor(Color.TRANSPARENT)
                        setTransparentCircleColor(Color.TRANSPARENT)
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
                        colors = listOf(Color.GREEN, Color.RED)
                        valueTextSize = 12f
                        valueTextColor = Color.WHITE
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
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

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

suspend fun fetchExchangeRates(): Map<String, Double> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://open.er-api.com/v6/latest/USD")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val data = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(data)
            val ratesJson = json.getJSONObject("rates")

            val rates = mutableMapOf<String, Double>()
            for (key in ratesJson.keys()) {
                rates[key] = ratesJson.getDouble(key)
            }
            rates["USDT"] = 1.0 // добавим USDT вручную
            rates
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
