package com.example.lab_8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.lab_8.model.TransactionModel
import com.example.lab_8.repository.TransactionRepository
import com.example.lab_8.ui.StatisticsScreen
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val repository by lazy { TransactionRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerApp(repository)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(repository: TransactionRepository) {
    var currentScreen by remember { mutableStateOf("transactions") }
    val transactions by repository.transactions.collectAsState()

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text("Учёт доходов и расходов") })
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Транзакции") },
                        label = { Text("Транзакции") },
                        selected = currentScreen == "transactions",
                        onClick = { currentScreen = "transactions" }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Статистика") },
                        label = { Text("Статистика") },
                        selected = currentScreen == "statistics",
                        onClick = { currentScreen = "statistics" }
                    )
                }
            }
        ) { padding ->
            when (currentScreen) {
                "transactions" -> TransactionsScreen(repository, padding)
                "statistics" -> StatisticsScreen(transactions)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(repository: TransactionRepository, padding: PaddingValues) {
    var description by remember { mutableStateOf(TextFieldValue()) }
    var amount by remember { mutableStateOf(TextFieldValue()) }
    var isIncome by remember { mutableStateOf(true) }
    var transactionToDelete by remember { mutableStateOf<TransactionModel?>(null) }
    val transactions by repository.transactions.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val currencies = listOf("BYN", "RUB", "USD", "EUR", "USDT")
    var selectedCurrency by remember { mutableStateOf(currencies.first()) }

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
    ) {
        // Выбор валюты
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCurrency,
                onValueChange = {},
                readOnly = true,
                label = { Text("Валюта") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            selectedCurrency = currency
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Сумма") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("Тип:")
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = isIncome,
                onClick = { isIncome = true },
                label = { Text("Доход") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = !isIncome,
                onClick = { isIncome = false },
                label = { Text("Расход") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val amt = amount.text.toDoubleOrNull() ?: 0.0
            if (description.text.isNotBlank() && amt > 0.0) {
                repository.addTransaction(description.text, amt, isIncome, selectedCurrency)
                description = TextFieldValue()
                amount = TextFieldValue()
            }
        }) {
            Text("Добавить")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(transactions) { tx ->
                TransactionRow(tx) {
                    transactionToDelete = tx
                }
            }
        }
    }

    // Диалог подтверждения удаления
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Подтверждение удаления") },
            text = { Text("Вы действительно хотите удалить эту транзакцию?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { repository.deleteTransaction(it.id) }
                        transactionToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun TransactionRow(tx: TransactionModel, onDelete: () -> Unit) {
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(tx.description, style = MaterialTheme.typography.titleMedium)
                    Text(if (tx.isIncome) "Доход" else "Расход", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (tx.isIncome) "+" else "-") + "%.2f ${tx.currency}".format(tx.amount),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                }
            }
            Text(
                text = formatter.format(tx.date),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
