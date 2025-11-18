package com.example.period_app_01.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/*
 * passing arguments for Room (persistence library) to build the database
 * passing Dates class, version corresponds ot Dates class configuration
 * and requires uninstalling app from emulator when changed
 */
@Database(entities = [Dates::class], version = 3, exportSchema = false)
//
@TypeConverters(DatesConverter::class)
// extends RoomDatabase class (built-in class)
abstract class DatesDatabase : RoomDatabase() {
    // function which returns the DAO (make database aware of DAO's existence)
    abstract fun datesDao(): DatesDao

    companion object {
        // ensures changes made to Instance (database) are harmonized across all threads
        @Volatile
        // variable keeps reference to database, when created, so single instance opened at a time
        private var Instance: DatesDatabase? = null

        // function which returns database
        fun getDatabase(context: Context): DatesDatabase {
            /*
             * synchronized keyword prevents multiple threads from accessing database at once
             * avoids race condition, so database is only initialized once
             * either returns Instance (database) or if null, initializes it (elvis operator)
             * this refers to the companion object
             */
            return Instance ?: synchronized(this) {
                // building the database instance
                Room.databaseBuilder(context, DatesDatabase::class.java, "dates_database")
                    .fallbackToDestructiveMigration()
                    .build().also { Instance = it }
            }
        }
    }
}