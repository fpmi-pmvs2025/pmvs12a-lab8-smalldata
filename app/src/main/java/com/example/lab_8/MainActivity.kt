package com.example.lab_8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import androidx.room.*

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val description: String,
    val amount: Double,
    val isIncome: Boolean,
    val date: Date,
    val currency: String
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>

    @Insert
    suspend fun insert(transaction: Transaction)
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

@Database(entities = [Transaction::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker.db"
                )
                    .build().also { INSTANCE = it }
            }
        }
    }
}

class TransactionRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).transactionDao()
    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: List<Transaction> get() = _transactions

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val entities = dao.getAll()
            _transactions.addAll(entities.map { it.toModel() })
        }
    }

    fun addTransaction(description: String, amount: Double, isIncome: Boolean, currency: String) {
        val tx = Transaction(UUID.randomUUID().toString(), description, amount, isIncome, Date(), currency)
        _transactions.add(tx)
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(tx.toEntity())
        }
    }

    private fun Transaction.toEntity() =
        Transaction(id, description, amount, isIncome, date, currency)

    private fun Transaction.toModel() =
        Transaction(id, description, amount, isIncome, date, currency)
}


/** Main Activity **/
class MainActivity : ComponentActivity() {
    private val repository by lazy { TransactionRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerApp(repository)
        }
    }
}

/** UI **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(repository: TransactionRepository) {
    var description by remember { mutableStateOf(TextFieldValue()) }
    var amount by remember { mutableStateOf(TextFieldValue()) }
    var isIncome by remember { mutableStateOf(true) }

    val transactions = repository.transactions

    var expanded by remember { mutableStateOf(false) }
    val currencies = listOf("BYN", "RUB", "USD", "EUR", "USDT")
    var selectedCurrency by remember { mutableStateOf(currencies.first()) }

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text("Учёт доходов и расходов") })
            },
            content = { padding ->
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
                            TransactionRow(tx)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun TransactionRow(tx: Transaction) {
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(tx.description, style = MaterialTheme.typography.titleMedium)
                    Text(if (tx.isIncome) "Доход" else "Расход", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = (if (tx.isIncome) "+" else "-") + "%.2f ${tx.currency}".format(tx.amount),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Дата: ${formatter.format(tx.date)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
