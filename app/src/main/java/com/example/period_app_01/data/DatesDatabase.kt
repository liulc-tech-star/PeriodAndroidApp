package com.example.period_app_01.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/*
 * Room database configuration
 * Version 4: Using PeriodRecord for calendar-based tracking
 * Old Dates table removed
 */
@Database(entities = [PeriodRecord::class], version = 5, exportSchema = false)
@TypeConverters(DatesConverter::class)
abstract class DatesDatabase : RoomDatabase() {
    abstract fun periodRecordDao(): PeriodRecordDao

    companion object {
        @Volatile
        private var Instance: DatesDatabase? = null

        /**
         * 获取数据库单例
         * 使用 synchronized 防止多线程同时访问
         */
        fun getDatabase(context: Context): DatesDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DatesDatabase::class.java, "dates_database")
                    .fallbackToDestructiveMigration(true)
                    .build().also { Instance = it }
            }
        }
    }
}