package com.example.lab_8.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.util.Date

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

@Database(entities = [TransactionEntity::class], version = 2)
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