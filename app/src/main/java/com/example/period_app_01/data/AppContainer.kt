package com.example.period_app_01.data

import android.content.Context


interface AppContainer {
    val datesRepository: DatesRepository
}

// extending AppContainer interface
class AppDataContainer(private val context: Context) : AppContainer {
    override val datesRepository: DatesRepository by lazy {
        // instantiating database instance and DAO
        OfflineDatesRepository(DatesDatabase.getDatabase(context).datesDao())
    }
}