package com.example.lab_8

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab_8.data.AppDatabase
import com.example.lab_8.repository.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TransactionRepository(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAddTransaction() = runBlocking {
        repository.addTransaction("Test", 100.0, true, "USD")
        val transactions = repository.transactions.value
        assertEquals(1, transactions.size)
        assertEquals("Test", transactions[0].description)
    }

    @Test
    fun testDeleteTransaction() = runBlocking {
        repository.addTransaction("To delete", 50.0, false, "EUR")
        val id = repository.transactions.value[0].id
        repository.deleteTransaction(id)
        assertEquals(0, repository.transactions.value.size)
    }
}
